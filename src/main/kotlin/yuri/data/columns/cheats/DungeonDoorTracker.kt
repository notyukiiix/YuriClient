package yuri.data.columns.cheats

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import yuri.data.columns.cheats.modules.DoorEspModule
import yuri.data.columns.cheats.modules.TranslucentDoorModule
import yuri.util.DungeonUtils

/**
 * Tracks iron-bar dungeon door voxels near the player for translucent doors and/or door ESP.
 */
object DungeonDoorTracker {
    private const val SCAN_INTERVAL_TICKS: Int = 20
    private const val HORIZONTAL_RADIUS: Int = 28
    private const val VERTICAL_HALF_RANGE: Int = 42

    private val doorBlockPackedPositions: LongOpenHashSet = LongOpenHashSet()
    private var ticksUntilScan: Int = 0
    private var eventsRegistered: Boolean = false

    @JvmStatic
    fun isDoorPackedPosition(packed: Long): Boolean = doorBlockPackedPositions.contains(packed)

    @JvmStatic
    fun isDoor(pos: BlockPos): Boolean = doorBlockPackedPositions.contains(pos.asLong())

    @JvmStatic
    fun forEachDoorPacked(consumer: java.util.function.LongConsumer) {
        doorBlockPackedPositions.forEach(consumer)
    }

    @JvmStatic
    fun registerEvents() {
        if (eventsRegistered) {
            return
        }
        eventsRegistered = true
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            val needScan =
                DungeonUtils.inDungeons() &&
                    (TranslucentDoorModule.module.enabled || DoorEspModule.module.enabled)
            if (!needScan) {
                doorBlockPackedPositions.clear()
                ticksUntilScan = 0
                return@register
            }
            val player = client.player ?: return@register
            val level = client.level ?: return@register
            ticksUntilScan--
            if (ticksUntilScan > 0) {
                return@register
            }
            ticksUntilScan = SCAN_INTERVAL_TICKS
            rescanAround(level, player.blockPosition())
        }
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { _, _ ->
            doorBlockPackedPositions.clear()
            ticksUntilScan = 0
        }
    }

    private fun rescanAround(level: Level, center: BlockPos) {
        doorBlockPackedPositions.clear()
        val mutable = BlockPos.MutableBlockPos()
        val minX = center.x - HORIZONTAL_RADIUS
        val maxX = center.x + HORIZONTAL_RADIUS
        val minZ = center.z - HORIZONTAL_RADIUS
        val maxZ = center.z + HORIZONTAL_RADIUS
        val yMin = (center.y - VERTICAL_HALF_RANGE).coerceAtLeast(4)
        val yMax = (center.y + VERTICAL_HALF_RANGE).coerceAtMost(220)
        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                for (y in yMin..yMax) {
                    mutable.set(x, y, z)
                    if (level.getBlockState(mutable).`is`(Blocks.IRON_BARS)) {
                        doorBlockPackedPositions.add(mutable.asLong())
                    }
                }
            }
        }
    }
}
