package yuri.mixin;

import java.util.List;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatComponent.class)
public interface ChatComponentAccess {
    @Invoker("refreshTrimmedMessages")
    void yuri$refreshTrimmedMessages();

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> yuri$getTrimmedMessages();

    @Invoker("screenToChatX")
    double yuri$screenToChatX(double mouseX);

    @Invoker("screenToChatY")
    double yuri$screenToChatY(double mouseY);

    @Invoker("getMessageLineIndexAt")
    int yuri$getMessageLineIndexAt(double chatX, double chatY);
}
