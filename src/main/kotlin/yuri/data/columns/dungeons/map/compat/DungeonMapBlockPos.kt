package yuri.data.columns.dungeons.map.compat

import net.minecraft.core.BlockPos

fun BlockPos.add(dx: Int, dy: Int, dz: Int): BlockPos = offset(dx, dy, dz)

val BlockPos.destructured: Triple<Int, Int, Int>
    get() = Triple(x, y, z)

fun BlockPos.rotate(degree: Int): BlockPos {
    return when ((degree % 360 + 360) % 360) {
        0 -> BlockPos(x, y, z)
        90 -> BlockPos(z, y, -x)
        180 -> BlockPos(-x, y, -z)
        270 -> BlockPos(-z, y, x)
        else -> BlockPos(x, y, z)
    }
}
