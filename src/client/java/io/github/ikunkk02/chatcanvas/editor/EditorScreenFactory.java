package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

/**
 * Central entry point for creating the Chat Canvas editor screen.
 * Always returns the same {@link ChatCanvasEditorScreen} — the UI
 * theme is controlled inside the screen via {@link ModernUiTheme#setStyle}.
 */
public final class EditorScreenFactory {
    private EditorScreenFactory() {}

    public static Screen create(@Nullable Screen parent) {
        return new ChatCanvasEditorScreen(parent);
    }
}
