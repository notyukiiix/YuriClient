package yuri.data.columns.cheats

import yuri.YuriData
import yuri.data.columns.cheats.modules.MobEspModule
import yuri.data.columns.cheats.modules.TranslucentDoorModule

object CheatsColumn {
    @JvmStatic
    fun createColumn(): YuriData.Column = YuriData.Column(
        "Cheats",
        914,
        42,
        MobEspModule.module,
        TranslucentDoorModule.module
    )
}
