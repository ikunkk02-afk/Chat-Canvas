package io.github.ikunkk02.chatcanvas.voice;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * Voice input overlay — microphone button, status, model download prompt.
 * Rendered over ChatScreen. Canvas UI compatible (no owo-lib).
 */
public final class VoiceInputOverlay {

    public static final int BUTTON_SPACE = 20;
    private static final int BUTTON_W = 18, BUTTON_H = 14;

    private final VoiceInputManager manager = VoiceInputManager.instance();
    private ChatScreen owner;
    private EditBox field;
    private Consumer<VoiceRecognitionResult> resultConsumer;
    private int btnX, btnY;
    private boolean mouseHolding, keyHolding, installPrompt;

    public void init(ChatScreen screen, EditBox field, Consumer<VoiceRecognitionResult> consumer) {
        owner = screen;
        this.field = field;
        resultConsumer = consumer;
        mouseHolding = false;
        keyHolding = false;
        installPrompt = false;
    }

    // ── Input ──

    public boolean mouseClicked(double mx, double my, int btn) {
        if (installPrompt) return promptClick(mx, my, btn);
        if (btn != GLFW.GLFW_MOUSE_BUTTON_LEFT || !hit(mx, my, btnX, btnY, BUTTON_W, BUTTON_H)) return false;
        if (manager.state() == VoiceInputState.MODEL_MISSING) { installPrompt = true; return true; }
        if (manager.isListening()) { manager.cancel(); mouseHolding = false; return true; }
        mouseHolding = manager.begin(resultConsumer);
        return true;
    }

    public boolean mouseReleased(double mx, double my, int btn) {
        if (btn != GLFW.GLFW_MOUSE_BUTTON_LEFT || !mouseHolding) return false;
        mouseHolding = false;
        manager.finish();
        return true;
    }

    public void keyboardPressed() {
        if (manager.state() == VoiceInputState.MODEL_MISSING) { installPrompt = true; return; }
        if (keyHolding) return;
        if (manager.state() == VoiceInputState.RECOGNIZING) manager.cancel();
        if (manager.begin(resultConsumer)) keyHolding = true;
    }

    public void keyboardReleased() {
        if (!keyHolding) return;
        keyHolding = false;
        var st = manager.state();
        if (st == VoiceInputState.LISTENING || st == VoiceInputState.MODEL_LOADING) manager.finish();
    }

    public void cancel() { mouseHolding = false; keyHolding = false; manager.cancel(); }
    public void dispose() { cancel(); owner = null; field = null; resultConsumer = null; installPrompt = false; }

    public void tick() {
        if (owner == null) return;
        long win = Minecraft.getInstance().getWindow().getWindow();
        if (mouseHolding && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_RELEASE) {
            mouseHolding = false; manager.finish();
        }
        if (keyHolding && !Minecraft.getInstance().isWindowActive()) cancel();
    }

