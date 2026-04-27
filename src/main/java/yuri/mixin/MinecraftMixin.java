package yuri.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuri.YuriData;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    private int rightClickDelay;

    @Inject(method = "startUseItem", at = @At("RETURN"))
    private void yuri$adjustInteractionDelay(CallbackInfo ci) {
        if (!YuriData.isAnimationsEnabled()) {
            return;
        }

        int speed = Math.max(0, Math.min(20, YuriData.ANIMATION_SETTINGS.swingSpeed));
        rightClickDelay = Math.max(0, Math.round((20 - speed) * (4.0F / 19.0F)));
    }
}
