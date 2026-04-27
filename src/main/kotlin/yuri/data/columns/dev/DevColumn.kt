package yuri.data.columns.dev

import yuri.YuriData
import yuri.data.columns.dev.modules.ClickGuiModule
import yuri.data.columns.dev.modules.CustomScoreboardModule
import yuri.data.columns.dev.modules.OpsecModule

object DevColumn {
    @JvmStatic
    fun createColumn(): YuriData.Column = YuriData.Column(
        "Dev",
        766,
        42,
        ClickGuiModule.module,
        OpsecModule.module,
        CustomScoreboardModule.module
    )
}
