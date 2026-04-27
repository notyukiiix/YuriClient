package yuri.data.columns.dungeons.map.handlers

import yuri.data.columns.dungeons.map.DungeonInfo
import yuri.data.columns.dungeons.map.compat.DungeonMapLocation
import yuri.data.columns.dungeons.client.YuriDungeonListener
import yuri.data.columns.dungeons.map.compat.DungeonMapWorld
import yuri.data.columns.dungeons.map.core.Door
import yuri.data.columns.dungeons.map.core.DoorType
import yuri.data.columns.dungeons.map.core.Room
import yuri.data.columns.dungeons.map.core.RoomState
import yuri.data.columns.dungeons.map.core.RoomType
import yuri.data.columns.dungeons.map.core.Unknown
import yuri.data.columns.dungeons.map.utils.LegacyRegistry

/**
 * Map room updater: map decoration → player smoothing is omitted (API changed in 1.21).
 */
object MapUpdater {

    fun updatePlayers() {
        // Vanilla map decorations are no longer keyed the same way; re-implement if you add a renderer.
    }

    fun onPlayerDeath() {
    }

    fun updateRooms() {
        if (DungeonMapLocation.inBoss) return
        if (YuriDungeonListener.dungeonEnded) return
        if (YuriDungeonListener.thePlayer?.isDead == true) return
        val mapData = DungeonInfo.mapData ?: return
        HotbarMapColorParser.updateMap(mapData)

        for (x in 0..10) {
            for (z in 0..10) {
                val idx = z * 11 + x
                val room = DungeonInfo.dungeonList[idx]
                val mapTile = HotbarMapColorParser.getTile(x, z)

                if (room is Unknown) {
                    DungeonInfo.dungeonList[idx] = mapTile
                    DungeonPathFinder.clearCache()
                    if (mapTile is Room) {
                        val connected = HotbarMapColorParser.getConnected(x, z)
                        connected.firstOrNull { it.data.name != "Unknown" }?.let {
                            mapTile.addToUnique(z, x, it.data.name)
                        }
                    }
                    continue
                }

                if (mapTile.state.ordinal < room.state.ordinal || mapTile is Room && room is Room && mapTile.data.type == RoomType.PUZZLE) {
                    room.state = mapTile.state
                }

                if (mapTile is Room && room is Room && mapTile.data.type != room.data.type) {
                    if (room.data.name == mapTile.data.name) room.data = mapTile.data
                }

                if (mapTile is Door && room is Door) {
                    if (mapTile.type == DoorType.WITHER && room.type != DoorType.WITHER) {
                        room.type = mapTile.type
                    }
                }

                if (room is Door && room.type in setOf(DoorType.ENTRANCE, DoorType.WITHER, DoorType.BLOOD)) {
                    if (mapTile is Door && mapTile.type == DoorType.WITHER) room.opened = false
                    else if (!room.opened) {
                        if (DungeonMapWorld.isChunkLoaded(room.x, room.z)) {
                            val legacy = LegacyRegistry.getLegacyId(DungeonMapWorld.getStateAt(room.x, 69, room.z))
                            if (legacy != null && legacy in setOf(0, 166)) {
                                room.opened = true
                            }
                        } else if (mapTile is Door && mapTile.state == RoomState.DISCOVERED) {
                            if (room.type == DoorType.BLOOD) {
                                val bloodRoom = DungeonInfo.dungeonList.filterIsInstance<Room>().find { it.data.type == RoomType.BLOOD }
                                if (bloodRoom != null && bloodRoom.state != RoomState.UNOPENED) room.opened = true
                            } else {
                                room.opened = true
                            }
                        }
                    }
                }
            }
        }
    }
}
