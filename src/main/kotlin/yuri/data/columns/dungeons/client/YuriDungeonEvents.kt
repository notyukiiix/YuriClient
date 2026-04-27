package yuri.data.columns.dungeons.client

import net.minecraft.core.BlockPos
import yuri.data.columns.dungeons.client.enums.SecretType
import yuri.data.columns.dungeons.map.core.UniqueRoom

data class DungeonRoomEnterEvent(val room: UniqueRoom)

data class DungeonSecretFoundEvent(val pos: BlockPos, val type: SecretType)

object YuriDungeonEvents {
    private val roomEnter = mutableListOf<(DungeonRoomEnterEvent) -> Unit>()
    private val secretFound = mutableListOf<(DungeonSecretFoundEvent) -> Unit>()

    fun registerRoomEnter(handler: (DungeonRoomEnterEvent) -> Unit) {
        roomEnter.add(handler)
    }

    fun registerSecretFound(handler: (DungeonSecretFoundEvent) -> Unit) {
        secretFound.add(handler)
    }

    @JvmStatic
    fun postRoomEnter(room: UniqueRoom) {
        val e = DungeonRoomEnterEvent(room)
        roomEnter.toList().forEach { it(e) }
    }

    @JvmStatic
    fun postSecretFound(pos: BlockPos, type: SecretType) {
        val e = DungeonSecretFoundEvent(pos, type)
        secretFound.toList().forEach { it(e) }
    }

    fun clearAll() {
        roomEnter.clear()
        secretFound.clear()
    }
}
