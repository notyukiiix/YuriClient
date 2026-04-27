package yuri.data.columns.general.modules

import yuri.YuriData

object ChatModule {
    private const val TITLE = "Chat"
    private const val OPTION_STACK_SIMILAR_MESSAGES = 0
    private const val OPTION_CLICK_COPY = 1
    private const val OPTION_SHORT_CMDS = 2

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    private val labels: Array<String> = arrayOf(
        "Stack Similar Messages",
        "Click + Copy",
        "Short Cmds"
    )

    private val toggles: BooleanArray = booleanArrayOf(
        true,
        false,
        true
    )

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
    fun shouldStackSimilarMessages(): Boolean = module.enabled && toggles[OPTION_STACK_SIMILAR_MESSAGES]

    @JvmStatic
    fun shouldClickToCopy(): Boolean = module.enabled && toggles[OPTION_CLICK_COPY]

    @JvmStatic
    fun shouldShortCommands(): Boolean = module.enabled && toggles[OPTION_SHORT_CMDS]
}
