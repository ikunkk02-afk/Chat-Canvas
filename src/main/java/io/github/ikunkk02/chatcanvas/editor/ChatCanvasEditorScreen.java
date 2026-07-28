package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.ChatCanvasForge;
import io.github.ikunkk02.chatcanvas.animation.AnimationClock;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import io.github.ikunkk02.chatcanvas.ui.forge.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;

public final class ChatCanvasEditorScreen extends Screen {

    private final Screen parent;
    private final AnimationClock clock = new AnimationClock();
    private EditorSession session;
    private CanvasTheme theme = CanvasTheme.DARK;

    private CanvasButton undoBtn, redoBtn, themeBtn, saveBtn, cancelBtn;
    private CanvasTabBar tabBar;
    private CanvasScrollPanel settingsScroll;
    private CanvasFlowLayout settingsContent;

    private enum DragTarget { NONE, PLAYER, COMMAND }
    private DragTarget dragging = DragTarget.NONE;

    public ChatCanvasEditorScreen(@Nullable Screen parent) {
        super(Component.translatable("chat_canvas.editor.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        Minecraft mc = minecraft;
        if (mc == null) return;

        session = new EditorSession(ChatCanvasConfig.instance().settings(), width, height);
        clock.reset();

        int btnW = 56, btnH = 18, gap = 4, y = 4, x = 4;
        undoBtn = btnStr(x, y, btnW, btnH, "\u21A9", b -> undo());
        redoBtn = btnStr(x += btnW + gap, y, btnW, btnH, "\u21AA", b -> redo());
        themeBtn = btnStr(x += btnW + gap, y, btnW + 16, btnH, "\u25D0", b -> flipTheme());
        saveBtn = btnCmp(width - btnW * 2 - gap - 8, y, btnW, btnH,
                Component.translatable("chat_canvas.editor.save"), b -> save());
        cancelBtn = btnCmp(width - btnW - 4, y, btnW, btnH,
                Component.translatable("chat_canvas.editor.cancel"), b -> onClose());

        tabBar = new CanvasTabBar(4, y + btnH + 6, width - 8, 16, id -> refresh(),
                theme.tabDefault(), theme.tabSelected(), theme.textPrimary());
        tabBar.setTabs(List.of(
            new CanvasTabBar.Tab("layout", Component.translatable("chat_canvas.editor.tab.layout"), true),
            new CanvasTabBar.Tab("text", Component.translatable("chat_canvas.editor.tab.text"), false)
        ));

        int panelX = width - 216, panelY = y + btnH + 28;
        settingsScroll = new CanvasScrollPanel(panelX, panelY, 210, height - panelY - 6, 400);
        settingsContent = new CanvasFlowLayout(0, 0, 196, 4);
        settingsScroll.addChild(settingsContent);
        refresh();
    }

    private CanvasButton btnStr(int x, int y, int w, int h, String text, java.util.function.Consumer<CanvasButton> a) {
        return new CanvasButton(x, y, w, h, Component.literal(text), a);
    }
    private CanvasButton btnCmp(int x, int y, int w, int h, Component text, java.util.function.Consumer<CanvasButton> a) {
        return new CanvasButton(x, y, w, h, text, a);
    }

    private void undo() { session.undo(); refresh(); }
    private void redo() { session.redo(); refresh(); }
    private void flipTheme() { theme = (theme == CanvasTheme.DARK) ? CanvasTheme.LIGHT : CanvasTheme.DARK; }

    private void save() {
        ChatCanvasConfig.instance().save(session.settings());
        ChatCanvasForge.LOGGER.info("Chat Canvas settings saved");
        onClose();
    }

    private void refresh() {
        settingsContent.clearChildren();
        PixelLayout pl = session.layout();
        PixelLayout cl = session.layout(EditorChannel.COMMAND_SYSTEM);

        addScrub("player x", pl.x(), 0, width, v -> setPlayerX(v));
        addScrub("player y", pl.y(), 0, height, v -> setPlayerY(v));
        addScrub("player w", pl.width(), 80, 600, v -> setPlayerW(v));
        addScrub("cmd x", cl.x(), 0, width, v -> setCmdX(v));
        addScrub("cmd y", cl.y(), 0, height, v -> setCmdY(v));

        ChatTextConfig t = session.text();
        addScrub("scale", t.fontScale(), 0.5, 3.0,
                v -> session.setText(new ChatTextConfig(v, t.lineSpacing(), t.textOpacity(), t.alignment(), t.shadow(), t.characterSpacing())));
    }

    private void addScrub(String key, double val, double min, double max, java.util.function.Consumer<Double> setter) {
        settingsContent.addChild(new CanvasNumericScrubber(0, 0, 0, 18,
                key, () -> val, v -> { setter.accept(v); refresh(); },
                min, max, (max - min) / 100.0, val, "%.1f"));
    }

    private void setPlayerX(double v) { PixelLayout p = session.layout(); session.setLayout(new PixelLayout((int)v, p.y(), p.width(), p.height())); }
    private void setPlayerY(double v) { PixelLayout p = session.layout(); session.setLayout(new PixelLayout(p.x(), (int)v, p.width(), p.height())); }
    private void setPlayerW(double v) { PixelLayout p = session.layout(); session.setLayout(new PixelLayout(p.x(), p.y(), (int)v, p.height())); }
    private void setCmdX(double v) { PixelLayout c = session.layout(EditorChannel.COMMAND_SYSTEM); session.setLayout(EditorChannel.COMMAND_SYSTEM, new PixelLayout((int)v, c.y(), c.width(), c.height())); }
    private void setCmdY(double v) { PixelLayout c = session.layout(EditorChannel.COMMAND_SYSTEM); session.setLayout(EditorChannel.COMMAND_SYSTEM, new PixelLayout(c.x(), (int)v, c.width(), c.height())); }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE) { onClose(); return true; }
        if (key == GLFW.GLFW_KEY_Z && (mods & GLFW.GLFW_MOD_CONTROL) != 0) { undo(); return true; }
        if (key == GLFW.GLFW_KEY_Y && (mods & GLFW.GLFW_MOD_CONTROL) != 0) { redo(); return true; }
        return super.keyPressed(key, scancode, mods);
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, theme.background());
        PixelLayout pl = session.layout(), cl = session.layout(EditorChannel.COMMAND_SYSTEM);
        drawBox(ctx, pl.x(), pl.y(), pl.width(), 20, Component.translatable("chat_canvas.editor.preview.player"), theme.textPrimary());
        drawBox(ctx, cl.x(), cl.y(), cl.width(), 20, Component.translatable("chat_canvas.editor.preview.command"), theme.textSecondary());

