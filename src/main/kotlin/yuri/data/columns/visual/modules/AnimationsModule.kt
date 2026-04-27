package yuri.data.columns.visual.modules

import yuri.YuriData

object AnimationsModule {
    private const val TITLE = "Animations"

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    @JvmField
    val settings: AnimationSettings = AnimationSettings()

    class AnimationSettings {
        @JvmField var size: Int = 6
        @JvmField var posX: Float = 0.0F
        @JvmField var posY: Float = 0.0F
        @JvmField var posZ: Float = 0.0F
        @JvmField var swingSpeed: Int = 10
        @JvmField var ignoreHaste: Boolean = false
        @JvmField var ignoreEquip: Boolean = false

        fun resetToDefaults() {
            size = 6
            posX = 0.0F
            posY = 0.0F
            posZ = 0.0F
            swingSpeed = 10
            ignoreHaste = false
            ignoreEquip = false
        }
    }
}
