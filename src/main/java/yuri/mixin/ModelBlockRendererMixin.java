package yuri.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yuri.data.columns.cheats.TranslucentDoorEffects;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {

    @Inject(
        method = "putQuadData(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/minecraft/client/renderer/block/ModelBlockRenderer$CommonRenderStorage;I)V",
        at = @At("HEAD")
    )
    private void yuri$capturePutQuadPos(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        VertexConsumer consumer,
        PoseStack.Pose pose,
        BakedQuad quad,
        ModelBlockRenderer.CommonRenderStorage renderStorage,
        int defaultColor,
        CallbackInfo ci
    ) {
        TranslucentDoorEffects.beginPutQuadBlockPos(pos);
    }

    @Inject(
        method = "putQuadData(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/minecraft/client/renderer/block/ModelBlockRenderer$CommonRenderStorage;I)V",
        at = @At("TAIL")
    )
    private void yuri$clearPutQuadPos(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        VertexConsumer consumer,
        PoseStack.Pose pose,
        BakedQuad quad,
        ModelBlockRenderer.CommonRenderStorage renderStorage,
        int defaultColor,
        CallbackInfo ci
    ) {
        TranslucentDoorEffects.endPutQuadBlockPos();
    }

    @Inject(
        method = "shouldRenderFace(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;ZLnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void yuri$translucentDoorFaces(
        BlockAndTintGetter level,
        BlockState state,
        boolean original,
        Direction facing,
        BlockPos pos,
        CallbackInfoReturnable<Boolean> cir
    ) {
        BlockPos other = pos.relative(facing);
        int selfAlpha = TranslucentDoorEffects.getBlockAlpha(pos);
        if (selfAlpha != 255) {
            cir.setReturnValue(original);
            cir.cancel();
            return;
        }
        int otherAlpha = TranslucentDoorEffects.getBlockAlpha(other);
        if (otherAlpha != 255) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @WrapOperation(
        method = "putQuadData(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/minecraft/client/renderer/block/ModelBlockRenderer$CommonRenderStorage;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFFF[IIZ)V"
        )
    )
    private void yuri$tintDoorPutBulk(
        VertexConsumer consumer,
        PoseStack.Pose pose,
        BakedQuad quad,
        float[] brightness,
        float red,
        float green,
        float blue,
        float alpha,
        int[] lightmap,
        int packed,
        boolean shade,
        Operation<Void> original
    ) {
        float m = yuri$doorVertexTintMultiplier();
        original.call(
            consumer,
            pose,
            quad,
            brightness,
            red * m,
            green * m,
            blue * m,
            alpha * m,
            lightmap,
            packed,
            shade
        );
    }

    /**
     * Second {@code putBulkData} overload (flat / non-array path) — vanilla uses it for many solid-model quads.
     * Without wrapping this, chiseled stone and similar door fills stayed fully opaque.
     */
    @WrapOperation(
        method = "putQuadData(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/minecraft/client/renderer/block/ModelBlockRenderer$CommonRenderStorage;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFFII)V"
        )
    )
    private void yuri$tintDoorPutBulkFlat(
        VertexConsumer consumer,
        PoseStack.Pose pose,
        BakedQuad quad,
        float red,
        float green,
        float blue,
        float alpha,
        int packedLight0,
        int packedLight1,
        Operation<Void> original
    ) {
        float m = yuri$doorVertexTintMultiplier();
        original.call(consumer, pose, quad, red * m, green * m, blue * m, alpha * m, packedLight0, packedLight1);
    }

    private static float yuri$doorVertexTintMultiplier() {
        BlockPos pos = TranslucentDoorEffects.putQuadBlockPos();
        if (pos == null) {
            return 1.0f;
        }
        return TranslucentDoorEffects.shouldTintDoorBlock(pos)
            ? TranslucentDoorEffects.doorTintMultiplier()
            : 1.0f;
    }
}
