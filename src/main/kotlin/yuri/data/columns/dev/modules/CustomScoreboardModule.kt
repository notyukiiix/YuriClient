package yuri.data.columns.dev.modules

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import yuri.YuriData
import yuri.YuriHudEditLayout
import yuri.YuriTheme
import yuri.client.OpsecTextMasker
import kotlin.math.max
import kotlin.math.min

object CustomScoreboardModule {
    private const val TITLE = "Custom Scoreboard"
    private const val OPTION_HIDE_SERVER_ID = 0
    private val SERVER_ID_REGEX = Regex("(?i)\\bm\\d{1,3}[a-z]{2}\\b")
    private val SERVER_ID_END_REGEX = Regex("(?i)\\s+m\\d{1,3}[a-z]{1,3}\\s*$")

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    private val labels: Array<String> = arrayOf("Hide Server ID")
    private val toggles: BooleanArray = booleanArrayOf(true)

    @JvmField
    var hudX: Int = 8

    @JvmField
    var hudY: Int = 60

    private data class Cache(
        val title: Component,
        val lines: List<Component>,
        val width: Int,
        val height: Int
    )

    private var cached: Cache? = null
    private var dragging: Boolean = false
    private var dragOffsetX: Int = 0
    private var dragOffsetY: Int = 0

    @JvmStatic
    fun optionCount(): Int = labels.size

    @JvmStatic
    fun optionLabel(index: Int): String = labels[index]

    @JvmStatic
    fun isEnabled(index: Int): Boolean = toggles[index]

    @JvmStatic
    fun setEnabled(index: Int, enabled: Boolean) {
        toggles[index] = enabled
    }

    @JvmStatic
    fun toggle(index: Int) {
        toggles[index] = !toggles[index]
    }

    @JvmStatic
    fun shouldRender(): Boolean = module.enabled

    @JvmStatic
    fun shouldHideVanillaSidebar(): Boolean = module.enabled

    @JvmStatic
    fun render(graphics: GuiGraphics, useExample: Boolean) {
        if (!shouldRender()) {
            return
        }
        val mc = Minecraft.getInstance() ?: return
        val font = mc.font
        val data = buildCache(useExample)
        if (data.lines.isEmpty()) {
            return
        }
        val x = hudX
        val y = hudY
        val right = x + data.width
        val bottom = y + data.height
        graphics.fill(x + 2, y + 2, right + 2, bottom + 2, YuriTheme.shadow())
        graphics.fill(x, y, right, bottom, 0xCC0F1117.toInt())
        graphics.fill(x, y, right, y + 2, YuriTheme.accent())
        graphics.fill(x, y, x + 1, bottom, YuriTheme.columnBorder())
        graphics.fill(right - 1, y, right, bottom, YuriTheme.columnBorder())
        graphics.fill(x, bottom - 1, right, bottom, YuriTheme.columnBorder())

        val titleY = y + 6
        graphics.drawCenteredString(font, data.title, x + data.width / 2, titleY, 0xFFF6F7FB.toInt())

        var lineY = titleY + font.lineHeight + 4
        for (line in data.lines) {
            graphics.drawString(font, line, x + 8, lineY, 0xFFE5EAF2.toInt(), false)
            lineY += font.lineHeight + 2
        }
    }

    @JvmStatic
    fun renderHudEditor(graphics: GuiGraphics) {
        render(graphics, true)
    }

    @JvmStatic
    fun containsHudPoint(mx: Double, my: Double): Boolean {
        val data = buildCache(true)
        val left = hudX
        val top = hudY
        return mx >= left && mx < left + data.width && my >= top && my < top + data.height
    }

    @JvmStatic
    fun editorBeginDrag(mx: Double, my: Double) {
        if (!containsHudPoint(mx, my)) {
            return
        }
        dragging = true
        dragOffsetX = (mx - hudX).toInt()
        dragOffsetY = (my - hudY).toInt()
    }

    @JvmStatic
    fun editorDragging(): Boolean = dragging

