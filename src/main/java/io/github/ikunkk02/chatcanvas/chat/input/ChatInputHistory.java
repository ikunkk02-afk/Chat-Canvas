package io.github.ikunkk02.chatcanvas.chat.input;

import java.util.ArrayList;
import java.util.List;

final class ChatInputHistory {
	private static final int DEFAULT_CAPACITY = 100;

	private final int capacity;
	private final List<String> entries = new ArrayList<>();
	private int index;
	private String browsingDraft = "";

	ChatInputHistory() {
		this(DEFAULT_CAPACITY);
	}

	ChatInputHistory(int capacity) {
		this.capacity = Math.max(1, capacity);
	}

	void add(String value) {
		if (value == null || value.isBlank()) return;
		if (entries.isEmpty() || !entries.get(entries.size() - 1).equals(value)) {
			if (entries.size() >= capacity) entries.remove(0);
			entries.add(value);
		}
		resetNavigation();
	}

	ChatInputSnapshot navigate(int offset, ChatInputSnapshot current) {
		if (entries.isEmpty() || offset == 0) return current;
		if (index == entries.size()) browsingDraft = current.text();
		int next = Math.max(0, Math.min(entries.size(), index + offset));
		if (next == index) return current;
		index = next;
		String value = index == entries.size() ? browsingDraft : entries.get(index);
		return ChatInputSnapshot.atEnd(value);
	}

	int index() {
		return index;
	}

	List<String> entries() {
		return List.copyOf(entries);
	}

	void clear() {
		entries.clear();
		browsingDraft = "";
		index = 0;
	}

	void resetNavigation() {
		index = entries.size();
		browsingDraft = "";
	}
}