    // ── Render ──

    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        if (owner == null || field == null) return;
        btnX = field.getX() + field.getWidth() + 5;
        btnY = field.getY() - 1;
        drawMic(ctx, mx, my);
        drawStatus(ctx);
        if (installPrompt) drawPrompt(ctx, mx, my);
    }

    private void drawMic(GuiGraphics ctx, int mx, int my) {
        var state = manager.state();
        boolean hover = hit(mx, my, btnX, btnY, BUTTON_W, BUTTON_H);
        int fill = state == VoiceInputState.LISTENING ? 0xE05D2E46
                : state == VoiceInputState.RECOGNIZING ? 0xE04A5368
                : state == VoiceInputState.MODEL_MISSING || state == VoiceInputState.ERROR ? 0xE06B4430
                : hover ? 0xD0445066 : 0xB02A3240;
        ctx.fill(btnX, btnY, btnX + BUTTON_W, btnY + BUTTON_H, fill);
        ctx.renderOutline(btnX, btnY, BUTTON_W, BUTTON_H,
                state == VoiceInputState.LISTENING ? 0xFFFF858D : 0xFF71809A);
        // Mic icon (simple lines)
        int cx = btnX + 9;
        ctx.fill(cx - 2, btnY + 3, cx + 3, btnY + 9, 0xFFE7ECF5);
        ctx.fill(cx - 4, btnY + 7, cx - 3, btnY + 10, 0xFFE7ECF5);
        ctx.fill(cx + 3, btnY + 7, cx + 4, btnY + 10, 0xFFE7ECF5);
        ctx.fill(cx - 3, btnY + 10, cx + 4, btnY + 11, 0xFFE7ECF5);
        ctx.fill(cx, btnY + 11, cx + 1, btnY + 13, 0xFFE7ECF5);
    }

    private void drawStatus(GuiGraphics ctx) {
        var state = manager.state();
        if (state != VoiceInputState.LISTENING && state != VoiceInputState.RECOGNIZING
                && state != VoiceInputState.MODEL_LOADING && state != VoiceInputState.MODEL_DOWNLOADING
                && state != VoiceInputState.MODEL_VERIFYING && state != VoiceInputState.MODEL_EXTRACTING) return;
        var font = Minecraft.getInstance().font;
        String key = state == VoiceInputState.LISTENING ? "chat_canvas.voice.listening"
                : state == VoiceInputState.RECOGNIZING ? "chat_canvas.voice.recognizing"
                : state == VoiceInputState.MODEL_DOWNLOADING ? "chat_canvas.voice.downloading"
                : "chat_canvas.voice.loading";
        Component label = Component.translatable(key);
        if (state == VoiceInputState.LISTENING && manager.settings().showPartialResults() && !manager.partial().isBlank()) {
            label = Component.translatable(key).append(Component.literal(": " + manager.partial()));
        }
        int w = Math.min(240, font.width(label) + 20);
        int x = Math.max(4, btnX + BUTTON_W - w);
        int y = Math.max(4, field.getY() - 34);
        ctx.fill(x, y, x + w, y + 20, 0xD0202632);
        ctx.renderOutline(x, y, w, 20, 0xFF71809A);
        ctx.drawString(font, label, x + 5, y + 4, 0xFFFFFFFF);
        if (state == VoiceInputState.LISTENING && manager.settings().showInputLevel()) {
            int meter = (int) ((w - 10) * Math.min(1.0, manager.level() * 8.0));
            ctx.fill(x + 5, y + 16, x + 5 + meter, y + 18, 0xFF63D297);
        }
    }

    private void drawPrompt(GuiGraphics ctx, int mx, int my) {
        int w = Math.min(330, owner.width - 16), h = 128;
        int x = (owner.width - w) / 2, y = Math.max(8, (owner.height - h) / 2);
        ctx.fill(x, y, x + w, y + h, 0xF0181D27);
        ctx.renderOutline(x, y, w, h, 0xFF71809A);
        ctx.drawString(Minecraft.getInstance().font, Component.translatable("chat_canvas.voice.model.title"), x + 10, y + 10, 0xFFFFFFFF);
        ctx.drawString(Minecraft.getInstance().font, Component.translatable("chat_canvas.voice.model.details"), x + 10, y + 28, 0xFFADB6C7);
        ctx.drawString(Minecraft.getInstance().font, Component.translatable("chat_canvas.voice.model.privacy"), x + 10, y + 44, 0xFFADB6C7);
        drawBtn(ctx, mx, my, x + 10, y + 82, 112, 20, "download");
        drawBtn(ctx, mx, my, x + 126, y + 82, 98, 20, "open");
        drawBtn(ctx, mx, my, x + 228, y + 82, w - 238, 20, "cancel");
    }

    private void drawBtn(GuiGraphics ctx, int mx, int my, int bx, int by, int bw, int bh, String action) {
        boolean hover = hit(mx, my, bx, by, bw, bh);
        ctx.fill(bx, by, bx + bw, by + bh, hover ? 0xFF3A4A62 : 0xFF343D50);
        ctx.renderOutline(bx, by, bw, bh, 0xFF71809A);
        ctx.drawCenteredString(Minecraft.getInstance().font,
                Component.translatable("chat_canvas.voice.model." + action), bx + bw / 2, by + 6, 0xFFFFFFFF);
    }

    private boolean promptClick(double mx, double my, int btn) {
        if (btn != GLFW.GLFW_MOUSE_BUTTON_LEFT || owner == null) return true;
        int w = Math.min(330, owner.width - 16), h = 128;
        int x = (owner.width - w) / 2, y = Math.max(8, (owner.height - h) / 2);
        if (!hit(mx, my, x, y, w, h)) { installPrompt = false; return true; }
        // Model download actions — TODO: wire to VoskModelManager
        installPrompt = false;
        return true;
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
