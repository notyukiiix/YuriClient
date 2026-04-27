package yuri.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yuri.YuriData;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private float offHandHeight;
    @Shadow
    private float oOffHandHeight;

    @Unique
    private boolean yuri$armWithItemPosePushed;
    @Unique
    private boolean yuri$fpvItemSizePosePushed;

    @Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true)
    private void yuri$instantReplaceWhenIgnoreEquip(ItemStack previous, ItemStack next, CallbackInfoReturnable<Boolean> cir) {
        if (YuriData.isAnimationsEnabled() && YuriData.ANIMATION_SETTINGS.ignoreEquip) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void yuri$finishEquipImmediatelyWhenIgnoreEquip(CallbackInfo ci) {
        if (!YuriData.isAnimationsEnabled() || !YuriData.ANIMATION_SETTINGS.ignoreEquip) {
            return;
        }
        if (mainHandHeight < 1.0F || offHandHeight < 1.0F) {
            mainHandHeight = 1.0F;
            oMainHandHeight = 1.0F;
            offHandHeight = 1.0F;
            oOffHandHeight = 1.0F;
        }
    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void yuri$applyFirstPersonTransform(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand,
                                                float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack,
                                                SubmitNodeCollector nodeCollector, int packedLight, CallbackInfo ci) {
        if (!YuriData.isAnimationsEnabled()) {
            yuri$armWithItemPosePushed = false;
            return;
        }

        var settings = YuriData.ANIMATION_SETTINGS;
        poseStack.pushPose();
        poseStack.translate(settings.posX, settings.posY, settings.posZ);
        yuri$armWithItemPosePushed = true;
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void yuri$restorePose(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand,
                                  float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack,
                                  SubmitNodeCollector nodeCollector, int packedLight, CallbackInfo ci) {
        if (yuri$armWithItemPosePushed) {
            yuri$armWithItemPosePushed = false;
            poseStack.popPose();
        }
    }

    @Inject(
        method = "renderItem",
        at = @At("HEAD")
    )
    private void yuri$pushItemSizeInFirstPerson(
        LivingEntity entity,
        ItemStack stack,
        ItemDisplayContext displayContext,
        PoseStack poseStack,
        SubmitNodeCollector nodeCollector,
        int packedLight,
        CallbackInfo ci
    ) {
        yuri$fpvItemSizePosePushed = false;
        if (!YuriData.isAnimationsEnabled()) {
            return;
        }
        if (!(entity instanceof LocalPlayer) || !yuri$isFirstPersonHandContext(displayContext)) {
            return;
        }

        float scale = 0.20F + (3.00F - 0.20F) * ((yuri$clampInt(YuriData.ANIMATION_SETTINGS.size, 1, 20) - 1) / 19.0F);
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        yuri$fpvItemSizePosePushed = true;
    }

    @Inject(
        method = "renderItem",
        at = @At("RETURN")
    )
    private void yuri$popItemSizeInFirstPerson(
        LivingEntity entity,
        ItemStack stack,
        ItemDisplayContext displayContext,
        PoseStack poseStack,
        SubmitNodeCollector nodeCollector,
        int packedLight,
        CallbackInfo ci
    ) {
        if (yuri$fpvItemSizePosePushed) {
            yuri$fpvItemSizePosePushed = false;
            poseStack.popPose();
        }
    }

    @Unique
    private static boolean yuri$isFirstPersonHandContext(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }

    @Unique
    private static int yuri$clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
