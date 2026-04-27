package yuri.data.columns.dungeons.modules

import yuri.YuriData
import yuri.util.DungeonUtils

object DungeonScoreModule {
    private const val TITLE = "Dungeon score"

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    @JvmStatic
    fun isActive(): Boolean = module.enabled && DungeonUtils.inDungeons()
}
