package yuri.data.columns.cosmetics

import yuri.YuriData
import yuri.data.columns.cosmetics.modules.PlayerSizeModule

object CosmeticsColumn {
    @JvmStatic
    fun createColumn(): YuriData.Column = YuriData.Column("Cosmetics", 618, 42, PlayerSizeModule.module)
}
