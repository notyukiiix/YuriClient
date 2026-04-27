package yuri.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yuri.client.OpsecTextMasker;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "getNameTag", at = @At("RETURN"), cancellable = true)
    private void yuri$maskLocalNameTag(Entity entity, CallbackInfoReturnable<Component> cir) {
        Component vanilla = cir.getReturnValue();
        if (vanilla == null) {
            return;
        }
        String fallbackName = entity.getName() != null ? entity.getName().getString() : null;
        Component masked = OpsecTextMasker.maskComponentForPlayer(vanilla, entity.getUUID(), fallbackName);
        if (masked != vanilla) {
            cir.setReturnValue(masked);
        }
    }
}
