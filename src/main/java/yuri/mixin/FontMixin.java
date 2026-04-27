package yuri.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import yuri.client.OpsecTextMasker;

@Mixin(Font.class)
public abstract class FontMixin {
    @ModifyVariable(method = "drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0)
    private String yuri$replaceNamesInDrawString(String text) {
        return OpsecTextMasker.maskString(text);
    }

    @ModifyVariable(method = "drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0)
    private Component yuri$replaceNamesInDrawComponent(Component component) {
        return OpsecTextMasker.maskComponent(component);
    }

    @ModifyVariable(method = "prepareText(Ljava/lang/String;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0)
    private String yuri$replaceNamesInPrepareText(String text) {
        return OpsecTextMasker.maskString(text);
    }

    @ModifyVariable(method = "width(Ljava/lang/String;)I",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0)
    private String yuri$replaceNamesInWidth(String text) {
        return OpsecTextMasker.maskString(text);
    }
}
