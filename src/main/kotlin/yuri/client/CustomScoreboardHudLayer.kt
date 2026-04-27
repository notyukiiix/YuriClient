package yuri.client

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import yuri.data.columns.dev.modules.CustomScoreboardModule

object CustomScoreboardHudLayer {
    @JvmStatic
    fun register() {
        HudRenderCallback.EVENT.register { graphics, _ ->
            CustomScoreboardModule.render(graphics, false)
        }
    }
}
