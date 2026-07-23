package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.config.LayoutConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class EditorHistory {
	private static final int DEFAULT_CAPACITY = 100;

	private final int capacity;
	private final List<LayoutConfig> entries = new ArrayList<>();
	private int cursor;

	public EditorHistory(LayoutConfig initial) {
		this(initial, DEFAULT_CAPACITY);
	}

	public EditorHistory(LayoutConfig initial, int capacity) {
		this.capacity = Math.max(2, capacity);
		entries.add(initial.sanitized());
		cursor = 0;
	}

	public void record(LayoutConfig state) {
		LayoutConfig sanitized = state.sanitized();
		if (entries.get(cursor).equals(sanitized)) {
			return;
		}
		while (entries.size() > cursor + 1) {
			entries.remove(entries.size() - 1);
		}
		entries.add(sanitized);
		cursor++;
		if (entries.size() > capacity) {
			entries.remove(0);
			cursor--;
		}
	}

	public Optional<LayoutConfig> undo() {
		if (!canUndo()) {
			return Optional.empty();
		}
		cursor--;
		return Optional.of(entries.get(cursor));
	}

	public Optional<LayoutConfig> redo() {
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

	public LayoutConfig current() {
		return entries.get(cursor);
	}

	public int size() {
		return entries.size();
	}
}
