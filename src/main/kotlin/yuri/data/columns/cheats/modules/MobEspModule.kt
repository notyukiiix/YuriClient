package yuri.data.columns.cheats.modules

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ambient.Bat
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.monster.EnderMan
import net.minecraft.world.entity.monster.Giant
import net.minecraft.world.entity.player.Player
import yuri.YuriData
import yuri.data.columns.cheats.client.MobEspWorldRenderer
import yuri.util.DungeonUtils
import java.util.LinkedHashSet

object MobEspModule {
    private const val TITLE = "Mob ESP"
    private const val OPTION_STARRED_ESP = 0
    private const val OPTION_HIDDEN_MOBS = 1
    private const val OPTION_SHOW_FELS = 2
    private const val OPTION_SHOW_SHADOW_ASSASSINS = 3
    private const val OPTION_SHOW_STEALTHY = 4

    private val watcherMobs: Set<String> = setOf(
        "Angry Archaeologist",
        "Frozen Adventurer",
        "Shadow Assassin",
        "Lost Adventurer",
        "Withermancer"
    )

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    private val labels: Array<String> = arrayOf(
        "Starred ESP",
        "Hidden Mobs",
        "Show Fels",
        "Show Shadow Assassins",
        "Show Stealthy"
    )

    private val toggles: BooleanArray = booleanArrayOf(
        true,
        true,
        true,
        true,
        true
    )
    private val starredRegex = Regex("^.*✯ .*\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?[kM]?❤$")
    private var glowColorRgb: Int = 0xFFFF00
    private var starred: MutableSet<Int> = LinkedHashSet()
    private var shadowAssassins: MutableSet<Int> = LinkedHashSet()
    private var eventsRegistered: Boolean = false

    @JvmStatic
    fun optionCount(): Int = labels.size

    @JvmStatic
    fun optionLabel(index: Int): String = labels[index]

    @JvmStatic
    fun isEnabled(index: Int): Boolean = toggles[index]

    @JvmStatic
    fun setEnabled(index: Int, enabled: Boolean) {
        toggles[index] = enabled
    }

    @JvmStatic
    fun toggle(index: Int) {
        toggles[index] = !toggles[index]
    }

    @JvmStatic
    fun glowColor(): Int = 0xFF000000.toInt() or (glowColorRgb and 0xFFFFFF)

    @JvmStatic
    fun glowColorHex(): String = String.format("#%06X", glowColorRgb and 0xFFFFFF)

