package yuri.client;

import java.util.List;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.FormattedCharSequence;
import yuri.data.columns.general.modules.ChatModule;
import yuri.mixin.ChatComponentAccess;

public final class ChatCopyHelper {
    private ChatCopyHelper() {
    }

    public static void tryCopyUnderCursor(Minecraft mc) {
        if (!ChatModule.shouldClickToCopy()) {
            return;
        }
        Screen screen = mc.screen;
        if (screen != null && !(screen instanceof ChatScreen)) {
            return;
        }

        ChatComponent chat = mc.gui.getChat();
        ChatComponentAccess access = (ChatComponentAccess) chat;
        double mx = mc.mouseHandler.getScaledXPos(mc.getWindow());
        double my = mc.mouseHandler.getScaledYPos(mc.getWindow());
        double cx = access.yuri$screenToChatX(mx);
        double cy = access.yuri$screenToChatY(my);
        int lineIndex = access.yuri$getMessageLineIndexAt(cx, cy);
        if (lineIndex < 0) {
            return;
        }

        List<GuiMessage.Line> lines = access.yuri$getTrimmedMessages();
        if (lineIndex >= lines.size()) {
            return;
        }

        String plain = toPlainText(lines.get(lineIndex).content());
        if (plain.isEmpty()) {
            return;
        }
        mc.keyboardHandler.setClipboard(plain);
    }

    private static String toPlainText(FormattedCharSequence sequence) {
        StringBuilder sb = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return sb.toString();
    }
}
