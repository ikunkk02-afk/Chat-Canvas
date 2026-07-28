package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.ChatCanvasForge;
import io.github.ikunkk02.chatcanvas.animation.AnimationClock;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasSettings;
import io.github.ikunkk02.chatcanvas.ui.forge.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Chat Canvas editor screen — native Canvas UI (replaces owo-lib version).
 */
public final class ChatCanvasEditorScreen extends Screen {

    private final Screen parent;
    private final AnimationClock clock = new AnimationClock();

    private EditorSession session;
    private CanvasTheme theme = CanvasTheme.DARK;

    // Widgets
    private CanvasButton undoBtn, redoBtn, themeBtn, saveBtn, cancelBtn;
    private CanvasTabBar tabBar;
    private CanvasScrollPanel settingsPanel;
    private CanvasFlowLayout settingsLayout;

    // Preview box drag
    private enum DragTarget { NONE, PLAYER, COMMAND }
    private DragTarget dragTarget = DragTarget.NONE;

    public ChatCanvasEditorScreen(@Nullable Screen parent) {
        super(Component.translatable("chat_canvas.editor.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        Minecraft mc = minecraft;
        if (mc == null) return;

        session = new EditorSession(ChatCanvasConfig.instance().settings, width, height);
        clock.reset();

        int btnW = 56, btnH = 18, gap = 4, tx = 4, ty = 4;
        undoBtn = button(tx, ty, btnW, btnH, "↩", b -> session.undo());
        redoBtn = button(tx += btnW + gap, ty, btnW, btnH, "↪", b -> session.redo());
        themeBtn = button(tx += btnW + gap, ty, btnW + 16, btnH, "◐", b -> toggleTheme());

        saveBtn = button(width - btnW * 2 - gap - 8, ty, btnW, btnH,
                Component.translatable("chat_canvas.editor.save"), b -> save());
        cancelBtn = button(width - btnW - 4, ty, btnW, btnH,
                Component.translatable("chat_canvas.editor.cancel"), b -> onClose());

        int tabY = ty + btnH + 6;
        tabBar = new CanvasTabBar(4, tabY, width - 8, 16, this::selectTab,
                theme.tabDefault(), theme.tabSelected(), theme.textPrimary());
        tabBar.setTabs(List.of(
            new CanvasTabBar.Tab("layout", Component.translatable("chat_canvas.editor.tab.layout"), true),
            new CanvasTabBar.Tab("text", Component.translatable("chat_canvas.editor.tab.text"), false),
            new CanvasTabBar.Tab("background", Component.translatable("chat_canvas.editor.tab.background"), false),
            new CanvasTabBar.Tab("behavior", Component.translatable("chat_canvas.editor.tab.behavior"), false)
        ));

        int px = width - 216, py = tabY + 22, pw = 210, ph = height - py - 6;
        settingsPanel = new CanvasScrollPanel(px, py, pw, ph, 600);
        settingsLayout = new CanvasFlowLayout(0, 0, pw - 8, 4);
        settingsPanel.addChild(settingsLayout);

        refreshSettings();
    }

    private CanvasButton button(int x, int y, int w, int h, String text, Consumer<CanvasButton> action) {
        return new CanvasButton(x, y, w, h, Component.literal(text), action);
    }

    private CanvasButton button(int x, int y, int w, int h, Component label, Consumer<CanvasButton> action) {
        return new CanvasButton(x, y, w, h, label, action);
    }

    private void toggleTheme() {
        theme = (theme == CanvasTheme.DARK) ? CanvasTheme.LIGHT : CanvasTheme.DARK;
    }

    private void selectTab(String id) { refreshSettings(); }

    private void refreshSettings() {
        settingsLayout.clearChildren();
        var snap = session.snapshot();
        var layout = snap.layout();

        scrub("player.x", () -> (double) layout.playerChatX(), v -> layout.setPlayerChatX(v.intValue()), 0, 1920, 1, 40);
        scrub("player.y", () -> (double) layout.playerChatY(), v -> layout.setPlayerChatY(v.intValue()), 0, 1080, 1, 60);
        scrub("player.w", () -> (double) layout.playerChatWidth(), v -> layout.setPlayerChatWidth(v.intValue()), 80, 600, 1, 320);
        scrub("cmd.x", () -> (double) layout.commandChatX(), v -> layout.setCommandChatX(v.intValue()), 0, 1920, 1, 960);
        scrub("cmd.y", () -> (double) layout.commandChatY(), v -> layout.setCommandChatY(v.intValue()), 0, 1080, 1, 60);

        var textCfg = snap.text();
        scrub("scale", textCfg::fontScale, textCfg::setFontScale, 0.5, 3.0, 0.05, 1.0);
        scrub("lineSpacing", textCfg::lineSpacing, textCfg::setLineSpacing, 0.0, 2.0, 0.05, 0.0);
        scrub("charSpacing", textCfg::charSpacing, textCfg::setCharSpacing, 0.0, 1.0, 0.01, 0.0);
    }

    private void scrub(String key, Supplier<Double> get, Consumer<Double> set,
                        double min, double max, double step, double def) {
        settingsLayout.addChild(new CanvasNumericScrubber(2, 0, 0, 18,
                key, get, v -> { set.accept(v); session.commit(); },
                min, max, step, def, "%.2f"));
    }

    private void save() {
        ChatCanvasConfig.instance().saveIfSafe();
        ChatCanvasForge.LOGGER.info("Chat Canvas settings saved");
        onClose();
    }

    @Override
    public void onClose() { minecraft.setScreen(parent); }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE) { onClose(); return true; }
        if (key == GLFW.GLFW_KEY_Z && (mods & GLFW.GLFW_MOD_CONTROL) != 0) { session.undo(); refreshSettings(); return true; }
        if (key == GLFW.GLFW_KEY_Y && (mods & GLFW.GLFW_MOD_CONTROL) != 0) { session.redo(); refreshSettings(); return true; }
        return super.keyPressed(key, scancode, mods);
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, theme.background());

