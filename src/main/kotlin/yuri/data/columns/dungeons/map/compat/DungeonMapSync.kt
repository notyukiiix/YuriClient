package yuri.data.columns.dungeons.map.compat

/** No-op optional external map sync bridge (WebSocket, etc.). */
object DungeonMapSync {
    @JvmStatic
    fun send(packet: Any) {
    }
}
