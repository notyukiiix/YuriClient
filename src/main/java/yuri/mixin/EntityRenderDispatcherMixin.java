package yuri.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yuri.data.columns.cheats.modules.MobEspModule;
import yuri.data.columns.visual.modules.RenderOptimiserModule;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void yuri$renderOptimiserSkipRender(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (MobEspModule.shouldRevealHiddenMob(entity)) {
            entity.setInvisible(false);
        }
        if (RenderOptimiserModule.shouldHideDeathAnimationRender(entity)) {
            cir.setReturnValue(false);
            return;
        }
        if (RenderOptimiserModule.shouldHideDyingMobArmorStand(entity)) {
            cir.setReturnValue(false);
        }
    }
}