    @JvmStatic
    fun editorEndDrag() {
        dragging = false
        if (YuriHudEditLayout.isGridSnapEnabled()) {
            hudX = YuriHudEditLayout.snapToGrid(hudX)
            hudY = YuriHudEditLayout.snapToGrid(hudY)
        }
    }

    @JvmStatic
    fun editorDragTo(mx: Double, my: Double, screenWidth: Int, screenHeight: Int) {
        if (!dragging) {
            return
        }
        val data = buildCache(true)
        var nx = (mx - dragOffsetX).toInt()
        var ny = (my - dragOffsetY).toInt()
        nx = min(max(0, nx), max(0, screenWidth - data.width))
        ny = min(max(0, ny), max(0, screenHeight - data.height))
        if (YuriHudEditLayout.isGridSnapEnabled()) {
            nx = YuriHudEditLayout.snapToGrid(nx)
            ny = YuriHudEditLayout.snapToGrid(ny)
        }
        hudX = nx
        hudY = ny
    }

    private fun buildCache(useExample: Boolean): Cache {
        val mc = Minecraft.getInstance() ?: return Cache(Component.empty(), emptyList(), 0, 0)
        val level = mc.level
        if (!useExample && level == null) {
            return Cache(Component.empty(), emptyList(), 0, 0)
        }
        val title: Component
        val lines: MutableList<Component> = mutableListOf()
        if (level != null) {
            val scoreboard = level.scoreboard
            val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR)
            if (objective != null) {
                title = OpsecTextMasker.maskComponent(objective.displayName) ?: objective.displayName
                fillLinesFromObjective(scoreboard, objective, lines)
            } else if (!useExample) {
                return Cache(Component.empty(), emptyList(), 0, 0)
            } else {
                title = Component.literal("SKYBLOCK")
            }
        } else {
            title = Component.literal("SKYBLOCK")
        }
        if (lines.isEmpty()) {
            lines.addAll(exampleLines())
        }
        val normalizedTitle = if (title.string.isBlank()) Component.literal("Scoreboard") else title
        val font = mc.font
        var width = font.width(normalizedTitle)
        for (line in lines) {
            width = max(width, font.width(line))
        }
        val paddedWidth = width + 16
        val paddedHeight = 6 + font.lineHeight + 4 + lines.size * (font.lineHeight + 2) + 6
        val next = Cache(normalizedTitle, lines, paddedWidth, paddedHeight)
        cached = next
        return next
    }

    private fun fillLinesFromObjective(scoreboard: Scoreboard, objective: Objective, out: MutableList<Component>) {
        val scores = scoreboard.listPlayerScores(objective)
            .sortedByDescending { it.value() }
            .take(15)
        for (score in scores) {
            val owner = score.owner()
            val team = scoreboard.getPlayersTeam(owner)
            var line: MutableComponent = PlayerTeam.formatNameForTeam(team, Component.literal(owner))
            if (toggles[OPTION_HIDE_SERVER_ID]) {
                line = stripServerId(line)
            }
            out.add(OpsecTextMasker.maskComponent(line) ?: line)
        }
    }

    private fun stripServerId(input: String): String {
        // Some Hypixel scoreboards append the server id as the final token on the date line (e.g. " ... m151A").
        var line = SERVER_ID_END_REGEX.replace(input, "")
        line = SERVER_ID_REGEX.replace(line, "")
        return line.replace(Regex("\\s{2,}"), " ").trim()
    }

    private fun stripServerId(line: MutableComponent): MutableComponent {
        val raw = line.string
        val stripped = stripServerId(raw)
        if (stripped == raw) {
            return line
        }
        return Component.literal(stripped).withStyle(line.style)
    }

    private fun exampleLines(): List<Component> = listOf(
        Component.literal("Date: Early Spring 12th"),
        Component.literal("Profile: Banana").withStyle(net.minecraft.ChatFormatting.RED),
        Component.literal("Purse: 12,345"),
        Component.literal("Bits: 128"),
        Component.literal("www.hypixel.net")
    )
}
