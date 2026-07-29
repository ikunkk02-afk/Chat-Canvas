package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import io.github.ikunkk02.chatcanvas.editor.EditorUiStyle;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * Provides themed rendering for the Chat Canvas editor UI.
 */
public final class ModernUiTheme {

    private static EditorUiStyle currentStyle = EditorUiStyle.CHAT_CANVAS;

    /** Enable to log suspiciously large vanilla-styled components. */
    public static final boolean VANILLA_THEME_RENDER_DEBUG = false;

    /* ── colour constants (modern theme) ─────────────────────── */
    public static final int PANEL_BACKGROUND = 0xE6191C26;
    public static final int PANEL_BORDER = 0x664C566A;
    public static final int ACCENT = 0xFF70A7FF;
    public static final int TEXT_PRIMARY = 0xFFF2F4F8;
    public static final int TEXT_SECONDARY = 0xFF9EA8BA;

    /* ── reasonable bounds for themed controls ──────────────── */
    private static final int MAX_REASONABLE_BUTTON_WIDTH = 400;
    private static final int MAX_REASONABLE_BUTTON_HEIGHT = 50;

    /* ── theme switching ─────────────────────────────────────── */

    public static EditorUiStyle currentStyle() { return currentStyle; }

    public static void setStyle(EditorUiStyle style) { currentStyle = style; }

    /* ── panel surface ───────────────────────────────────────── */

    public static final Surface PANEL_SURFACE = (context, component) -> {
        int w = component.width();
        int h = component.height();
        if (w <= 0 || h <= 0) return;
        if (currentStyle == EditorUiStyle.VANILLA) {
            drawVanillaPanel(context, component.x(), component.y(), w, h);
        } else {
            shadow(context, component.x(), component.y(), w, h);
            roundedRect(context, component.x(), component.y(), w, h, 7, PANEL_BACKGROUND);
            border(context, component.x(), component.y(), w, h, PANEL_BORDER);
        }
    };

    private static void drawVanillaPanel(DrawContext context, int x, int y, int w, int h) {
        context.fill(x, y, x + w, y + h, 0xC8000000);
        context.fill(x, y, x + w, y + 1, 0xFF555555);
        context.fill(x, y + h - 1, x + w, y + h, 0xFF555555);
        context.fill(x, y, x + 1, y + h, 0xFF555555);
        context.fill(x + w - 1, y, x + w, y + h, 0xFF555555);
    }

    /* ── button factory ──────────────────────────────────────── */

    private static final Map<ButtonComponent, Long> PRESSED_AT = new WeakHashMap<>();

    private ModernUiTheme() {}

    /**
     * Create a themed button. For transparent hit targets and colour swatches
     * that should never draw a solid background, prefer
     * {@link #transparentButton(Text, Consumer)}.
     */
    public static ButtonComponent button(Text text, Consumer<ButtonComponent> action) {
        ButtonComponent button = Components.button(text, clicked -> {
            PRESSED_AT.put(clicked, System.nanoTime());
            action.accept(clicked);
        });
        button.renderer(ModernUiTheme::drawButton);
        button.textShadow(false);
        return button;
    }

    /** Create a button that never draws a solid background in either theme. */
    public static ButtonComponent transparentButton(Text text, Consumer<ButtonComponent> action) {
        ButtonComponent button = Components.button(text, action);
        button.renderer(ModernUiTheme::drawTransparentButton);
        button.textShadow(false);
        return button;
    }

    private static void drawButton(OwoUIDrawContext context, ButtonComponent button, float delta) {
        if (currentStyle == EditorUiStyle.VANILLA) {
            drawVanillaButton(context, button);
        } else {
            drawModernButton(context, button);
        }
    }

    private static void drawTransparentButton(OwoUIDrawContext context, ButtonComponent button, float delta) {
        // In vanilla theme, draw no background (prevents gray rectangle from oversized hit targets).
        // In modern theme, draw the normal modern background.
        if (currentStyle == EditorUiStyle.VANILLA) {
            // Fully transparent — just rely on text rendering or parent surface.
            return;
        }
        drawModernButton(context, button);
    }

