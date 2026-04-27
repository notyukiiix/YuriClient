package yuri

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.client.Minecraft
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.UUID

/**
 * Shared cosmetics registry for visuals that should appear to all users running the mod.
 *
 * Add global cosmetic definitions and related helpers here.
 */
object GlobalCosmetics {
    data class PlayerCosmetic(
        val uuid: UUID,
        /** Note-only label for easier lookup in code; runtime replacement resolves actual in-game name by UUID. */
        val noteName: String,
        val customNameCode: String,
        val scaleX: Float,
        val scaleY: Float,
        val scaleZ: Float
    )

    private val cosmetics: Map<UUID, PlayerCosmetic> = listOf(
        PlayerCosmetic(
            UUID.fromString("7e35ce85-fce8-4202-8fad-365977b21c85"),
            "kuru",
            "&#F97E9C&l&oY&#F98AA5&l&ou&#F997AF&l&ok&#F9A3B8&l&oi&#F9AFC1&l&o'&#F9BCCB&l&os &#F9D4DD&l&oW&#F9E0E6&l&oi&#F9EDF0&l&of&#F9F9F9&l&oe",
            0.8f,
            0.8f,
            0.8f
        ),
        PlayerCosmetic(
            UUID.fromString("16ee59f2-7b12-4bfa-9769-039b4a7f978a"),
            "emzy",
            "&#B80968&l&ob&#9F0857&l&oo&#870645&l&oy&#6E0534&l&of&#550323&l&oo&#3D0211&l&oi&#240000&l&od",
            1.2f,
            0.7f,
            1.2f
        ),
        PlayerCosmetic(
            UUID.fromString("0f046eba-34f1-4f5d-b579-317ad6eb88db"),
            "amy",
            "&#691B55&lA&#E894C2&lm&#F4CAE1&ly&#FFFFFF&lU&#E894C2&lw&#691B55&lU",
            0.9f,
            0.9f,
            0.9f
        )
    ).associateBy { it.uuid }
    private val resolvedNames: MutableMap<UUID, String> = mutableMapOf()
    private val resolveInFlight: MutableSet<UUID> = mutableSetOf()
    private val lastRemoteResolveAttemptMs: MutableMap<UUID, Long> = mutableMapOf()
    private val lastRemoteResolveSuccessMs: MutableMap<UUID, Long> = mutableMapOf()
    private val plainByUuid: Map<UUID, String> = cosmetics.mapValues { (_, data) -> plainText(data.customNameCode) }
    private val legacyByUuid: Map<UUID, String> = cosmetics.mapValues { (_, data) -> legacyText(data.customNameCode) }
    private val componentByUuid: Map<UUID, Component> = cosmetics.mapValues { (_, data) -> parseStyledText(data.customNameCode) }
    private var replacementCacheDirty: Boolean = true
    private var cachedPlainReplacementMap: Map<String, String> = emptyMap()
    private var cachedLegacyReplacementMap: Map<String, String> = emptyMap()
    private var cachedComponentReplacementMap: Map<String, Component> = emptyMap()
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .build()
    private const val REMOTE_LOOKUP_RETRY_MS: Long = 60_000
    private const val REMOTE_LOOKUP_SUCCESS_COOLDOWN_MS: Long = 10 * 60_000

    @JvmStatic
    fun init() {
        resolvedNames.clear()
        resolveInFlight.clear()
        lastRemoteResolveAttemptMs.clear()
        lastRemoteResolveSuccessMs.clear()
        replacementCacheDirty = true
    }

    @JvmStatic
    fun refreshResolvedNames(mc: Minecraft) {
        for ((uuid, _) in cosmetics) {
            val resolved = resolveNameForUuid(mc, uuid)
            if (!resolved.isNullOrBlank()) {
                val prev = resolvedNames[uuid]
                if (prev != resolved) {
                    resolvedNames[uuid] = resolved
                    replacementCacheDirty = true
                }
            } else {
                triggerRemoteResolve(uuid)
            }
        }
    }

    @JvmStatic
    fun replacementMap(): Map<String, String> {
        rebuildReplacementCachesIfNeeded()
        return cachedPlainReplacementMap
    }

    @JvmStatic
    fun replacementLegacyMap(): Map<String, String> {
        rebuildReplacementCachesIfNeeded()
        return cachedLegacyReplacementMap
    }

    @JvmStatic
    fun replacementComponentMap(): Map<String, Component> {
        rebuildReplacementCachesIfNeeded()
        return cachedComponentReplacementMap
    }

    private fun rebuildReplacementCachesIfNeeded() {
        if (!replacementCacheDirty) {
            return
        }
        val out = linkedMapOf<String, Component>()
        val plainOut = linkedMapOf<String, String>()
        val legacyOut = linkedMapOf<String, String>()
        for ((uuid, _) in cosmetics) {
            val resolvedName = resolvedNames[uuid]
            if (!resolvedName.isNullOrBlank()) {
                plainOut[resolvedName] = plainByUuid[uuid] ?: continue
                legacyOut[resolvedName] = legacyByUuid[uuid] ?: continue
                out[resolvedName] = componentByUuid[uuid] ?: continue
            }
        }
        cachedPlainReplacementMap = plainOut
        cachedLegacyReplacementMap = legacyOut
        cachedComponentReplacementMap = out
        replacementCacheDirty = false
    }

    @JvmStatic
    fun hasCosmetic(uuid: UUID?): Boolean = uuid != null && cosmetics.containsKey(uuid)

