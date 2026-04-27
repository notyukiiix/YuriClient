package yuri.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuri.data.columns.cheats.modules.MobEspModule;
import yuri.data.columns.visual.modules.RenderOptimiserModule;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleAddEntity", at = @At("HEAD"), cancellable = true)
    private void yuri$renderOptimiserAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        if (RenderOptimiserModule.shouldBlockEntitySpawn(packet.getType())) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSetEntityData", at = @At("HEAD"), cancellable = true)
    private void yuri$renderOptimiserEntityData(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        MobEspModule.onEntityDataUpdated(packet.id());
        Minecraft minecraft = Minecraft.getInstance();
        int selfId = minecraft.player != null ? minecraft.player.getId() : -1;
        if (RenderOptimiserModule.shouldCancelArcherBoneMealMetadata(packet.id(), selfId, packet.packedItems())) {
            ci.cancel();
            scheduleRemoveEntity(minecraft, packet.id());
        }
    }

    @Inject(method = "handleParticleEvent", at = @At("HEAD"), cancellable = true)
    private void yuri$renderOptimiserParticles(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
        if (RenderOptimiserModule.shouldCancelExplosionParticle(packet.getParticle())) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSetEquipment", at = @At("HEAD"), cancellable = true)
    private void yuri$renderOptimiserEquipment(ClientboundSetEquipmentPacket packet, CallbackInfo ci) {
        int entityId = packet.getEntity();
        for (Pair<EquipmentSlot, ItemStack> entry : packet.getSlots()) {
            if (RenderOptimiserModule.shouldDiscardEntityForEquipmentSlot(entry.getFirst(), entry.getSecond())) {
                ci.cancel();
                scheduleRemoveEntity(Minecraft.getInstance(), entityId);
                return;
            }
        }
    }

    private static void scheduleRemoveEntity(Minecraft minecraft, int entityId) {
        minecraft.execute(() -> {
            ClientLevel level = minecraft.level;
            if (level != null) {
                level.removeEntity(entityId, RemovalReason.DISCARDED);
            }
        });
    }
}
