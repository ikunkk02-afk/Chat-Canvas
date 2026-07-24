package io.github.ikunkk02.chatcanvas.editor;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the full editing session state so it can be transferred between
 * editor screens (modern ↔ vanilla) without losing unsaved changes,
 * undo/redo history, scroll positions, or the active category.
 */
public record EditorScreenState(
        EditorSession session,
        int activeCategoryOrdinal,
        Map<Integer, Double> scrollPositions
) {
    public EditorScreenState(EditorSession session, int activeCategoryOrdinal) {
        this(session, activeCategoryOrdinal, new HashMap<>());
    }

    public EditorScreenState withScrollPosition(int categoryOrdinal, double scrollAmount) {
        Map<Integer, Double> copy = new HashMap<>(scrollPositions);
        copy.put(categoryOrdinal, scrollAmount);
        return new EditorScreenState(session, activeCategoryOrdinal, copy);
    }
}
