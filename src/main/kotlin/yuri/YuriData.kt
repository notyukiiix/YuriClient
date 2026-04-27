package yuri

import yuri.data.columns.cheats.CheatsColumn
import yuri.data.columns.cosmetics.CosmeticsColumn
import yuri.data.columns.cosmetics.modules.PlayerSizeModule
import yuri.data.columns.dev.DevColumn
import yuri.data.columns.dev.modules.ClickGuiModule
import yuri.data.columns.dungeons.DungeonsColumn
import yuri.data.columns.floor7.Floor7Column
import yuri.data.columns.general.GeneralColumn
import yuri.data.columns.general.modules.SillySpeakModule
import yuri.data.columns.visual.VisualColumn
import yuri.data.columns.visual.modules.AnimationsModule

object YuriData {
    @JvmField
    val COLUMNS: Array<Column> = arrayOf(
        GeneralColumn.createColumn(),
        DungeonsColumn.createColumn(),
        Floor7Column.createColumn(),
        VisualColumn.createColumn(),
        CosmeticsColumn.createColumn(),
        DevColumn.createColumn(),
        CheatsColumn.createColumn()
    )

    @JvmField
    val ANIMATION_SETTINGS = AnimationsModule.settings

    @JvmField
    val PLAYER_SIZE_SETTINGS = PlayerSizeModule.settings

    class Column internal constructor(
        @JvmField val title: String,
        x: Int,
        y: Int,
        @JvmField vararg val modules: Module
    ) {
        @JvmField val defaultX: Int = x
        @JvmField val defaultY: Int = y
        @JvmField var x: Int = x
        @JvmField var y: Int = y
        @JvmField var collapsed: Boolean = false

        fun resetPosition() {
            x = defaultX
            y = defaultY
            collapsed = false
        }
    }

    class Module internal constructor(@JvmField val title: String) {
        @JvmField var enabled: Boolean = false
    }

    @JvmStatic
    fun isModuleEnabled(columnTitle: String, moduleTitle: String): Boolean {
        val module = findModule(columnTitle, moduleTitle)
        return module?.enabled == true
    }

    @JvmStatic
    fun isAnimationsEnabled(): Boolean = AnimationsModule.module.enabled

    @JvmStatic
    fun isClickGuiEnabled(): Boolean = ClickGuiModule.module.enabled

    @JvmStatic
    fun isPlayerSizeEnabled(): Boolean = PlayerSizeModule.module.enabled

    @JvmStatic
    fun isSillySpeakEnabled(): Boolean = SillySpeakModule.module.enabled

    private fun findModule(columnTitle: String, moduleTitle: String): Module? {
        for (column in COLUMNS) {
            if (!column.title.equals(columnTitle, ignoreCase = true)) {
                continue
            }
            for (module in column.modules) {
                if (module.title.equals(moduleTitle, ignoreCase = true)) {
                    return module
                }
            }
        }
        return null
    }
}
