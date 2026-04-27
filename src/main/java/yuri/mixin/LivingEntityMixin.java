package yuri.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yuri.YuriData;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "getCurrentSwingDuration", at = @At("RETURN"), cancellable = true)
    private void yuri$adjustSwingDuration(CallbackInfoReturnable<Integer> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof LocalPlayer) || !YuriData.isAnimationsEnabled()) {
            return;
        }

        var settings = YuriData.ANIMATION_SETTINGS;
        if (settings.swingSpeed <= 0) {
            cir.setReturnValue(Integer.MAX_VALUE);
            return;
        }
        int targetDuration = toTargetSwingDuration(settings.swingSpeed);

        if (settings.ignoreHaste) {
            cir.setReturnValue(targetDuration);
            return;
        }

        int vanillaDuration = cir.getReturnValue();
        int adjusted = Math.max(1, Math.round(vanillaDuration * (targetDuration / 6.0F)));
        cir.setReturnValue(adjusted);
    }

    private static int toTargetSwingDuration(int speed) {
        int clamped = Math.max(1, Math.min(20, speed));
        return Math.max(2, Math.round(16.0F - (clamped - 1) * (14.0F / 19.0F)));
    }
}
