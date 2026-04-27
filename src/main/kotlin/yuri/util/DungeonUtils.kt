package yuri.util

import net.minecraft.client.Minecraft
import net.minecraft.world.scores.DisplaySlot

object DungeonUtils {
    @JvmStatic
    fun inDungeons(): Boolean {
        val level = Minecraft.getInstance().level ?: return false
        val objective = level.scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return false
        val title = objective.displayName?.string ?: return false
        val t = title.lowercase()
        return t.contains("catacombs") || t.contains("dungeon")
    }
}
