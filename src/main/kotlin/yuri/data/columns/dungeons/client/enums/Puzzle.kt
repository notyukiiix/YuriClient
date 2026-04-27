package yuri.data.columns.dungeons.client.enums

import yuri.data.columns.dungeons.map.DungeonInfo
import yuri.data.columns.dungeons.map.compat.equalsOneOf
import yuri.data.columns.dungeons.map.core.RoomState

enum class Puzzle(val roomDataName: String, val tabName: String = roomDataName) {
    BOMB_DEFUSE("Bomb Defuse"), // Rest in peace
    BOULDER("Boulder"),
    CREEPER_BEAMS("Creeper Beams"),
    HIGHER_BLAZE("Higher Blaze", "Higher Or Lower"),
    ICE_FILL("Ice Fill"),
    ICE_PATH("Ice Path"),
    LOWER_BLAZE("Lower Blaze", "Higher Or Lower"),
    QUIZ("Quiz"),
    TELEPORT_MAZE("Teleport Maze"),
    THREE_WEIRDOS("Three Weirdos"),
    TIC_TAC_TOE("Tic Tac Toe"),
    WATER_BOARD("Water Board"),
    UNKNOWN("???");

    companion object {
        fun fromName(name: String) = entries.find { e ->
            name.equalsOneOf(e.roomDataName, e.tabName)
        }
    }

    var state = if (roomDataName == "???") RoomState.UNOPENED else RoomState.DISCOVERED

    val room get() = DungeonInfo.uniqueRooms[roomDataName]
}
