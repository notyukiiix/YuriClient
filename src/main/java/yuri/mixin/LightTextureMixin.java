package yuri.mixin;

import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yuri.data.columns.visual.modules.FullbrightModule;

@Mixin(LightTexture.class)
public class LightTextureMixin {

    @Inject(method = "getBrightness(FI)F", at = @At("HEAD"), cancellable = true)
    private static void yuri$fullbrightBrightness(float dimensionAmbient, int packedLight, CallbackInfoReturnable<Float> cir) {
        if (FullbrightModule.isEnabled()) {
            cir.setReturnValue(1.0F);
            cir.cancel();
        }
    }
}
