package yuri.client

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.PlainTextContents
import yuri.GlobalCosmetics
import yuri.data.columns.dev.modules.OpsecModule

object OpsecTextMasker {
    @JvmStatic
    fun maskComponent(input: Component?): Component? {
        if (input == null) {
            return null
        }
        return maskTree(input)
    }

    @JvmStatic
    fun maskComponentForPlayer(input: Component?, playerUuid: java.util.UUID?, fallbackName: String?): Component? {
        if (input == null) {
            return null
        }
        return maskTree(input, playerUuid, fallbackName)
    }

    @JvmStatic
    fun maskString(input: String?): String {
        if (input.isNullOrEmpty()) {
            return input ?: ""
        }
        return applyStringMask(input, null, null)
    }

    private fun maskTree(node: Component, forcedUuid: java.util.UUID? = null, fallbackName: String? = null): Component {
        val contents = node.contents
        val rebuilt: MutableComponent = if (contents is PlainTextContents) {
            val before = contents.text()
            val componentAfter = replaceInComponentText(before, node.style, forcedUuid, fallbackName)
            componentAfter ?: run {
                val after = applyStringMask(before, forcedUuid, fallbackName)
                if (after == before) {
                    MutableComponent.create(contents).setStyle(node.style)
                } else {
                    MutableComponent.create(PlainTextContents.create(after)).setStyle(node.style)
                }
            }
        } else {
            MutableComponent.create(contents).setStyle(node.style)
        }
        for (sibling in node.siblings) {
            rebuilt.append(maskTree(sibling, forcedUuid, fallbackName))
        }
        return rebuilt
    }

    private fun replaceInComponentText(
        text: String,
        baseStyle: net.minecraft.network.chat.Style,
        forcedUuid: java.util.UUID?,
        fallbackName: String?
    ): MutableComponent? {
        if (text.isEmpty()) {
            return null
        }
        val replacements = linkedMapOf<String, Component>()
        replacements.putAll(GlobalCosmetics.replacementComponentMap())
        val forcedName = GlobalCosmetics.replacementNameFor(forcedUuid, fallbackName)
        val forcedComponent = GlobalCosmetics.replacementComponentFor(forcedUuid)
        if (!forcedName.isNullOrBlank() && forcedComponent != null) {
            replacements[forcedName] = forcedComponent
        }
        if (replacements.isEmpty()) {
            return null
        }

        var cursor = 0
        var changed = false
        val out = Component.empty().setStyle(baseStyle)

        while (cursor < text.length) {
            var bestIndex = -1
            var bestName: String? = null
            for (name in replacements.keys) {
                val idx = indexOfIgnoreCase(text, name, cursor)
                if (idx >= 0 && (bestIndex < 0 || idx < bestIndex)) {
                    bestIndex = idx
                    bestName = name
                }
            }
            if (bestIndex < 0 || bestName == null) {
                val tail = applyOpsec(text.substring(cursor))
                out.append(Component.literal(tail).setStyle(baseStyle))
                break
            }
            if (bestIndex > cursor) {
                val head = applyOpsec(text.substring(cursor, bestIndex))
                out.append(Component.literal(head).setStyle(baseStyle))
            }
            val replacement = replacements[bestName]
            if (replacement != null) {
                out.append(replacement.copy())
                changed = true
            } else {
                out.append(Component.literal(bestName).setStyle(baseStyle))
            }
            cursor = bestIndex + bestName.length
        }

        return if (changed) out else null
    }

    private fun applyStringMask(input: String, forcedUuid: java.util.UUID?, fallbackName: String?): String {
        var out = input
        for ((name, replacement) in GlobalCosmetics.replacementLegacyMap()) {
            out = out.replace(name, replacement, ignoreCase = true)
        }
        val forcedName = GlobalCosmetics.replacementNameFor(forcedUuid, fallbackName)
        val forcedReplacement = GlobalCosmetics.replacementLegacyFor(forcedUuid)
        if (!forcedName.isNullOrBlank() && !forcedReplacement.isNullOrBlank()) {
            out = out.replace(forcedName, forcedReplacement, ignoreCase = true)
        }
        return applyOpsec(out)
    }

    private fun indexOfIgnoreCase(text: String, needle: String, startIndex: Int): Int {
        if (needle.isEmpty()) {
            return startIndex.coerceIn(0, text.length)
        }
        val start = startIndex.coerceAtLeast(0)
        val maxStart = text.length - needle.length
        if (start > maxStart) {
            return -1
        }
        for (i in start..maxStart) {
            if (text.regionMatches(i, needle, 0, needle.length, ignoreCase = true)) {
                return i
            }
        }
        return -1
    }

    private fun applyOpsec(input: String): String {
        val replacement = OpsecModule.nameChange.trim()
        if (!OpsecModule.module.enabled || replacement.isEmpty()) {
            return input
        }
        val mc = Minecraft.getInstance()
        val ownName = mc.player?.gameProfile?.name ?: mc.user?.name ?: ""
        if (ownName.isEmpty()) {
            return input
        }
        return input.replace(ownName, replacement, ignoreCase = false)
    }
}
