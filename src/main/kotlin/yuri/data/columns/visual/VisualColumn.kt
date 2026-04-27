package yuri.data.columns.visual

import yuri.YuriData
import yuri.data.columns.visual.modules.AnimationsModule
import yuri.data.columns.visual.modules.FullbrightModule
import yuri.data.columns.visual.modules.ImageHudModule
import yuri.data.columns.visual.modules.YuriHudMetricsModule
import yuri.data.columns.visual.modules.YuriVisualBundleModule
import yuri.data.columns.visual.modules.RenderOptimiserModule

object VisualColumn {
    @JvmStatic
    fun createColumn(): YuriData.Column = YuriData.Column(
        "Visual",
        470,
        42,
        AnimationsModule.module,
        RenderOptimiserModule.module,
        ImageHudModule.module,
        FullbrightModule.module,
        YuriHudMetricsModule.module,
        YuriVisualBundleModule.module
    )
}
