package yuri.data.columns.dungeons.map.core

/**
 * [ordinal] matters — compared in map update logic.
 */
enum class RoomState {
    GREEN,
    CLEARED,
    DISCOVERED,
    FAILED,
    UNOPENED,
    UNDISCOVERED
}
