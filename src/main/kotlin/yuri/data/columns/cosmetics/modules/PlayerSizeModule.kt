package yuri.data.columns.cosmetics.modules

import yuri.YuriData

object PlayerSizeModule {
    private const val TITLE = "Player Size"

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    @JvmField
    val settings: PlayerSizeSettings = PlayerSizeSettings()

    class PlayerSizeSettings {
        @JvmField var scaleX: Float = 1.0F
        @JvmField var scaleY: Float = 1.0F
        @JvmField var scaleZ: Float = 1.0F

        fun resetToDefaults() {
            scaleX = 1.0F
            scaleY = 1.0F
            scaleZ = 1.0F
        }
    }
}
