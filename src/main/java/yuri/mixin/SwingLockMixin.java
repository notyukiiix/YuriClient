package yuri.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuri.YuriData;

@Mixin(LivingEntity.class)
public abstract class SwingLockMixin {
    @Shadow
    private boolean swinging;

    @Shadow
    private int swingTime;

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    private void yuri$preventRestartingSwing(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
        if (!((Object) this instanceof LocalPlayer) || !YuriData.isAnimationsEnabled()) {
            return;
        }

        if (swinging && swingTime > 0) {
            ci.cancel();
        }
    }
}
