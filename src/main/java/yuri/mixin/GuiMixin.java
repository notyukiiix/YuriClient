package yuri.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.world.scores.Objective;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuri.data.columns.visual.modules.RenderOptimiserModule;
import yuri.data.columns.dev.modules.CustomScoreboardModule;

@Mixin(net.minecraft.client.gui.Gui.class)
public abstract class GuiMixin {
    @Inject(method = "displayScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void yuri$hideVanillaSidebar(GuiGraphics graphics, Objective objective, CallbackInfo ci) {
        if (CustomScoreboardModule.shouldHideVanillaSidebar()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void yuri$hideActionBarHud(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (RenderOptimiserModule.shouldHideActionBarHud()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void yuri$hideEffectsHud(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (RenderOptimiserModule.shouldHideEffectsHud()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderCameraOverlays", at = @At("HEAD"), cancellable = true)
    private void yuri$hideCameraEffectOverlays(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (RenderOptimiserModule.shouldHideEffectsHud()) {
            ci.cancel();
        }
    }
}
