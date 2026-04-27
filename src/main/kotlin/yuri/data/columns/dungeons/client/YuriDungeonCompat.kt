package yuri.data.columns.dungeons.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val yuriDungeonScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

fun String.stripSection(): String = replace(Regex("(?i)§[0-9A-FK-OR]"), "").trim()

fun String.romanOrInt(): Int =
    if (all { it.isDigit() }) toIntOrNull() ?: 0 else romanToDecimal()

fun String.romanToDecimal(): Int {
    if (isEmpty()) return 0
    val u = uppercase()
    var total = 0
    var i = 0
    val pairs = listOf("M" to 1000, "CM" to 900, "D" to 500, "CD" to 400, "C" to 100, "XC" to 90, "L" to 50, "XL" to 40, "X" to 10, "IX" to 9, "V" to 5, "IV" to 4, "I" to 1)
    while (i < u.length) {
        var matched = false
        for ((sym, v) in pairs) {
            if (u.startsWith(sym, i)) {
                total += v
                i += sym.length
                matched = true
                break
            }
        }
        if (!matched) {
            i++
        }
    }
    return total
}
