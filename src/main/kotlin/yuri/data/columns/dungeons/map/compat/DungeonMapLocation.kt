package yuri.data.columns.dungeons.map.compat

import net.minecraft.client.Minecraft
import net.minecraft.world.scores.DisplaySlot

/**
 * Floor / boss heuristics for map calibration.
 */
object DungeonMapLocation {
    /** Catacombs floor index: 0 = entrance, 1–6 = F1–F6, 7 = F7/Master, or -1 if unknown. */
    @JvmField
    var dungeonFloorNumber: Int = -1

    @JvmField
    var inBoss: Boolean = false

    fun refreshFromScoreboard() {
        val mc = Minecraft.getInstance()
        val objective = mc.level?.scoreboard?.getDisplayObjective(DisplaySlot.SIDEBAR) ?: run {
            dungeonFloorNumber = -1
            inBoss = false
            return
        }
        val title = objective.displayName.string.lowercase().replace(Regex("§."), "")
        inBoss = title.contains("boss") || title.contains("necron") || title.contains("wither king")

        val floorMatch = Regex("""\(f\s*(\d+|e|m)\)""").find(title)
        dungeonFloorNumber = when {
            floorMatch == null -> -1
            floorMatch.groupValues[1] == "e" -> 0
            floorMatch.groupValues[1] == "m" -> 7
            else -> floorMatch.groupValues[1].toIntOrNull()?.coerceIn(0, 7) ?: -1
        }
    }
}
