package yuri

import kotlin.math.max

/**
 * Shared slot above the Dev column ("Edit Hud" on the main GUI, "Grid Toggle" on the HUD editor).
 * Also holds grid-snap state for future HUD elements.
 */
object YuriHudEditLayout {
    const val SLOT_WIDTH: Int = 128
    const val SLOT_HEIGHT: Int = 26
    private const val GAP_ABOVE_DEV: Int = 4
    private const val MIN_SCREEN_Y: Int = 10

    /** Spacing between grid lines when grid snap is enabled (HUD editor preview). */
    const val GRID_CELL_PX: Int = 8

    private var gridSnapEnabled: Boolean = false

    @JvmStatic
    fun isGridSnapEnabled(): Boolean = gridSnapEnabled

    @JvmStatic
    fun setGridSnapEnabled(value: Boolean) {
        gridSnapEnabled = value
    }

    @JvmStatic
    fun toggleGridSnap() {
        gridSnapEnabled = !gridSnapEnabled
    }

    @JvmStatic
    fun devColumn(): YuriData.Column? =
        YuriData.COLUMNS.firstOrNull { it.title == "Dev" }

    /**
     * @return `[left, top, width, height]` in screen coordinates, or null if there is no Dev column.
     */
    @JvmStatic
    fun slotLeftTopWidthHeight(): IntArray? {
        val dev = devColumn() ?: return null
        val top = max(MIN_SCREEN_Y, dev.y - GAP_ABOVE_DEV - SLOT_HEIGHT)
        return intArrayOf(dev.x, top, SLOT_WIDTH, SLOT_HEIGHT)
    }

    /**
     * Snap a coordinate to the grid when grid snap is enabled; no-op otherwise.
     */
    @JvmStatic
    fun snapToGrid(value: Int): Int {
        if (!gridSnapEnabled) {
            return value
        }
        val c = GRID_CELL_PX
        return ((value + c / 2) / c) * c
    }
}
