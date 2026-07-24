package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

/**
 * Central entry point for creating the Chat Canvas editor screen.
 * Reads the player's {@link EditorUiStyle} preference and dispatches
 * to the appropriate implementation.
 */
public final class EditorScreenFactory {
    private EditorScreenFactory() {}

    public static Screen create(@Nullable Screen parent) {
        EditorUiStyle style = ChatCanvasConfig.instance().settings().editorUiStyle();
        return switch (style) {
            case CHAT_CANVAS -> new ChatCanvasEditorScreen(parent);
            case VANILLA -> new VanillaChatCanvasEditorScreen(parent);
        };
    }

    /**
     * Creates an editor screen that continues a session exported from
     * another editor (e.g. switching UI styles mid-edit).
     */
    public static Screen createWithState(@Nullable Screen parent, EditorScreenState state) {
        EditorUiStyle style = ChatCanvasConfig.instance().settings().editorUiStyle();
        return switch (style) {
            case CHAT_CANVAS -> new ChatCanvasEditorScreen(parent, state);
            case VANILLA -> new VanillaChatCanvasEditorScreen(parent, state);
        };
    }
}
