package yuri.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuri.GlobalCosmetics;
import yuri.YuriData;

@Mixin(AvatarRenderer.class)
public abstract class PlayerSizeRenderMixin {
    @Inject(method = "scale", at = @At("RETURN"))
    private void yuri$scaleLocalPlayerInThirdPerson(AvatarRenderState state, PoseStack poseStack, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || state == null) {
            return;
        }
        if (minecraft.level != null) {
            var entity = minecraft.level.getEntity(state.id);
            if (entity instanceof Player player) {
                float[] globalScale = GlobalCosmetics.customScaleFor(player.getUUID());
                if (globalScale != null) {
                    poseStack.scale(globalScale[0], globalScale[1], globalScale[2]);
                    return;
                }
            }
        }
        if (state.id != minecraft.player.getId()) {
            return;
        }
        if (minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        if (!YuriData.isPlayerSizeEnabled()) {
            return;
        }
        var settings = YuriData.PLAYER_SIZE_SETTINGS;
        poseStack.scale(settings.scaleX, settings.scaleY, settings.scaleZ);
    }
}
