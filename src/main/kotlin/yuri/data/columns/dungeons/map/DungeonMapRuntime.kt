package yuri.data.columns.dungeons.map

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import yuri.client.YuriWitherDoorWorldRenderer
import yuri.data.columns.dungeons.map.compat.DungeonMapLocation
import yuri.data.columns.dungeons.map.handlers.DungeonScanner
import yuri.data.columns.dungeons.map.handlers.MapUpdater
import yuri.data.columns.dungeons.map.handlers.ScoreCalculation
import yuri.data.columns.dungeons.map.utils.MapUtils
import yuri.data.columns.dungeons.map.utils.ScanUtils
import yuri.data.columns.dungeons.modules.DungeonMapModule

object DungeonMapRuntime {
    private var registered = false

    @JvmStatic
    fun registerEvents() {
        if (registered) return
        registered = true
        WorldRenderEvents.END_MAIN.register(YuriWitherDoorWorldRenderer::render)
        ScanUtils.register()
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { _, _ ->
            resetForWorldChange()
        }
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (DungeonMapModule.isActive()) {
                DungeonMapLocation.refreshFromScoreboard()
                ScanUtils.onClientTick(client)
                if (DungeonScanner.shouldScan) {
                    DungeonScanner.scan()
                }
                if (DungeonInfo.mapData != null) {
                    if (!MapUtils.calibrated) {
                        if (MapUtils.calibrateMap()) {
                            MapUtils.calibrated = true
                        }
                    }
                    MapUpdater.updateRooms()
                    MapUpdater.updatePlayers()
                }
                if (DungeonInfo.mimicRoom == null &&
                    (DungeonMapLocation.dungeonFloorNumber ?: 0) > 5 &&
                    !DungeonMapLocation.inBoss &&
                    !ScoreCalculation.mimicKilled
                ) {
                    DungeonScanner.findMimicRoom()?.let { room ->
                        DungeonInfo.mimicRoom = room
                        room.hasMimic = true
                    }
                }
            }
        }
    }

    @JvmStatic
    fun resetForWorldChange() {
        DungeonInfo.reset()
        MapUtils.reset()
        DungeonScanner.hasScanned = false
        MapUtils.calibrated = false
    }
}
