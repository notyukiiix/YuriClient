package yuri.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yuri.YuriData;
import yuri.data.columns.dev.modules.OpsecModule;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "getCurrentItemAttackStrengthDelay", at = @At("RETURN"), cancellable = true)
    private void yuri$adjustAttackDelay(CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof LocalPlayer) || !YuriData.isAnimationsEnabled()) {
            return;
        }

        int speed = Math.max(0, Math.min(20, YuriData.ANIMATION_SETTINGS.swingSpeed));
        if (speed == 0) {
            return;
        }
        float vanillaDelay = cir.getReturnValue();
        float adjustedDelay = Math.max(1.0F, vanillaDelay * (10.0F / speed));
        cir.setReturnValue(adjustedDelay);
    }

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void yuri$maskLocalName(CallbackInfoReturnable<Component> cir) {
        if (!((Object) this instanceof LocalPlayer)) {
            return;
        }
        Component vanilla = cir.getReturnValue();
        if (vanilla == null) {
            return;
        }
        String masked = OpsecModule.getMaskedName(vanilla.getString());
        if (!masked.equals(vanilla.getString())) {
            cir.setReturnValue(Component.literal(masked));
        }
    }
}