    @JvmStatic
    fun trySetGlowColor(value: String?): Boolean {
        if (value == null) {
            return false
        }
        var normalized = value.trim()
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1)
        }
        if (normalized.length != 6) {
            return false
        }
        for (ch in normalized) {
            if (Character.digit(ch, 16) < 0) {
                return false
            }
        }
        glowColorRgb = normalized.toInt(16) and 0xFFFFFF
        return true
    }

    @JvmStatic
    fun shouldStarredEsp(): Boolean = module.enabled && toggles[OPTION_STARRED_ESP]

    @JvmStatic
    fun shouldHiddenMobs(): Boolean = module.enabled && toggles[OPTION_HIDDEN_MOBS]

    @JvmStatic
    fun shouldShowHiddenFels(): Boolean = shouldHiddenMobs() && toggles[OPTION_SHOW_FELS]

    @JvmStatic
    fun shouldShowHiddenShadowAssassins(): Boolean = shouldHiddenMobs() && toggles[OPTION_SHOW_SHADOW_ASSASSINS]

    @JvmStatic
    fun shouldShowHiddenStealthy(): Boolean = shouldHiddenMobs() && toggles[OPTION_SHOW_STEALTHY]

    /**
     * Invokes [action] for each loaded entity that should show Mob ESP (starred / shadow assassin targets).
     */
    @JvmStatic
    fun forEachEspTarget(action: (Entity) -> Unit) {
        if (!module.enabled || !DungeonUtils.inDungeons()) {
            return
        }
        val level = Minecraft.getInstance().level ?: return
        val ids = LinkedHashSet<Int>(starred.size + shadowAssassins.size)
        ids.addAll(starred)
        ids.addAll(shadowAssassins)
        for (id in ids) {
            val entity = level.getEntity(id) ?: continue
            if (entity.isRemoved) {
                continue
            }
            action(entity)
        }
    }

    @JvmStatic
    fun registerEvents() {
        if (eventsRegistered) {
            return
        }
        eventsRegistered = true
        WorldRenderEvents.END_MAIN.register(MobEspWorldRenderer::render)
        ClientTickEvents.END_WORLD_TICK.register(ClientTickEvents.EndWorldTick { world ->
            if (!module.enabled || !DungeonUtils.inDungeons()) {
                starred.clear()
                shadowAssassins.clear()
                return@EndWorldTick
            }
            updateTrackedEntities(world.entitiesForRendering())
        })
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(ClientWorldEvents.AfterClientWorldChange { _, _ ->
            starred.clear()
            shadowAssassins.clear()
        })
    }

    @JvmStatic
    fun shouldRevealHiddenMob(entity: Entity): Boolean {
        if (!shouldHiddenMobs() || !DungeonUtils.inDungeons() || !entity.isInvisible) {
            return false
        }
        if (!shouldShowHiddenFels() && !shouldShowHiddenShadowAssassins() && !shouldShowHiddenStealthy()) {
            return false
        }
        val name = displayName(entity) ?: return false
        if (name.isEmpty()) {
            return false
        }
        val isFel = shouldShowHiddenFels() && entity is EnderMan && name == "Dinnerbone"
        val isShadowAssassin = shouldShowHiddenShadowAssassins() &&
            entity is AbstractClientPlayer &&
            name.contains("Shadow Assassin", ignoreCase = true)
        val isWatcherMob = shouldShowHiddenStealthy() &&
            entity is AbstractClientPlayer &&
            watcherMobs.any { it.equals(name, ignoreCase = true) }
        val isGiant = shouldShowHiddenStealthy() && entity is Giant && !entity.getItemBySlot(EquipmentSlot.FEET).isEmpty
        return isFel || isShadowAssassin || isWatcherMob || isGiant
    }

    @JvmStatic
    fun shouldStarredMobEsp(entity: Entity): Boolean {
        if (!module.enabled || !DungeonUtils.inDungeons()) {
            return false
        }
        if (shouldStarredEsp() && starred.contains(entity.id)) return true
        if (shouldShowHiddenShadowAssassins() && shadowAssassins.contains(entity.id)) return true
        return false
    }

    @JvmStatic
    fun onEntityDataUpdated(entityId: Int) {
        // Kept for compatibility with existing packet hooks.
        if (!module.enabled || !DungeonUtils.inDungeons()) return
        val level = Minecraft.getInstance().level ?: return
        val entity = level.getEntity(entityId) ?: return
        if (entity.isRemoved) {
            starred.remove(entityId)
            shadowAssassins.remove(entityId)
        }
    }

    private fun updateTrackedEntities(entities: Iterable<Entity>) {
        val localPlayer = Minecraft.getInstance().player
        val currentStarred = LinkedHashSet<Int>()
        val currentShadow = LinkedHashSet<Int>()

        for (entity in entities) {
            if (entity is AbstractClientPlayer && entity.name.string == "Shadow Assassin") {
                currentShadow.add(entity.id)
            }
            if (entity !is ArmorStand || !entity.isInvisible) {
                continue
            }
            val strippedName = stripFormatting(entity.customName) ?: continue
            if (!starredRegex.matches(strippedName)) {
                continue
            }
            val owner = findStarOwner(entity, entities, localPlayer) ?: continue
            currentStarred.add(owner.id)
        }

        starred = currentStarred
        shadowAssassins = currentShadow
    }

    private fun findStarOwner(marker: ArmorStand, entities: Iterable<Entity>, localPlayer: LocalPlayer?): Entity? {
        val markerPos = marker.position()
        val markerX = round1(markerPos.x)
        val markerZ = round1(markerPos.z)
        return entities.firstOrNull { candidate ->
            if (!isValidStarCandidate(candidate, localPlayer)) {
                return@firstOrNull false
            }
            val pos = candidate.position()
            round1(pos.x) == markerX && round1(pos.z) == markerZ
        }
    }

    private fun isValidStarCandidate(entity: Entity, localPlayer: LocalPlayer?): Boolean {
        if (entity is ArmorStand || entity.isRemoved) {
            return false
        }
        if (entity is Player) {
            return entity != localPlayer
        }
        return true
    }

    private fun round1(value: Double): Double = kotlin.math.round(value * 10.0) / 10.0

    private fun stripFormatting(component: Component?): String? {
        val raw = component?.string?.trim() ?: return null
        if (raw.isEmpty()) return null
        val noSection = raw.replace(Regex("(?i)§[0-9A-FK-OR]"), "")
        return noSection.trim()
    }

    private fun displayName(entity: Entity): String? =
        entity.customName?.string?.trim()?.takeIf { it.isNotEmpty() }

    private fun isStarredName(name: String): Boolean {
        if (name.isEmpty()) {
            return false
        }
        for (ch in name) {
            when (ch) {
                '✯', '✭', '★', '☆', '⭐', '✦', '✧', '✪', '❋', '※' -> return true
            }
        }
        return false
    }
}