    private static void drawModernButton(OwoUIDrawContext context, ButtonComponent button) {
        int color;
        if (!button.active()) {
            color = 0x55343A48;
        } else if (button.isHovered()) {
            color = 0xE04B5970;
        } else {
            color = 0xC8374256;
        }
        Long pressedAt = PRESSED_AT.get(button);
        boolean pressed = pressedAt != null && System.nanoTime() - pressedAt < 90_000_000L;
        int inset = pressed ? 1 : 0;
        roundedRect(context, button.getX() + inset, button.getY() + inset,
                button.getWidth() - inset * 2, button.getHeight() - inset * 2, 5, color);
        border(context, button.getX() + inset, button.getY() + inset,
                button.getWidth() - inset * 2, button.getHeight() - inset * 2,
                button.active() ? 0x554F6079 : 0x223C4452);
    }

    private static void drawVanillaButton(OwoUIDrawContext context, ButtonComponent button) {
        int w = button.getWidth();
        int h = button.getHeight();
        int x = button.getX();
        int y = button.getY();

        if (w <= 0 || h <= 0) return;

        // Defensive: if the button is abnormally large, log and skip background fill.
        if (w > MAX_REASONABLE_BUTTON_WIDTH || h > MAX_REASONABLE_BUTTON_HEIGHT) {
            if (VANILLA_THEME_RENDER_DEBUG) {
                net.minecraft.client.MinecraftClient client =
                        net.minecraft.client.Minecraft.getInstance();
                String text = "";
                try { text = button.getMessage().getString(); } catch (Exception ignored) {}
                ChatCanvas.LOGGER.warn(
                        "[ChatCanvas Vanilla UI] Oversized component: text='{}' class={} bounds={},{},{},{} " +
                        "screen={}x{} guiScale={}",
                        text, button.getClass().getSimpleName(), x, y, w, h,
                        client != null ? client.getWindow().getFramebufferWidth() : "?",
                        client != null ? client.getWindow().getFramebufferHeight() : "?",
                        client != null ? client.getWindow().getScaleFactor() : "?");
            }
            return; // Skip drawing — oversized button background would cover the preview.
        }

        int bg, borderCol;
        if (!button.active()) {
            bg = 0xFF555555; borderCol = 0xFF333333;
        } else if (button.isHovered()) {
            bg = 0xFF8B8B8B; borderCol = 0xFFFFFFFF;
        } else {
            bg = 0xFF666666; borderCol = 0xFF888888;
        }
        context.fill(x, y, x + w, y + h, bg);
        // 1px border
        context.fill(x, y, x + w, y + 1, borderCol);
        context.fill(x, y + h - 1, x + w, y + h, borderCol);
        context.fill(x, y, x + 1, y + h, borderCol);
        context.fill(x + w - 1, y, x + w, y + h, borderCol);
    }

    /* ── shared draw utilities ───────────────────────────────── */

    public static void shadow(DrawContext context, int x, int y, int width, int height) {
        roundedRect(context, x - 3, y + 4, width + 6, height + 4, 8, 0x32000000);
        roundedRect(context, x - 1, y + 2, width + 2, height + 2, 7, 0x45000000);
    }

    public static void roundedRect(DrawContext context, int x, int y,
                                    int width, int height, int radius, int color) {
        if (width <= 0 || height <= 0) return;
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        context.fill(x + r, y, x + width - r, y + height, color);
        context.fill(x, y + r, x + width, y + height - r, color);
        for (int i = 0; i < r; i++) {
            int inset = r - (int) Math.sqrt(Math.max(0, r * r - (r - i) * (r - i)));
            context.fill(x + inset, y + i, x + width - inset, y + i + 1, color);
            context.fill(x + inset, y + height - i - 1, x + width - inset, y + height - i, color);
        }
    }

    public static void border(DrawContext context, int x, int y,
                               int width, int height, int color) {
        if (width <= 1 || height <= 1) return;
        context.fill(x + 5, y, x + width - 5, y + 1, color);
        context.fill(x + 5, y + height - 1, x + width - 5, y + height, color);
        context.fill(x, y + 5, x + 1, y + height - 5, color);
        context.fill(x + width - 1, y + 5, x + width, y + height - 5, color);
    }
}
