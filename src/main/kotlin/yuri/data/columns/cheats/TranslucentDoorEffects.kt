package yuri.data.columns.cheats

import net.minecraft.core.BlockPos
import yuri.data.columns.cheats.modules.TranslucentDoorModule

/**
 * Client-side translucency for tracked dungeon door blocks (mirrors the intent of legitcat
 * [TranslucentBlocks] without solo-only gating).
 */
object TranslucentDoorEffects {
    private val putQuadBlockPos: ThreadLocal<BlockPos?> = ThreadLocal.withInitial { null }

    @JvmStatic
    fun beginPutQuadBlockPos(pos: BlockPos) {
        putQuadBlockPos.set(pos)
    }

    @JvmStatic
    fun endPutQuadBlockPos() {
        putQuadBlockPos.remove()
    }

    @JvmStatic
    fun putQuadBlockPos(): BlockPos? = putQuadBlockPos.get()

    @JvmStatic
    fun getBlockAlpha(pos: BlockPos): Int {
        if (!TranslucentDoorModule.isActive()) {
            return 255
        }
        return if (DungeonDoorTracker.isDoor(pos)) {
            TranslucentDoorModule.doorsAlpha()
        } else {
            255
        }
    }

    @JvmStatic
    fun shouldRenderFace(original: Boolean, pos: BlockPos, otherPos: BlockPos): Boolean {
        val blockAlpha = getBlockAlpha(pos)
        if (blockAlpha != 255) {
            return original
        }
        val otherAlpha = getBlockAlpha(otherPos)
        return if (otherAlpha != 255) {
            true
        } else {
            original
        }
    }

    @JvmStatic
    fun doorTintMultiplier(): Float {
        if (!TranslucentDoorModule.isActive()) {
            return 1.0f
        }
        return TranslucentDoorModule.doorsAlpha() / 255.0f
    }

    @JvmStatic
    fun shouldTintDoorBlock(pos: BlockPos): Boolean =
        TranslucentDoorModule.isActive() && DungeonDoorTracker.isDoor(pos)
}
