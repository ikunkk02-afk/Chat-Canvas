package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import io.github.ikunkk02.chatcanvas.editor.EditorUiStyle;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

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

    /* ── colour constants (Minecraft-modern neutral theme) ───── */
    public static final int SCREEN_OVERLAY = 0x88000000;
    public static final int PANEL_BACKGROUND = 0xE6141414;
    public static final int PANEL_ELEVATED = 0xF01B1B1B;
    public static final int PANEL_BORDER = 0x80585858;
    public static final int DIVIDER = 0x554A4A4A;
    public static final int CONTROL_BACKGROUND = 0xCC292929;
    public static final int CONTROL_HOVER = 0xE03A3A3A;
    public static final int CONTROL_ACTIVE = 0xE0443F32;
    public static final int CONTROL_DISABLED = 0x66303030;
    public static final int ACCENT = 0xFFB7A26A;
    public static final int ACCENT_MUTED = 0x997C704C;
    public static final int TEXT_PRIMARY = 0xFFF1F1F1;
    public static final int TEXT_SECONDARY = 0xFFB5B5B5;
    public static final int TEXT_DISABLED = 0xFF777777;
    public static final int DANGER = 0xFFD98282;
    public static final int WARNING = 0xFFCDA661;
    public static final int SUCCESS = 0xFF8FA879;
    public static final int SCROLLBAR = 0xFF777777;
    public static final int SHADOW = 0x40000000;

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
            roundedRect(context, component.x(), component.y(), w, h, 2, PANEL_BACKGROUND);
            border(context, component.x(), component.y(), w, h, PANEL_BORDER);
        }
    };

    /** A neutral surface for chat overlays which is independent of editor theme selection. */
    public static final Surface FIXED_PANEL_SURFACE = (context, component) ->
            drawFixedPanel(context, component.x(), component.y(), component.width(), component.height(), true);

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
        ButtonComponent button = UIComponents.button(text, clicked -> {
            PRESSED_AT.put(clicked, System.nanoTime());
            action.accept(clicked);
        });
        button.renderer(ModernUiTheme::drawButton);
        button.textShadow(false);
        return button;
    }

    /** Create a button that never draws a solid background in either theme. */
    public static ButtonComponent transparentButton(Text text, Consumer<ButtonComponent> action) {
        ButtonComponent button = UIComponents.button(text, action);
        button.renderer(ModernUiTheme::drawTransparentButton);
        button.textShadow(false);
        return button;
    }

    /** Create a neutral chat-overlay button independent of the editor theme preference. */
    public static ButtonComponent fixedButton(Text text, Consumer<ButtonComponent> action) {
        ButtonComponent button = UIComponents.button(text, clicked -> {
            PRESSED_AT.put(clicked, System.nanoTime());
            action.accept(clicked);
        });
        button.renderer((context, component, delta) -> drawNeutralButton(context, component));
        button.textShadow(false);
        return button;
    }

    private static void drawButton(OwoUIGraphics context, ButtonComponent button, float delta) {
        if (currentStyle == EditorUiStyle.VANILLA) {
            drawVanillaButton(context, button);
        } else {
            drawModernButton(context, button);
        }
    }

    private static void drawTransparentButton(OwoUIGraphics context, ButtonComponent button, float delta) {
        // In vanilla theme, draw no background (prevents gray rectangle from oversized hit targets).
        // In modern theme, draw the normal modern background.
        if (currentStyle == EditorUiStyle.VANILLA) {
            // Fully transparent — just rely on text rendering or parent surface.
            return;
        }
        drawModernButton(context, button);
    }

    private static void drawModernButton(OwoUIGraphics context, ButtonComponent button) {
        drawNeutralButton(context, button);
    }

    private static void drawNeutralButton(OwoUIGraphics context, ButtonComponent button) {
        int color = !button.active()
                ? CONTROL_DISABLED
                : button.isHovered() ? CONTROL_HOVER : CONTROL_BACKGROUND;
        Long pressedAt = PRESSED_AT.get(button);
        boolean pressed = pressedAt != null && System.nanoTime() - pressedAt < 90_000_000L;
        int inset = pressed ? 1 : 0;
        roundedRect(context, button.getX() + inset, button.getY() + inset,
                button.getWidth() - inset * 2, button.getHeight() - inset * 2, 2, color);
        border(context, button.getX() + inset, button.getY() + inset,
                button.getWidth() - inset * 2, button.getHeight() - inset * 2,
                button.active() ? PANEL_BORDER : DIVIDER);
    }

    private static void drawVanillaButton(OwoUIGraphics context, ButtonComponent button) {
        int w = button.getWidth();
        int h = button.getHeight();
        int x = button.getX();
        int y = button.getY();

        if (w <= 0 || h <= 0) return;

        // Defensive: if the button is abnormally large, log and skip background fill.
        if (w > MAX_REASONABLE_BUTTON_WIDTH || h > MAX_REASONABLE_BUTTON_HEIGHT) {
            if (VANILLA_THEME_RENDER_DEBUG) {
                net.minecraft.client.MinecraftClient client =
                        net.minecraft.client.MinecraftClient.getInstance();
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
        roundedRect(context, x + 1, y + 2, width, height, 2, SHADOW);
    }

    public static void drawFixedPanel(DrawContext context, int x, int y,
                                      int width, int height, boolean withShadow) {
        if (width <= 0 || height <= 0) return;
        if (withShadow) shadow(context, x, y, width, height);
        roundedRect(context, x, y, width, height, 2, PANEL_ELEVATED);
        border(context, x, y, width, height, PANEL_BORDER);
    }

    public static void drawFixedControl(DrawContext context, int x, int y,
                                        int width, int height,
                                        boolean hovered, boolean selected, boolean enabled) {
        int background = !enabled ? CONTROL_DISABLED
                : selected ? CONTROL_ACTIVE
                : hovered ? CONTROL_HOVER : CONTROL_BACKGROUND;
        roundedRect(context, x, y, width, height, 2, background);
        border(context, x, y, width, height,
                selected ? ACCENT : enabled ? PANEL_BORDER : DIVIDER);
    }

    public static void drawFixedTab(DrawContext context, int x, int y,
                                    int width, int height,
                                    boolean hovered, boolean selected, boolean enabled) {
        int background = !enabled ? CONTROL_DISABLED
                : selected ? CONTROL_ACTIVE
                : hovered ? 0xB5363636 : 0x78242424;
        roundedRect(context, x, y, width, height, 2, background);
        border(context, x, y, width, height,
                selected ? ACCENT_MUTED : hovered ? PANEL_BORDER : DIVIDER);
        if (selected && width > 4 && height > 2) {
            context.fill(x + 2, y + height - 2, x + width - 2, y + height - 1, ACCENT);
        }
    }

    public static String fitText(TextRenderer renderer, Text text, int maxWidth) {
        String value = text == null ? "" : text.getString();
		if (maxWidth <= 0) return "";
		if (renderer.getWidth(value) <= maxWidth) return value;
        String ellipsis = "…";
        int contentWidth = Math.max(0, maxWidth - renderer.getWidth(ellipsis));
        return renderer.trimToWidth(value, contentWidth) + ellipsis;
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
        context.fill(x + 1, y, x + width - 1, y + 1, color);
        context.fill(x + 1, y + height - 1, x + width - 1, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }
}
