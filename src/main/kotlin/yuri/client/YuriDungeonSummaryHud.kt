package yuri.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import yuri.data.columns.dungeons.client.YuriDungeonListener
import yuri.data.columns.dungeons.client.enums.Blessing
import yuri.data.columns.dungeons.client.enums.Puzzle
import yuri.data.columns.dungeons.map.core.RoomState
import yuri.util.DungeonUtils

/**
 * Compact dungeon sidebar-style readout: puzzle states and active blessings (from [YuriDungeonListener]).
 */
object YuriDungeonSummaryHud {
    /** Use -1 to anchor from the right edge of the scaled window. */
    @JvmField
    var hudX: Int = -1

    @JvmField
    var hudY: Int = 36

    @JvmField
    var enabled: Boolean = true

    fun render(graphics: GuiGraphics) {
        if (!enabled || !DungeonUtils.inDungeons()) return
        val mc = Minecraft.getInstance()
        val font = mc.font
        val sw = mc.window.guiScaledWidth
        val left = if (hudX < 0) sw + hudX else hudX
        var y = hudY
        val puzzles = YuriDungeonListener.puzzles
        if (puzzles.isNotEmpty()) {
            val line = puzzles.joinToString(" ") { p ->
                val sym = when (p.state) {
                    RoomState.GREEN -> "✔"
                    RoomState.FAILED -> "✖"
                    RoomState.DISCOVERED, RoomState.CLEARED -> "◆"
                    RoomState.UNOPENED -> "?"
                    RoomState.UNDISCOVERED -> "·"
                }
                "${shortPuzzle(p)}$sym"
            }
            val clipped = if (line.length > 48) line.take(45) + "…" else line
            graphics.drawString(font, Component.literal(clipped), left, y, 0xFFCCDDFF.toInt(), false)
            y += font.lineHeight + 2
        }
        val bless = Blessing.entries.filter { it.current > 0 }
        if (bless.isNotEmpty()) {
            val bline = bless.joinToString(" ") { "${it.displayString.take(4)}${it.current}" }
            graphics.drawString(font, Component.literal(bline), left, y, 0xFFFFCC88.toInt(), false)
        }
    }

    private fun shortPuzzle(p: Puzzle): String =
        p.roomDataName.split(" ").firstOrNull()?.take(4) ?: p.name.take(4) // enum name fallback
}