    @JvmStatic
    fun replacementNameFor(uuid: UUID?, fallbackName: String?): String? {
        if (uuid == null) {
            return null
        }
        cosmetics[uuid] ?: return null
        val resolved = resolvedNames[uuid]
        if (!resolved.isNullOrBlank()) {
            return resolved
        }
        return fallbackName?.takeIf { it.isNotBlank() }
    }

    @JvmStatic
    fun replacementComponentFor(uuid: UUID?): Component? {
        if (uuid == null) {
            return null
        }
        cosmetics[uuid] ?: return null
        return componentByUuid[uuid]
    }

    @JvmStatic
    fun replacementLegacyFor(uuid: UUID?): String? {
        if (uuid == null) {
            return null
        }
        cosmetics[uuid] ?: return null
        return legacyByUuid[uuid]
    }

    @JvmStatic
    fun customNameFor(uuid: UUID): Component? {
        cosmetics[uuid] ?: return null
        return componentByUuid[uuid]
    }

    @JvmStatic
    fun customScaleFor(uuid: UUID): FloatArray? {
        val data = cosmetics[uuid] ?: return null
        return floatArrayOf(data.scaleX, data.scaleY, data.scaleZ)
    }

    private fun resolveNameForUuid(mc: Minecraft, uuid: UUID): String? {
        val conn = mc.player?.connection
        if (conn != null) {
            for (info in conn.listedOnlinePlayers) {
                val profile = info.profile ?: continue
                if (profile.id == uuid) {
                    return profile.name
                }
            }
        }
        val level = mc.level
        if (level != null) {
            for (player in level.players()) {
                if (player.uuid == uuid) {
                    return player.gameProfile?.name ?: player.name.string
                }
            }
        }
        return null
    }

    private fun triggerRemoteResolve(uuid: UUID) {
        if (resolvedNames.containsKey(uuid)) {
            return
        }
        val now = System.currentTimeMillis()
        val lastSuccess = lastRemoteResolveSuccessMs[uuid] ?: 0L
        if (now - lastSuccess < REMOTE_LOOKUP_SUCCESS_COOLDOWN_MS) {
            return
        }
        val lastAttempt = lastRemoteResolveAttemptMs[uuid] ?: 0L
        if (now - lastAttempt < REMOTE_LOOKUP_RETRY_MS) {
            return
        }
        if (!resolveInFlight.add(uuid)) {
            return
        }
        lastRemoteResolveAttemptMs[uuid] = now

        val compact = uuid.toString().replace("-", "")
        val req = HttpRequest.newBuilder()
            .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/$compact"))
            .timeout(Duration.ofSeconds(4))
            .GET()
            .build()

        httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
            .whenComplete { resp, _ ->
                try {
                    if (resp != null && resp.statusCode() == 200) {
                        val name = parseMojangName(resp.body())
                        if (!name.isNullOrBlank()) {
                            resolvedNames[uuid] = name
                            lastRemoteResolveSuccessMs[uuid] = System.currentTimeMillis()
                            replacementCacheDirty = true
                        }
                    }
                } finally {
                    resolveInFlight.remove(uuid)
                }
            }
    }

    private fun parseMojangName(json: String?): String? {
        if (json.isNullOrBlank()) {
            return null
        }
        val m = Regex("\"name\"\\s*:\\s*\"([A-Za-z0-9_]{1,16})\"").find(json) ?: return null
        return m.groupValues.getOrNull(1)
    }

    private fun plainText(code: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < code.length) {
            if (i + 8 <= code.length && code[i] == '&' && code[i + 1] == '#') {
                i += 8
                continue
            }
            if (i + 2 <= code.length && code[i] == '&' && (code[i + 1] == 'l' || code[i + 1] == 'o')) {
                i += 2
                continue
            }
            sb.append(code[i])
            i++
        }
        return sb.toString()
    }

    private fun parseStyledText(code: String): Component {
        val root = Component.empty()
        var style = Style.EMPTY
        var i = 0
        while (i < code.length) {
            if (i + 8 <= code.length && code[i] == '&' && code[i + 1] == '#') {
                val hex = "#" + code.substring(i + 2, i + 8)
                val parsed = TextColor.parseColor(hex)
                if (parsed.result().isPresent) {
                    style = style.withColor(parsed.result().get())
                }
                i += 8
                continue
            }
            if (i + 2 <= code.length && code[i] == '&') {
                when (code[i + 1]) {
                    'l' -> {
                        style = style.withBold(true)
                        i += 2
                        continue
                    }
                    'o' -> {
                        style = style.withItalic(true)
                        i += 2
                        continue
                    }
                }
            }
            root.append(Component.literal(code[i].toString()).withStyle(style))
            i++
        }
        return root
    }

    private fun legacyText(code: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < code.length) {
            if (i + 8 <= code.length && code[i] == '&' && code[i + 1] == '#') {
                val hex = code.substring(i + 2, i + 8)
                out.append('\u00A7').append('x')
                for (ch in hex) {
                    out.append('\u00A7').append(ch.lowercaseChar())
                }
                i += 8
                continue
            }
            if (i + 2 <= code.length && code[i] == '&') {
                when (code[i + 1]) {
                    'l', 'L' -> {
                        out.append('\u00A7').append('l')
                        i += 2
                        continue
                    }
                    'o', 'O' -> {
                        out.append('\u00A7').append('o')
                        i += 2
                        continue
                    }
                    'r', 'R' -> {
                        out.append('\u00A7').append('r')
                        i += 2
                        continue
                    }
                }
            }
            out.append(code[i])
            i++
        }
        return out.toString()
    }
}
