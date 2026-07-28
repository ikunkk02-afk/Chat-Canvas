package io.github.ikunkk02.chatcanvas.mixin;

import io.github.ikunkk02.chatcanvas.ChatCanvasForge;
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

/**
 * Injects Emoji picker and Command tools into the ChatScreen.
 * Uses Canvas UI — zero owo-lib dependency.
 */
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Shadow
    protected EditBox input;

    private EmojiPickerPanel chat_canvas$emojiPanel;
    private CommandToolPanel chat_canvas$commandPanel;
    private boolean chat_canvas$emojiVisible;
    private boolean chat_canvas$commandVisible;

    @Inject(method = "init", at = @At("RETURN"))
    private void chat_canvas$onInit(CallbackInfo ci) {
        if (!ChatCanvasConfig.instance().enabled()) return;
        ChatScreen self = (ChatScreen) (Object) this;
        int panelW = 200, panelH = 180;
        chat_canvas$emojiPanel = new EmojiPickerPanel(
                self.width - panelW - 4, 4, panelW, panelH,
                glyph -> insertAtCursor(input, glyph));
        chat_canvas$commandPanel = new CommandToolPanel(
                self.width - panelW - 4, 4, panelW, panelH,
                cmd -> setInput(input, cmd));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void chat_canvas$onRender(GuiGraphics ctx, int mx, int my, float delta, CallbackInfo ci) {
        if (!ChatCanvasConfig.instance().enabled()) return;
        if (chat_canvas$emojiVisible && chat_canvas$emojiPanel != null)
            chat_canvas$emojiPanel.render(ctx, mx, my, delta);
        if (chat_canvas$commandVisible && chat_canvas$commandPanel != null)
            chat_canvas$commandPanel.render(ctx, mx, my, delta);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void chat_canvas$onKeyPressed(int key, int scancode, int mods, CallbackInfoReturnable<Boolean> cir) {
        if (!ChatCanvasConfig.instance().enabled()) return;
        // Toggle emoji panel: Ctrl+E
        if (key == 69 && (mods & 2) != 0) { chat_canvas$emojiVisible = !chat_canvas$emojiVisible; cir.setReturnValue(true); return; }
        // Toggle command panel: Ctrl+T
        if (key == 84 && (mods & 2) != 0) { chat_canvas$commandVisible = !chat_canvas$commandVisible; chat_canvas$commandPanel.refresh(); cir.setReturnValue(true); }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void chat_canvas$onMouseClicked(double mx, double my, int btn, CallbackInfoReturnable<Boolean> cir) {
        if (chat_canvas$emojiVisible && chat_canvas$emojiPanel != null && chat_canvas$emojiPanel.mouseClicked(mx, my, btn))
            cir.setReturnValue(true);
        if (chat_canvas$commandVisible && chat_canvas$commandPanel != null && chat_canvas$commandPanel.mouseClicked(mx, my, btn))
            cir.setReturnValue(true);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void chat_canvas$onMouseScrolled(double mx, double my, double delta, CallbackInfoReturnable<Boolean> cir) {
        if (chat_canvas$emojiVisible && chat_canvas$emojiPanel != null && chat_canvas$emojiPanel.mouseScrolled(mx, my, delta))
            cir.setReturnValue(true);
        if (chat_canvas$commandVisible && chat_canvas$commandPanel != null && chat_canvas$commandPanel.mouseScrolled(mx, my, delta))
            cir.setReturnValue(true);
    }

    private static void insertAtCursor(EditBox field, String text) {
        if (field == null) return;
        int pos = Math.min(field.getCursorPosition(), field.getValue().length());
        String before = field.getValue().substring(0, pos);
        String after = field.getValue().substring(pos);
        field.setValue(before + text + after);
        field.setCursorPosition(pos + text.length());
    }

    private static void setInput(EditBox field, String text) {
        if (field != null) field.setValue(text);
    }
}
