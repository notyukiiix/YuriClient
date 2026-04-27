package yuri.data.columns.dungeons.map.core

import yuri.data.columns.dungeons.map.DungeonInfo
import yuri.data.columns.dungeons.map.compat.DungeonMapConfig
import yuri.data.columns.dungeons.map.handlers.DungeonScanner
import java.awt.Color
import kotlin.math.roundToInt

class Room(override val x: Int, override val z: Int, var data: RoomData) : Tile {
    var core = 0
    var isSeparator = false
    var uniqueRoom: UniqueRoom? = null

    override var state: RoomState = RoomState.UNDISCOVERED

    override val color: Color
        get() {
            val swatch = if (state == RoomState.UNOPENED) DungeonMapConfig.colorUnopened
            else when (data.type) {
                RoomType.BLOOD -> DungeonMapConfig.colorBlood
                RoomType.CHAMPION -> DungeonMapConfig.colorMiniboss
                RoomType.ENTRANCE -> DungeonMapConfig.colorEntrance
                RoomType.FAIRY -> DungeonMapConfig.colorFairy
                RoomType.PUZZLE -> DungeonMapConfig.colorPuzzle
                RoomType.RARE -> DungeonMapConfig.colorRare
                RoomType.TRAP -> DungeonMapConfig.colorTrap
                else -> DungeonMapConfig.colorRoom
            }
            var c = swatch.value
            if (DungeonMapConfig.highlightMimicRoom && uniqueRoom?.hasMimic == true) {
                c = lerpColor(c, DungeonMapConfig.colorMimic.value, 0.22f)
            }
            return if (DungeonMapConfig.dungeonMapCheater && state == RoomState.UNDISCOVERED) {
                c.darker().darker()
            } else {
                c
            }
        }

    private fun lerpColor(a: Color, b: Color, t: Float): Color {
        val u = t.coerceIn(0f, 1f)
        fun ch(x: Int, y: Int) = (x + (y - x) * u).roundToInt().coerceIn(0, 255)
        return Color(
            ch(a.red, b.red),
            ch(a.green, b.green),
            ch(a.blue, b.blue),
            ch(a.alpha, b.alpha),
        )
    }

    fun getArrayPosition(): Pair<Int, Int> {
        return Pair((x - DungeonScanner.startX) / 16, (z - DungeonScanner.startZ) / 16)
    }

    fun addToUnique(row: Int, column: Int, roomName: String = data.name) {
        val unique = DungeonInfo.uniqueRooms[roomName]

        if (unique == null) {
            UniqueRoom(column, row, this).let {
                DungeonInfo.uniqueRooms[data.name] = it
                uniqueRoom = it
            }
        } else {
            unique.addTile(column, row, this)
            uniqueRoom = unique
        }
    }
}
