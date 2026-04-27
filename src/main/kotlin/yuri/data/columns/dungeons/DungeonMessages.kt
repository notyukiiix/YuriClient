package yuri.data.columns.dungeons

import java.util.regex.Pattern

/**
 * Hypixel dungeon chat patterns (matches intent of legitcat CEsp dungeon strings).
 */
object DungeonMessages {
    @JvmField
    val witherKeyClaimPattern: Pattern =
        Pattern.compile("(?:\\[[A-Za-z+]+] )?([A-Za-z0-9_]+) has obtained Wither Key!")

    @JvmField
    val witherKeyPickedUpString: String = "A Wither Key was picked up!"

    @JvmField
    val witherDoorOpenPattern: Pattern =
        Pattern.compile("([A-Za-z0-9_]+) opened a WITHER door!")

    @JvmField
    val bloodKeyClaimPattern: Pattern =
        Pattern.compile("(?:\\[[A-Za-z+]+] )?([A-Za-z0-9_]+) has obtained Blood Key!")

    @JvmField
    val bloodKeyPickedUpString: String = "A Blood Key was picked up!"

    @JvmField
    val bloodDoorOpenMessage: String = "The BLOOD DOOR has been opened!"
}
