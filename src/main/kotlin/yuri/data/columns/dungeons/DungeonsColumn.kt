package yuri.data.columns.dungeons

import yuri.YuriData
import yuri.data.columns.dungeons.modules.KeyHighlightModule

object DungeonsColumn {
    @JvmStatic
    fun createColumn(): YuriData.Column = YuriData.Column(
        "Dungeons",
        174,
        42,
        KeyHighlightModule.module
    )
}
