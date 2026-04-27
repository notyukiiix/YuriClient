package yuri.data.columns.visual.modules

import yuri.client.FullbrightLightmapCacheUtil
import net.minecraft.client.Minecraft
import yuri.YuriData

/**
 * Clears the GPU lightmap pipeline/shader cache so the lightmap fragment shader is recompiled.
 * When the module is toggled, deferred execution matches vanilla timing for shader reload.
 */
object FullbrightModule {
    private const val TITLE = "Fullbright"

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    private var lastEnabled: Boolean = false

    @JvmStatic
    fun onClientTick(client: Minecraft) {
        val enabled = module.enabled
        if (enabled == lastEnabled) {
            return
        }
        lastEnabled = enabled
        client.execute { FullbrightLightmapCacheUtil.invalidateLightmapCaches() }
    }
}
