package yuri.data.columns.dungeons.map

import net.minecraft.world.level.saveddata.maps.MapItemSavedData
import yuri.data.columns.dungeons.map.core.Tile
import yuri.data.columns.dungeons.map.core.UniqueRoom
import yuri.data.columns.dungeons.map.core.Unknown

object DungeonInfo {
    val dungeonList = Array<Tile>(121) { Unknown(0, 0) }
    val uniqueRooms = mutableMapOf<String, UniqueRoom>()

    var roomCount = 0

    var trapType = ""
    var mimicRoom: UniqueRoom? = null
    var witherDoors = 0
    var cryptCount = 0
    var secretCount = 0

    var mapData: MapItemSavedData? = null

    fun reset() {
        dungeonList.fill(Unknown(0, 0))
        roomCount = 0
        uniqueRooms.clear()

        trapType = ""
        witherDoors = 0
        cryptCount = 0
        secretCount = 0

        mapData = null
    }
}
