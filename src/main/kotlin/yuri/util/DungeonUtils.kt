package yuri.util

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.DisplaySlot

object DungeonUtils {
    private val dungeonKeywords: List<String> = listOf(
        "catacombs",
        "dungeon",
        "crypt",
        "necron",
        "master mode",
        "class ",
        "class:",
        "healer",
        "mage",
        "berserk",
        "archer",
        "tank",
        "floor ",
        "floor:",
        "requeue",
        "spirit",
        "bonzo",
        "professor",
        "thorn",
        "livid",
        "sadan",
        "wither key"
    )

    @JvmStatic
    fun inDungeons(): Boolean {
        val level = Minecraft.getInstance().level ?: return false
        val scoreboard = level.scoreboard
        val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return false
        val title = stripFormatting(objective.displayName) ?: return false
        if (titleLooksLikeDungeon(title)) {
            return true
        }
        for (entry in scoreboard.listPlayerScores(objective)) {
            if (entry.isHidden) {
                continue
            }
            val fromDisplay = stripFormatting(entry.display()) ?: ""
            val fromOwner = stripFormatting(entry.ownerName()) ?: ""
            val combined = "$fromDisplay $fromOwner".lowercase()
            if (titleLooksLikeDungeon(combined)) {
                return true
            }
        }
        return false
    }

    private fun titleLooksLikeDungeon(text: String): Boolean {
        val t = text.lowercase()
        return dungeonKeywords.any { kw -> t.contains(kw) }
    }

    private fun stripFormatting(component: Component?): String? {
        if (component == null) {
            return null
        }
        val raw = component.string.trim()
        if (raw.isEmpty()) {
            return null
        }
        return raw.replace(Regex("(?i)§[0-9A-FK-OR]"), "").trim()
    }
}
