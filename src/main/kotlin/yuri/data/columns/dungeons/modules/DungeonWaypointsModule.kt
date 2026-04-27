package yuri.data.columns.dungeons.modules

import yuri.YuriData
import yuri.util.DungeonUtils

object DungeonWaypointsModule {
    private const val TITLE = "Dungeon waypoints"

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    @JvmStatic
    fun isActive(): Boolean = module.enabled && DungeonUtils.inDungeons()
}
