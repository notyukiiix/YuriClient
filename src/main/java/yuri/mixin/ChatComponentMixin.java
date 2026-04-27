package yuri.mixin;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuri.data.columns.general.modules.ChatModule;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Shadow
    @Final
    private List<GuiMessage> allMessages;

    @Unique
    private static String yuri$lastNormalizedKey = "";

    @Unique
    private static Component yuri$lastMessageComponent = Component.empty();

    @Unique
    private static int yuri$repeatCount = 0;

    @Unique
    private static boolean yuri$reentrant = false;

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("HEAD"), cancellable = true)
    private void yuri$stackSimilarMessages(Component message, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag, CallbackInfo ci) {
        if (yuri$reentrant) {
            return;
        }

        if (!ChatModule.shouldStackSimilarMessages()) {
            yuri$resetState(message);
            return;
        }

        String currentKey = yuri$normalizeStackKey(message.getString());
        if (!currentKey.equals(yuri$lastNormalizedKey)) {
            yuri$resetState(message);
            return;
        }

        yuri$repeatCount++;
        if (!this.allMessages.isEmpty()) {
            this.allMessages.remove(0);
        }
        ((ChatComponentAccess) (Object) this).yuri$refreshTrimmedMessages();

        String suffix = yuri$repeatCount > 100 ? " (100+)" : " (" + yuri$repeatCount + ")";
        Component stacked = Component.empty()
            .append(yuri$lastMessageComponent.copy())
            .append(Component.literal(suffix).withStyle(ChatFormatting.GRAY));

        yuri$reentrant = true;
        try {
            ((ChatComponent) (Object) this).addMessage(stacked, signature, tag);
        } finally {
            yuri$reentrant = false;
        }
        ci.cancel();
    }

    @Inject(method = "clearMessages", at = @At("HEAD"))
    private void yuri$onClearMessages(boolean clearRecentChat, CallbackInfo ci) {
        yuri$lastNormalizedKey = "";
        yuri$lastMessageComponent = Component.empty();
        yuri$repeatCount = 0;
    }

    @Unique
    private static void yuri$resetState(Component message) {
        yuri$lastNormalizedKey = yuri$normalizeStackKey(message.getString());
        yuri$lastMessageComponent = message.copy();
        yuri$repeatCount = 1;
    }

    /**
     * Strips our repeat suffix so "hello (3)" and "hello" are treated as the same chat key.
     */
    @Unique
    private static String yuri$normalizeStackKey(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.stripTrailing();
        if (s.endsWith(" (100+)")) {
            return s.substring(0, s.length() - " (100+)".length());
        }
        int open = s.lastIndexOf(" (");
        if (open > 0 && s.endsWith(")")) {
            String inner = s.substring(open + 2, s.length() - 1);
            if (!inner.isEmpty() && inner.chars().allMatch(Character::isDigit)) {
                return s.substring(0, open);
            }
        }
        return s;
    }
}
