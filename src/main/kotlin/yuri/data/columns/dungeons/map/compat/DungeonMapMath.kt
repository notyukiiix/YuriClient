package yuri.data.columns.dungeons.map.compat

fun Int.equalsOneOf(vararg values: Int): Boolean = values.any { it == this }

fun <T> T.equalsOneOf(vararg values: T): Boolean = values.any { it == this }

fun String.equalsOneOf(vararg values: String): Boolean =
    values.any { this.equals(it, ignoreCase = true) }

fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

fun interpolateYaw(start: Float, end: Float, progress: Float): Float {
    var delta = (end - start) % 360f
    if (delta > 180f) delta -= 360f
    if (delta < -180f) delta += 360f
    return (start + delta * progress + 360f * 3) % 360f
}
