package yuri.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yuri.client.ShortCommandHelper;
import yuri.data.columns.general.modules.ChatModule;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Shadow
    protected EditBox input;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void yuri$shortCmdTabComplete(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (!ChatModule.shouldShortCommands()) {
            return;
        }
        if (event.key() != InputConstants.KEY_TAB) {
            return;
        }
        String current = this.input.getValue();
        if (current == null || !current.startsWith("/") || current.contains(" ")) {
            return;
        }
        List<String> suggestions = ShortCommandHelper.tabSuggestions(current);
        if (suggestions.isEmpty()) {
            return;
        }
        this.input.setValue(suggestions.get(0));
        this.input.setCursorPosition(this.input.getValue().length());
        cir.setReturnValue(true);
    }
}
