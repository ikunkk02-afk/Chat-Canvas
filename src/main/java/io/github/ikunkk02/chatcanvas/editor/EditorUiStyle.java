package io.github.ikunkk02.chatcanvas.editor;

/**
 * Determines which visual theme the settings editor uses.
 * <p>
 * This is a UI preference — it does not affect chat rendering,
 * config data, or saved settings.  Switching styles preserves the
 * current editing session including unsaved changes, undo/redo
 * history, and scroll positions.
 */
public enum EditorUiStyle {
    CHAT_CANVAS,
    VANILLA
}