        undoBtn.render(ctx, mx, my, delta); redoBtn.render(ctx, mx, my, delta);
        themeBtn.render(ctx, mx, my, delta); saveBtn.render(ctx, mx, my, delta);
        cancelBtn.render(ctx, mx, my, delta);
        tabBar.render(ctx, mx, my, delta); settingsScroll.render(ctx, mx, my, delta);
        super.render(ctx, mx, my, delta);
    }

    private void drawBox(GuiGraphics ctx, int x, int y, int w, int h, Component label, int color) {
        ctx.fill(x, y, x + w, y + h, theme.widgetDefault());
        ctx.drawString(font, label, x + 4, y + (h - 8) / 2, color);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (in(session.layout(), mx, my)) { dragging = DragTarget.PLAYER; return true; }
        if (in(session.layout(EditorChannel.COMMAND_SYSTEM), mx, my)) { dragging = DragTarget.COMMAND; return true; }
        if (undoBtn.mouseClicked(mx, my, btn)) return true;
        if (redoBtn.mouseClicked(mx, my, btn)) return true;
        if (themeBtn.mouseClicked(mx, my, btn)) return true;
        if (saveBtn.mouseClicked(mx, my, btn)) return true;
        if (cancelBtn.mouseClicked(mx, my, btn)) return true;
        if (tabBar.mouseClicked(mx, my, btn)) { refresh(); return true; }
        if (settingsScroll.mouseClicked(mx, my, btn)) return true;
        return super.mouseClicked(mx, my, btn);
    }

    @Override public boolean mouseReleased(double mx, double my, int btn) { dragging = DragTarget.NONE; return super.mouseReleased(mx, my, btn); }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (dragging == DragTarget.NONE) return super.mouseDragged(mx, my, btn, dx, dy);
        EditorChannel ch = dragging == DragTarget.PLAYER ? EditorChannel.PLAYER_CHAT : EditorChannel.COMMAND_SYSTEM;
        PixelLayout l = session.layout(ch);
        session.setLayout(ch, new PixelLayout(l.x() + (int) dx, l.y() + (int) dy, l.width(), l.height()));
        return true;
    }

    private static boolean in(PixelLayout l, double mx, double my) { return mx >= l.x() && mx < l.x() + l.width() && my >= l.y() && my < l.y() + 20; }

    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public void tick() { clock.tick(); }
    @Override public boolean isPauseScreen() { return false; }
}
