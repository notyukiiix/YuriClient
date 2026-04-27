package yuri.client

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import yuri.data.columns.visual.modules.ImageHudModule

object ImageHudHudLayer {
    @JvmStatic
    fun register() {
        HudRenderCallback.EVENT.register { graphics, _ ->
            if (!ImageHudModule.shouldDrawInGameHud()) {
                return@register
            }
            ImageHudModule.renderImage(graphics)
        }
    }
}
