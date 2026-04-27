package yuri.data.columns.dungeons.map.compat

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk

object DungeonMapWorld {
    private fun level() = Minecraft.getInstance().level

    @JvmStatic
    fun isChunkLoaded(blockX: Int, blockZ: Int): Boolean {
        val lvl = level() ?: return false
        return lvl.hasChunk(blockX shr 4, blockZ shr 4)
    }

    @JvmStatic
    fun getStateAt(pos: BlockPos): BlockState {
        val lvl = level() ?: return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
        return lvl.getBlockState(pos)
    }

    @JvmStatic
    fun getStateAt(x: Int, y: Int, z: Int): BlockState = getStateAt(BlockPos(x, y, z))

    @JvmStatic
    fun getBlockAt(x: Int, y: Int, z: Int) = getStateAt(x, y, z).block

    /** Block entities near the dungeon grid (client); used for mimic detection. */
    @JvmStatic
    fun getBlockEntityList(): List<BlockEntity> {
        val lvl = level() ?: return emptyList()
        val out = ArrayList<BlockEntity>(512)
        val baseCx = Mth.floorDiv(-185, 16)
        val baseCz = Mth.floorDiv(-185, 16)
        val r = 14
        for (dx in -r..r) {
            for (dz in -r..r) {
                val cx = baseCx + dx
                val cz = baseCz + dz
                if (!lvl.hasChunk(cx, cz)) {
                    continue
                }
                val chunk = lvl.getChunk(cx, cz) as? LevelChunk ?: continue
                out.addAll(chunk.blockEntities.values)
            }
        }
        return out
    }
}
