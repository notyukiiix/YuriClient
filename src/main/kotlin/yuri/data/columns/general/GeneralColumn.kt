package yuri.data.columns.general

import yuri.YuriData
import yuri.data.columns.general.modules.ChatModule
import yuri.data.columns.general.modules.SillySpeakModule

object GeneralColumn {
    @JvmStatic
    fun createColumn(): YuriData.Column = YuriData.Column("General", 26, 42, SillySpeakModule.module, ChatModule.module)
}
