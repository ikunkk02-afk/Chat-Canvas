package io.github.ikunkk02.chatcanvas.chat.notification;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.UUID;

/**
 * Assigns a stable local ID to one received message object without conflating
 * two distinct message instances that happen to contain identical text.
 */
public final class MentionMessageIdRegistry<T> {
	public static final int DEFAULT_CAPACITY = 512;
	private final int capacity;
	private final IdentityHashMap<T, UUID> ids = new IdentityHashMap<>();
	private final Deque<T> insertionOrder = new ArrayDeque<>();

	public MentionMessageIdRegistry() {
		this(DEFAULT_CAPACITY);
	}

	public MentionMessageIdRegistry(int capacity) {
		this.capacity = Math.max(1, capacity);
	}

	public synchronized UUID idFor(T messageInstance) {
		if (messageInstance == null) throw new IllegalArgumentException("messageInstance");
		UUID existing = ids.get(messageInstance);
		if (existing != null) return existing;

		UUID created = UUID.randomUUID();
		ids.put(messageInstance, created);
		insertionOrder.add(messageInstance);
		while (insertionOrder.size() > capacity) {
			T oldest = insertionOrder.removeFirst();
			ids.remove(oldest);
		}
		return created;
	}

	public synchronized void clear() {
		ids.clear();
		insertionOrder.clear();
	}

	synchronized int size() {
		return ids.size();
	}
}
