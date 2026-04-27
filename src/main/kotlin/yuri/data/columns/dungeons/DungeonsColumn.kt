package yuri.data.columns.dungeons

import yuri.YuriData
import yuri.data.columns.dungeons.modules.DungeonClearInfoModule
import yuri.data.columns.dungeons.modules.DungeonMapModule
import yuri.data.columns.dungeons.modules.DungeonScoreModule
import yuri.data.columns.dungeons.modules.DungeonWaypointsModule
import yuri.data.columns.dungeons.modules.KeyHighlightModule

object DungeonsColumn {
    @JvmStatic
    fun createColumn(): YuriData.Column = YuriData.Column(
        "Dungeons",
        174,
        42,
        KeyHighlightModule.module,
        DungeonMapModule.module,
        DungeonWaypointsModule.module,
        DungeonScoreModule.module,
        DungeonClearInfoModule.module
    )
}
