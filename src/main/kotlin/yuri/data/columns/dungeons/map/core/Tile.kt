package yuri.data.columns.dungeons.map.core

import java.awt.Color

interface Tile {
    val x: Int
    val z: Int
    var state: RoomState
    val color: Color
}
