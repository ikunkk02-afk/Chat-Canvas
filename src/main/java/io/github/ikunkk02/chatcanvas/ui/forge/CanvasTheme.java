package io.github.ikunkk02.chatcanvas.ui.forge;

/**
 * Theme color definitions for the Canvas UI framework.
 * Two theme presets: DARK and LIGHT.
 */
public record CanvasTheme(
        int background,
        int panelBackground,
        int widgetDefault,
        int widgetHover,
        int widgetActive,
        int textPrimary,
        int textSecondary,
        int accent,
        int border,
        int tabSelected,
        int tabDefault
) {
    public static final CanvasTheme DARK = new CanvasTheme(
            0xCC000000, 0xCC202020, 0xFF404040, 0xFF606060, 0xFF808080,
            0xFFFFFFFF, 0xFFAAAAAA, 0xFF4FC3F7, 0xFF666666,
            0xFF4FC3F7, 0xFF303030
    );

    public static final CanvasTheme LIGHT = new CanvasTheme(
            0xCCFFFFFF, 0xCCF0F0F0, 0xFFCCCCCC, 0xFFAAAAAA, 0xFF888888,
            0xFF222222, 0xFF666666, 0xFF1976D2, 0xFF999999,
            0xFF1976D2, 0xFFDDDDDD
    );
}
