package yuri.data.columns.visual.modules

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import yuri.YuriData
import yuri.data.columns.visual.client.YuriVisualPorts

object YuriVisualBundleModule {
    private const val TITLE = "Yuri visual extras (stubs)"

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    private var registered = false
    private var stubsLoaded = false

    @JvmStatic
    fun registerEvents() {
        if (registered) return
        registered = true
        ClientTickEvents.END_CLIENT_TICK.register {
            if (module.enabled && !stubsLoaded) {
                YuriVisualPorts.registerAll()
                stubsLoaded = true
            }
            if (!module.enabled) {
                stubsLoaded = false
            }
        }
    }
}
