package yuri.data.columns.cheats.modules

import yuri.YuriData
import yuri.util.DungeonUtils

object TranslucentDoorModule {
    private const val TITLE = "Translucent Door"
    private const val ALPHA_MIN = 0
    private const val ALPHA_MAX = 255
    private const val DEFAULT_DOOR_ALPHA = 100

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    /** Vertex alpha for dungeon door blocks (0–255). */
    private var doorAlpha: Int = DEFAULT_DOOR_ALPHA

    @JvmStatic
    fun doorsAlpha(): Int = doorAlpha

    @JvmStatic
    fun defaultDoorAlpha(): Int = DEFAULT_DOOR_ALPHA

    @JvmStatic
    fun setDoorsAlpha(value: Int) {
        doorAlpha = value.coerceIn(ALPHA_MIN, ALPHA_MAX)
    }

    @JvmStatic
    fun resetDoorAlphaToDefault() {
        doorAlpha = DEFAULT_DOOR_ALPHA
    }

    @JvmStatic
    fun isActive(): Boolean = module.enabled && DungeonUtils.inDungeons()
}
