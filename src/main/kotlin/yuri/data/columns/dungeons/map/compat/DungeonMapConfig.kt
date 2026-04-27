package yuri.data.columns.dungeons.map.compat

import java.awt.Color

/** Default dungeon map colours / layout config. */
class MapColorVar(@JvmField var value: Color)

object DungeonMapConfig {
    @JvmField var dungeonMapCheater: Boolean = false

    @JvmField var centerStyle: Boolean = true

    /** Extra lines under the map (secrets / score / deaths). */
    @JvmField var mapExtraInfo: Boolean = false

    @JvmField var mapHideInBoss: Boolean = true

    @JvmField var highlightMimicRoom: Boolean = true

    val colorMimic = MapColorVar(Color(255, 0, 0))

    @JvmField var boxWitherDoors: Boolean = true

    /** ARGB fill for wither door world boxes when the player has no wither key. */
    @JvmField var witherDoorNoKeyArgb: Int = 0x55FF0000.toInt()

    /** ARGB fill when the player has at least one wither key. */
    @JvmField var witherDoorKeyArgb: Int = 0x5500FF00.toInt()

    val colorBlood = MapColorVar(Color(178, 0, 0))
    val colorEntrance = MapColorVar(Color(0, 255, 0))
    val colorFairy = MapColorVar(Color(227, 155, 226))
    val colorMiniboss = MapColorVar(Color(255, 200, 0))
    val colorRoom = MapColorVar(Color(121, 70, 0))
    val colorPuzzle = MapColorVar(Color(123, 0, 123))
    val colorRare = MapColorVar(Color(178, 178, 178))
    val colorTrap = MapColorVar(Color(255, 130, 0))
    val colorUnopened = MapColorVar(Color(65, 65, 65))

    val colorBloodDoor = MapColorVar(Color(178, 0, 0))
    val colorEntranceDoor = MapColorVar(Color(0, 255, 0))
    val colorRoomDoor = MapColorVar(Color(121, 70, 0))
    val colorWitherDoor = MapColorVar(Color(16, 16, 16))
    val colorOpenWitherDoor = MapColorVar(Color(121, 70, 0))
    val colorUnopenedDoor = MapColorVar(Color(65, 65, 65))
}
