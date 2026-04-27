package yuri.mixin;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import yuri.client.OpsecTextMasker;

@Mixin(net.minecraft.client.gui.components.ChatComponent.class)
public abstract class ChatComponentOpsecMixin {
    @ModifyVariable(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private Component yuri$maskNameInChat(Component message) {
        return OpsecTextMasker.maskComponent(message);
    }
}
