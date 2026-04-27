package yuri.data.columns.dungeons.map.handlers

import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import yuri.data.columns.dungeons.map.DungeonInfo
import yuri.data.columns.dungeons.client.YuriDungeonListener
import yuri.data.columns.dungeons.map.compat.DungeonMapSync
import yuri.data.columns.dungeons.map.compat.DungeonMapWorld
import yuri.data.columns.dungeons.map.core.Door
import yuri.data.columns.dungeons.map.core.DoorType
import yuri.data.columns.dungeons.map.core.Room
import yuri.data.columns.dungeons.map.core.RoomType
import yuri.data.columns.dungeons.map.core.Tile
import yuri.data.columns.dungeons.map.core.Unknown
import yuri.data.columns.dungeons.map.core.UniqueRoom
import yuri.data.columns.dungeons.map.utils.ScanUtils

object DungeonScanner {
    const val startX = -185
    const val startZ = -185

    const val roomSize = 32
    const val halfRoomSize = 15

    val clayBlocksCorners = listOf(
        Pair(-halfRoomSize, -halfRoomSize),
        Pair(halfRoomSize, -halfRoomSize),
        Pair(halfRoomSize, halfRoomSize),
        Pair(-halfRoomSize, halfRoomSize)
    )

    private var lastScanTime = 0L
    private var isScanning = false
    var hasScanned = false

    val shouldScan: Boolean
        get() = !isScanning && !hasScanned && System.currentTimeMillis() - lastScanTime >= 250

    fun scan() {
        isScanning = true
        var allChunksLoaded = true

        for (x in 0..10) {
            for (z in 0..10) {
                val wX = startX + x * (roomSize shr 1)
                val wZ = startZ + z * (roomSize shr 1)

                if (!DungeonMapWorld.isChunkLoaded(wX, wZ)) {
                    allChunksLoaded = false
                    continue
                }

                val roofHeight = ScanUtils.getHighestY(wX, wZ)
                if (roofHeight <= 0) continue

                val roomInGrid = DungeonInfo.dungeonList[x + z * 11]
                if (roomInGrid !is Unknown && (roomInGrid as? Room)?.data?.name != "Unknown") continue

                scanRoom(wX, wZ, z, x, roofHeight)?.let { room ->
                    DungeonInfo.dungeonList[z * 11 + x] = room
                    DungeonPathFinder.clearCache()
                    if (YuriDungeonListener.dungeonTeammatesNoSelf.isEmpty()) return@let

                    if (room is Room && room.data.name != "Unknown") {
                        DungeonMapSync.send(Any())
                    }

                    if (room is Door) {
                        DungeonMapSync.send(Any())
                    }
                }
            }
        }

        if (allChunksLoaded) {
            DungeonInfo.roomCount = DungeonInfo.dungeonList.count { it is Room && !it.isSeparator }
            hasScanned = true
        }

        lastScanTime = System.currentTimeMillis()
        isScanning = false
    }

    fun findMimicRoom(): UniqueRoom? {
        DungeonMapWorld.getBlockEntityList()
            .filter { DungeonMapWorld.getStateAt(it.blockPos).block === Blocks.TRAPPED_CHEST }
            .groupingBy { ScanUtils.getRoomFromPos(Vec3(it.blockPos.x.toDouble(), it.blockPos.y.toDouble(), it.blockPos.z.toDouble()))?.data?.name }
            .eachCount()
            .forEach { (roomName, trappedCount) ->
                if (roomName == null) return@forEach

                val roomEntry = DungeonInfo.uniqueRooms.entries.find {
                    it.key == roomName && it.value.data.trappedChests < trappedCount
                }

                if (roomEntry != null) return roomEntry.value
            }

        return null
    }

    private fun scanRoom(x: Int, z: Int, row: Int, column: Int, roofHeight: Int): Tile? {
        val rowEven = row and 1 == 0
        val columnEven = column and 1 == 0

        return when {
            rowEven && columnEven -> {
                val roomCore = ScanUtils.getCore(x, z)
                Room(x, z, ScanUtils.getRoomData(roomCore) ?: return null).apply {
                    core = roomCore
                    addToUnique(row, column)
                }
            }

            !rowEven && !columnEven -> {
                DungeonInfo.dungeonList[column - 1 + (row - 1) * 11].let {
                    if (it is Room) {
                        Room(x, z, it.data).apply {
                            isSeparator = true
                            addToUnique(row, column)
                        }
                    } else {
                        null
                    }
                }
            }

            roofHeight == 74 || roofHeight == 82 || roofHeight == 73 || roofHeight == 81 -> {
                Door(
                    x, z,
                    type = when (DungeonMapWorld.getBlockAt(x, 69, z)) {
                        Blocks.COAL_BLOCK -> {
                            DungeonInfo.witherDoors++
                            DoorType.WITHER
                        }

                        Blocks.INFESTED_CHISELED_STONE_BRICKS -> DoorType.ENTRANCE
                        Blocks.RED_TERRACOTTA -> DoorType.BLOOD
                        else -> DoorType.NORMAL
                    }
                )
            }

            else -> DungeonInfo.dungeonList[if (rowEven) row * 11 + column - 1 else (row - 1) * 11 + column].let {
                when {
                    it !is Room -> null
                    it.data.type == RoomType.ENTRANCE -> Door(x, z, DoorType.ENTRANCE)
                    else -> Room(x, z, it.data).apply {
                        isSeparator = true
                        addToUnique(row, column)
                    }
                }
            }
        }
    }
}
