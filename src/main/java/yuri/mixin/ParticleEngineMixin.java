package yuri.mixin;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yuri.data.columns.visual.modules.RenderOptimiserModule;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void yuri$hideExplosionParticles(ParticleOptions parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir) {
        if (RenderOptimiserModule.shouldCancelExplosionParticle(parameters)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(
        method = "createTrackingEmitter(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/particles/ParticleOptions;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void yuri$hideExplosionTrackingEmitter(Entity entity, ParticleOptions particleOptions, CallbackInfo ci) {
        if (RenderOptimiserModule.shouldCancelExplosionParticle(particleOptions)) {
            ci.cancel();
        }
    }

    @Inject(
        method = "createTrackingEmitter(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/particles/ParticleOptions;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void yuri$hideExplosionTrackingEmitterWithLifetime(Entity entity, ParticleOptions particleOptions, int lifetime, CallbackInfo ci) {
        if (RenderOptimiserModule.shouldCancelExplosionParticle(particleOptions)) {
            ci.cancel();
        }
    }
}
