package yuri.data.columns.dungeons.map.handlers

import yuri.data.columns.dungeons.map.core.RoomData
import yuri.data.columns.dungeons.map.core.RoomState
import yuri.data.columns.dungeons.client.DungeonPlayer

/**
 * Clear-info updater shell. Hypixel profile / chat integration is not wired yet.
 */
object ClearInfoUpdater {
    @JvmStatic
    fun checkSplits(room: RoomData, oldState: RoomState, newState: RoomState, players: List<DungeonPlayer>) {
    }

    @JvmStatic
    fun updateDeaths(player: String, reason: String) {
    }

    @JvmStatic
    fun initStartSecrets() {
    }

    @JvmStatic
    fun sendClearInfoMessage() {
    }
}
