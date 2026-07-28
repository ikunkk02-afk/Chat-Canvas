package io.github.ikunkk02.chatcanvas.mixin;

import io.github.ikunkk02.chatcanvas.chat.emoji.EmojiPickerPanel;
import io.github.ikunkk02.chatcanvas.chat.command.ui.CommandToolPanel;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Shadow protected EditBox input;

    private EmojiPickerPanel cc$emojiPanel;
    private CommandToolPanel cc$commandPanel;
    private boolean cc$showEmoji, cc$showCmd;

    @Inject(method = "init", at = @At("RETURN"))
    private void cc$onInit(CallbackInfo ci) {
        if (!ChatCanvasConfig.instance().enabled()) return;
        ChatScreen self = (ChatScreen) (Object) this;
        int pw = 200, ph = 180;
        cc$emojiPanel = new EmojiPickerPanel(self.width - pw - 4, 4, pw, ph,
                g -> insertAt(input, g));
        cc$commandPanel = new CommandToolPanel(self.width - pw - 4, 4, pw, ph,
                List.of(), new ArrayList<>(), cmd -> setText(input, cmd));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void cc$onRender(GuiGraphics ctx, int mx, int my, float delta, CallbackInfo ci) {
        if (cc$showEmoji && cc$emojiPanel != null) cc$emojiPanel.render(ctx, mx, my, delta);
        if (cc$showCmd && cc$commandPanel != null) cc$commandPanel.render(ctx, mx, my, delta);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void cc$onKey(int key, int sc, int mods, CallbackInfoReturnable<Boolean> cir) {
        if (!ChatCanvasConfig.instance().enabled()) return;
        if (key == 69 && (mods & 2) != 0) { cc$showEmoji = !cc$showEmoji; cir.setReturnValue(true); return; }
        if (key == 84 && (mods & 2) != 0) { cc$showCmd = !cc$showCmd; cir.setReturnValue(true); }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void cc$onClick(double mx, double my, int btn, CallbackInfoReturnable<Boolean> cir) {
        if (cc$showEmoji && cc$emojiPanel != null && cc$emojiPanel.mouseClicked(mx, my, btn)) cir.setReturnValue(true);
        if (cc$showCmd && cc$commandPanel != null && cc$commandPanel.mouseClicked(mx, my, btn)) cir.setReturnValue(true);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void cc$onScroll(double mx, double my, double delta, CallbackInfoReturnable<Boolean> cir) {
        if (cc$showEmoji && cc$emojiPanel != null && cc$emojiPanel.mouseScrolled(mx, my, delta)) cir.setReturnValue(true);
        if (cc$showCmd && cc$commandPanel != null && cc$commandPanel.mouseScrolled(mx, my, delta)) cir.setReturnValue(true);
    }

    private static void insertAt(EditBox box, String s) {
        if (box == null) return;
        int pos = box.getCursorPosition();
        if (pos < 0 || pos > box.getValue().length()) return;
        box.setValue(box.getValue().substring(0, pos) + s + box.getValue().substring(pos));
        box.setCursorPosition(pos + s.length());
    }

    private static void setText(EditBox box, String s) {
        if (box != null) box.setValue(s);
    }
}
