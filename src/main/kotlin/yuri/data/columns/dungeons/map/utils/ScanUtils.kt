package yuri.data.columns.dungeons.map.utils

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import yuri.data.columns.dungeons.map.DungeonInfo
import yuri.data.columns.dungeons.map.compat.DungeonMapRoomLoader
import yuri.data.columns.dungeons.map.compat.DungeonMapWorld
import yuri.data.columns.dungeons.map.compat.add
import yuri.data.columns.dungeons.map.compat.destructured
import yuri.data.columns.dungeons.map.compat.equalsOneOf
import yuri.data.columns.dungeons.map.compat.rotate
import yuri.data.columns.dungeons.map.core.Room
import yuri.data.columns.dungeons.map.core.RoomData
import yuri.data.columns.dungeons.map.core.UniqueRoom
import yuri.data.columns.dungeons.map.handlers.DungeonScanner
import yuri.data.columns.dungeons.client.YuriDungeonEvents
import yuri.util.DungeonUtils
import kotlin.math.roundToInt

object ScanUtils {
    val roomList: List<RoomData> by lazy { DungeonMapRoomLoader.load() }

    private var tickAccumulator = 0

    @JvmField
    var currentRoom: UniqueRoom? = null

    @JvmField
    var lastKnownRoom: UniqueRoom? = null

    fun register() {
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { _, _ ->
            currentRoom = null
            lastKnownRoom = null
        }
    }

    fun onClientTick(client: Minecraft) {
        tickAccumulator++
        if (tickAccumulator < 5) {
            return
        }
        tickAccumulator = 0
        if (!DungeonUtils.inDungeons()) {
            return
        }
        client.execute {
            val player = client.player ?: return@execute
            val room = getRoomFromPos(player.position())
            if (currentRoom == room) {
                return@execute
            }
            lastKnownRoom = currentRoom
            currentRoom = room
            room?.let { YuriDungeonEvents.postRoomEnter(it) }
        }
    }

    fun getRoomData(hash: Int): RoomData? = roomList.find { hash in it.cores }

    fun getRoomData(name: String): RoomData? = roomList.find { it.name == name }

    fun getRoomGraf(pos: Vec3): Pair<Int, Int> {
        val roomIndexX = ((pos.x - DungeonScanner.startX) / DungeonScanner.roomSize).roundToInt()
        val roomIndexZ = ((pos.z - DungeonScanner.startZ) / DungeonScanner.roomSize).roundToInt()
        val gridX = roomIndexX * 2
        val gridZ = roomIndexZ * 2
        return gridX.coerceIn(0, 10) to gridZ.coerceIn(0, 10)
    }

    fun getRoomFromPos(vec: Vec3): UniqueRoom? {
        val (gx, gz) = getRoomGraf(vec)
        val unq = (DungeonInfo.dungeonList[gz * 11 + gx] as? Room)?.uniqueRoom
        return unq
    }

    fun getCore(x: Int, z: Int): Int {
        val sb = StringBuilder(150)

        for (y in 140 downTo 12) {
            val id = LegacyRegistry.getLegacyId(DungeonMapWorld.getStateAt(x, y, z))
            if (id != null && id.equalsOneOf(5, 54, 146)) continue
            sb.append(id ?: 0)
        }
        return sb.toString().hashCode()
    }

    fun getHighestY(x: Int, z: Int): Int {
        var height = 0

        for (idx in 256 downTo 0) {
            val blockState = DungeonMapWorld.getStateAt(x, idx, z)
            val block = blockState.block
            if (blockState.isAir || block == Blocks.GOLD_BLOCK) continue

            height = idx
            break
        }

        return height
    }

    fun getRealCoord(pos: BlockPos, roomCenter: BlockPos, rotation: Int): BlockPos {
        val (cx, _, cz) = roomCenter.destructured
        return pos.rotate(rotation).add(cx, 0, cz)
    }

    fun getRelativeCoord(realPos: BlockPos, roomCorner: BlockPos, rotation: Int): BlockPos {
        val (cx, _, cz) = roomCorner.destructured
        val centeredPos = realPos.add(-cx, 0, -cz)
        return centeredPos.rotate(-rotation)
    }
}