        var snap = session.snapshot();
        var layout = snap.layout();
        // Preview boxes
        drawPreview(ctx, layout.playerChatX(), layout.playerChatY(), layout.playerChatWidth(), 20,
                Component.translatable("chat_canvas.editor.preview.player"), theme.textPrimary());
        drawPreview(ctx, layout.commandChatX(), layout.commandChatY(), layout.playerChatWidth(), 20,
                Component.translatable("chat_canvas.editor.preview.command"), theme.textSecondary());

        undoBtn.render(ctx, mx, my, delta);
        redoBtn.render(ctx, mx, my, delta);
        themeBtn.render(ctx, mx, my, delta);
        saveBtn.render(ctx, mx, my, delta);
        cancelBtn.render(ctx, mx, my, delta);
        tabBar.render(ctx, mx, my, delta);
        settingsPanel.render(ctx, mx, my, delta);

        super.render(ctx, mx, my, delta);
    }

    private void drawPreview(GuiGraphics ctx, int x, int y, int w, int h, Component label, int color) {
        ctx.fill(x, y, x + w, y + h, theme.widgetDefault());
        ctx.drawString(font, label, x + 4, y + 6, color);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        var layout = session.snapshot().layout();
        if (inBox(mx, my, layout.playerChatX(), layout.playerChatY(), layout.playerChatWidth(), 20)) {
            dragTarget = DragTarget.PLAYER; return true;
        }
        if (inBox(mx, my, layout.commandChatX(), layout.commandChatY(), layout.playerChatWidth(), 20)) {
            dragTarget = DragTarget.COMMAND; return true;
        }
        if (undoBtn.mouseClicked(mx, my, btn)) { refreshSettings(); return true; }
        if (redoBtn.mouseClicked(mx, my, btn)) { refreshSettings(); return true; }
        if (themeBtn.mouseClicked(mx, my, btn)) { refreshSettings(); return true; }
        if (saveBtn.mouseClicked(mx, my, btn)) return true;
        if (cancelBtn.mouseClicked(mx, my, btn)) return true;
        if (tabBar.mouseClicked(mx, my, btn)) { refreshSettings(); return true; }
        if (settingsPanel.mouseClicked(mx, my, btn)) return true;
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) { dragTarget = DragTarget.NONE; return super.mouseReleased(mx, my, btn); }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        var layout = session.snapshot().layout();
        switch (dragTarget) {
            case PLAYER -> { layout.setPlayerChatX(layout.playerChatX() + (int) dx); layout.setPlayerChatY(layout.playerChatY() + (int) dy); return true; }
            case COMMAND -> { layout.setCommandChatX(layout.commandChatX() + (int) dx); layout.setCommandChatY(layout.commandChatY() + (int) dy); return true; }
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    private static boolean inBox(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override public void tick() { clock.tick(); }
    @Override public boolean isPauseScreen() { return false; }
}
