package yuri.data.columns.dungeons.modules

import yuri.YuriData
import yuri.util.DungeonUtils

object DungeonClearInfoModule {
    private const val TITLE = "Dungeon clear info"

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    @JvmStatic
    fun isActive(): Boolean = module.enabled && DungeonUtils.inDungeons()
}
