package yuri.data.columns.cheats.modules

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import yuri.YuriData
import yuri.data.columns.cheats.client.DoorEspWorldRenderer
import yuri.data.columns.dungeons.DungeonDoorChatState
import yuri.util.DungeonUtils

object DoorEspModule {
    private const val TITLE = "Door ESP"

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    private var outlineRgb: Int = 0xFFFFFF
    private var openableRgb: Int = 0x00FF00

    private var eventsRegistered: Boolean = false

    @JvmStatic
    fun isActive(): Boolean = module.enabled && DungeonUtils.inDungeons()

    @JvmStatic
    fun outlineArgb(): Int = 0xFF000000.toInt() or (outlineRgb and 0xFFFFFF)

    @JvmStatic
    fun openableArgb(): Int = 0xFF000000.toInt() or (openableRgb and 0xFFFFFF)

    @JvmStatic
    fun outlineColorHex(): String = String.format("#%06X", outlineRgb and 0xFFFFFF)

    @JvmStatic
    fun openableColorHex(): String = String.format("#%06X", openableRgb and 0xFFFFFF)

    @JvmStatic
    fun trySetOutlineColor(value: String?): Boolean = parseHexRgb(value) { outlineRgb = it }

    @JvmStatic
    fun trySetOpenableColor(value: String?): Boolean = parseHexRgb(value) { openableRgb = it }

    @JvmStatic
    fun shouldUseOpenableHighlight(): Boolean =
        DungeonDoorChatState.shouldHighlightOpenableRough()

    @JvmStatic
    fun registerEvents() {
        if (eventsRegistered) {
            return
        }
        eventsRegistered = true
        WorldRenderEvents.END_MAIN.register(DoorEspWorldRenderer::render)
    }

    private fun parseHexRgb(value: String?, setter: (Int) -> Unit): Boolean {
        if (value == null) {
            return false
        }
        var normalized = value.trim()
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1)
        }
        if (normalized.length != 6) {
            return false
        }
        for (ch in normalized) {
            if (Character.digit(ch, 16) < 0) {
                return false
            }
        }
        setter(normalized.toInt(16) and 0xFFFFFF)
        return true
    }
}
