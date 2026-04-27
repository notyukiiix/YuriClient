package yuri.client

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.gui.GuiGraphics

/** Single HUD callback for dungeon map + summary to reduce listener overhead. */
object YuriDungeonHudLayer {
    private var registered = false

    @JvmStatic
    fun register() {
        if (registered) return
        registered = true
        HudRenderCallback.EVENT.register { graphics: GuiGraphics, _ ->
            YuriDungeonMapHud.render(graphics)
            YuriDungeonSummaryHud.render(graphics)
        }
    }
}
