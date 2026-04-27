package yuri.data.columns.visual.modules

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import yuri.YuriData

object YuriHudMetricsModule {
    private const val TITLE = "Yuri HUD (FPS/TPS)"

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    private var registered = false

    @JvmStatic
    fun registerEvents() {
        if (registered) return
        registered = true
        HudRenderCallback.EVENT.register { graphics: GuiGraphics, _ ->
            if (!module.enabled) return@register
            val mc = Minecraft.getInstance()
            val font = mc.font
            val x = 4
            var y = mc.window.guiScaledHeight - 28
            graphics.drawString(font, "${mc.fps} fps", x, y, 0xFFE672E6.toInt(), true)
            y += 10
            val tps = mc.level?.tickRateManager()?.tickrate()?.toString() ?: "?"
            graphics.drawString(font, "TPS: $tps", x, y, 0xFF0072FF.toInt(), true)
        }
    }
}
