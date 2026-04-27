package yuri.data.columns.dungeons.map.core

import yuri.data.columns.dungeons.map.compat.DungeonMapConfig
import yuri.data.columns.dungeons.map.handlers.DungeonPathFinder
import yuri.data.columns.dungeons.map.handlers.DungeonScanner
import java.awt.Color

class Door(override val x: Int, override val z: Int, var type: DoorType) : Tile {
    var opened = false
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() {
            if (state == RoomState.UNOPENED) return DungeonMapConfig.colorUnopenedDoor.value
            val color = when (this.type) {
                DoorType.BLOOD -> DungeonMapConfig.colorBloodDoor
                DoorType.ENTRANCE -> DungeonMapConfig.colorEntranceDoor
                DoorType.WITHER -> if (opened && state != RoomState.UNDISCOVERED) {
                    DungeonMapConfig.colorOpenWitherDoor
                } else {
                    DungeonMapConfig.colorWitherDoor
                }
                else -> DungeonMapConfig.colorRoomDoor
            }.value

            return if (state == RoomState.UNDISCOVERED && !DungeonPathFinder.isFairy(this)) {
                color.darker().darker()
            } else {
                color
            }
        }

    val arrayPos: Pair<Int, Int> = run {
        val step = DungeonScanner.roomSize shr 1
        val row = ((z - DungeonScanner.startZ) / step).coerceIn(0, 10)
        val column = ((x - DungeonScanner.startX) / step).coerceIn(0, 10)
        row to column
    }
}
