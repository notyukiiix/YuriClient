package yuri.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuri.data.columns.visual.modules.RenderOptimiserModule;

@Mixin(EffectsInInventory.class)
public abstract class EffectsInInventoryMixin {
    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void yuri$hideInventoryEffectSprites(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (RenderOptimiserModule.shouldHideEffectsHud()) {
            ci.cancel();
        }
    }
}
