package io.github.ikunkk02.chatcanvas.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class EditorHistory {
	private static final int DEFAULT_CAPACITY = 100;

	private final int capacity;
	private final List<EditorSnapshot> entries = new ArrayList<>();
	private int cursor;

	public EditorHistory(EditorSnapshot initial) {
		this(initial, DEFAULT_CAPACITY);
	}

	public EditorHistory(EditorSnapshot initial, int capacity) {
		this.capacity = Math.max(2, capacity);
		entries.add(initial);
		cursor = 0;
	}

	public void record(EditorSnapshot state) {
		if (entries.get(cursor).equals(state)) {
			return;
		}
		while (entries.size() > cursor + 1) {
			entries.remove(entries.size() - 1);
		}
		entries.add(state);
		cursor++;
		if (entries.size() > capacity) {
			entries.remove(0);
			cursor--;
		}
	}

	public Optional<EditorSnapshot> undo() {
		if (!canUndo()) {
			return Optional.empty();
		}
		cursor--;
		return Optional.of(entries.get(cursor));
	}

	public Optional<EditorSnapshot> redo() {
		if (!canRedo()) {
			return Optional.empty();
		}
		cursor++;
		return Optional.of(entries.get(cursor));
	}

	public boolean canUndo() {
		return cursor > 0;
	}

	public boolean canRedo() {
		return cursor + 1 < entries.size();
	}

	public EditorSnapshot current() {
		return entries.get(cursor);
	}

	public int size() {
		return entries.size();
	}
}
