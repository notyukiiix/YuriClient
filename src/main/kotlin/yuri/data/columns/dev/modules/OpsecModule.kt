package yuri.data.columns.dev.modules

import net.minecraft.client.Minecraft
import yuri.YuriData

object OpsecModule {
    private const val TITLE = "Name change"

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    @JvmField
    var nameChange: String = ""

    @JvmStatic
    fun optionCount(): Int = 0

    @JvmStatic
    fun optionLabel(index: Int): String = ""

    @JvmStatic
    fun isEnabled(index: Int): Boolean = false

    @JvmStatic
    fun setEnabled(index: Int, enabled: Boolean) {}

    @JvmStatic
    fun toggle(index: Int) {}

    @JvmStatic
    fun getMaskedName(vanillaName: String): String {
        val custom = nameChange.trim()
        if (!module.enabled || custom.isEmpty()) {
            return vanillaName
        }
        return custom
    }

    @JvmStatic
    fun applyLocalNametag(client: Minecraft) {
        val player = client.player ?: return
        // Keep server-provided nametag content intact (rank/prefix/suffix), only force visibility.
        player.setCustomNameVisible(true)
    }
}
