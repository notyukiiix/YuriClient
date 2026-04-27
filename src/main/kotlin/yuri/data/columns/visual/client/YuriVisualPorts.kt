package yuri.data.columns.visual.client

/**
 * Stub registry for optional HUD / overlay features (reference copies under repo root).
 * Replace individual register() bodies as features are ported to Fabric APIs.
 */
object YuriVisualPorts {
    @JvmStatic
    fun registerAll() {
        Animations.register()
        BlessingDisplay.register()
        ClockDisplay.register()
        CpsDisplay.register()
        DamageSplash.register()
        DarkMode.register()
        FpsDisplay.register()
        FreezeDisplay.register()
        LifelineHud.register()
        MaskTimers.register()
        PetDisplay.register()
        PlayerHud.register()
        RenderOptimizer.register()
        RevertAxes.register()
        RunSplits.register()
        ScoreboardOverlay.register()
        SpringBoots.register()
        TpsDisplay.register()
        WarpCooldown.register()
    }
}

private object Animations { fun register() {} }
private object BlessingDisplay { fun register() {} }
private object ClockDisplay { fun register() {} }
private object CpsDisplay { fun register() {} }
private object DamageSplash { fun register() {} }
private object DarkMode { fun register() {} }
private object FpsDisplay { fun register() {} }
private object FreezeDisplay { fun register() {} }
private object LifelineHud { fun register() {} }
private object MaskTimers { fun register() {} }
private object PetDisplay { fun register() {} }
private object PlayerHud { fun register() {} }
private object RenderOptimizer { fun register() {} }
private object RevertAxes { fun register() {} }
private object RunSplits { fun register() {} }
private object ScoreboardOverlay { fun register() {} }
private object SpringBoots { fun register() {} }
private object TpsDisplay { fun register() {} }
private object WarpCooldown { fun register() {} }
