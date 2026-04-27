package yuri.data.columns.dungeons.modules

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import yuri.YuriData
import yuri.data.columns.dungeons.DungeonDoorChatState
import yuri.util.DungeonUtils

object KeyHighlightModule {
    private const val TITLE = "Key highlight"

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    private var registered: Boolean = false

    @JvmStatic
    fun registerEvents() {
        if (registered) {
            return
        }
        registered = true
        HudRenderCallback.EVENT.register { graphics, _ ->
            renderHud(graphics)
        }
    }

    private fun renderHud(graphics: GuiGraphics) {
        if (!module.enabled || !DungeonUtils.inDungeons()) {
            return
        }
        val client = Minecraft.getInstance()
        val font = client.font
        var y = 6
        val x = 6
        val line = 0xE0FFFFFF.toInt()
        val accent = 0xFF55FF55.toInt()
        graphics.drawString(font, "Wither keys: ${DungeonDoorChatState.getWitherKeys()}", x, y, accent, true)
        y += 11
        val blood = if (DungeonDoorChatState.hasBloodKey()) "Blood key: yes" else "Blood key: no"
        graphics.drawString(font, blood, x, y, line, true)
        y += 11
        if (DungeonDoorChatState.isBloodDoorOpened()) {
            graphics.drawString(font, "Blood door opened", x, y, line, true)
        }
    }
}
