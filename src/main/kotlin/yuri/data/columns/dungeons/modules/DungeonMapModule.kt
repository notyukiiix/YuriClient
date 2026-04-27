package yuri.data.columns.dungeons.modules

import yuri.YuriData
import yuri.client.YuriDungeonHudLayer
import yuri.data.columns.dungeons.map.DungeonMapRuntime
import yuri.util.DungeonUtils

object DungeonMapModule {
    private const val TITLE = "Dungeon map"

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    private var eventsRegistered = false

    @JvmStatic
    fun isActive(): Boolean = module.enabled && DungeonUtils.inDungeons()

    @JvmStatic
    fun registerEvents() {
        if (eventsRegistered) {
            return
        }
        eventsRegistered = true
        DungeonMapRuntime.registerEvents()
        YuriDungeonHudLayer.register()
    }
}
