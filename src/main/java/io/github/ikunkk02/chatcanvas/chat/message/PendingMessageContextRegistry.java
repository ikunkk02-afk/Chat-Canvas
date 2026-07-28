package io.github.ikunkk02.chatcanvas.chat.message;

import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class PendingMessageContextRegistry {
	private static final int CAPACITY = 256;
	private static final long UNSIGNED_TTL_MS = 5_000L;
	private final LinkedHashMap<MessageSignature, PendingMessage> signatures =
			new LinkedHashMap<>();
	private final IdentityHashMap<Component, Deque<PendingMessage>> identities =
			new IdentityHashMap<>();
	private final Deque<Component> order = new ArrayDeque<>();
	private final Deque<PendingMessage> unsignedOrder = new ArrayDeque<>();

	public synchronized PendingMessage register(
			Component message, MessageSignature signature, MessageContext context) {
		if (message == null) throw new IllegalArgumentException("message");
		UUID id = signature == null
				? UUID.randomUUID()
				: UUID.nameUUIDFromBytes(signature.bytes());
		PendingMessage pending = new PendingMessage(
				id, context, message.getString(), System.currentTimeMillis());
		if (signature != null) {
			signatures.put(signature, pending);
			trimSignatures();
		}
		identities.computeIfAbsent(message, ignored -> new ArrayDeque<>()).add(pending);
		order.add(message);
		if (signature == null) {
			unsignedOrder.add(pending);
			while (unsignedOrder.size() > CAPACITY) unsignedOrder.remove(0);
		}
		while (order.size() > CAPACITY) removeOldest();
		return pending;
	}

	public synchronized PendingMessage consume(Component message, MessageSignature signature) {
		PendingMessage pending = signature == null ? null : signatures.remove(signature);
		Deque<PendingMessage> queue = identities.get(message);
		if (pending != null && queue != null) {
			queue.removeFirstOccurrence(pending);
		} else if (pending == null && queue != null) {
			pending = queue.pollFirst();
		}
		if (pending == null && signature == null) {
			pending = consumeUnsigned(message.getString(), System.currentTimeMillis());
		}
		if (queue != null && queue.isEmpty()) identities.remove(message);
		if (pending != null) {
			unsignedOrder.removeFirstOccurrence(pending);
			removeOneOrderReference(message);
		}
		return pending;
	}

	public synchronized void clear() {
		signatures.clear();
		identities.clear();
		order.clear();
		unsignedOrder.clear();
	}

	private void trimSignatures() {
		Iterator<Map.Entry<MessageSignature, PendingMessage>> iterator =
				signatures.entrySet().iterator();
		while (signatures.size() > CAPACITY && iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	private void removeOldest() {
		Component oldest = order.pollFirst();
		if (oldest == null) return;
		Deque<PendingMessage> queue = identities.get(oldest);
		if (queue == null) return;
		PendingMessage removed = queue.pollFirst();
		if (queue.isEmpty()) identities.remove(oldest);
		if (removed != null) {
			signatures.values().removeIf(value -> value == removed);
			unsignedOrder.removeFirstOccurrence(removed);
		}
	}

	private void removeOneOrderReference(Component message) {
		Iterator<Component> iterator = order.iterator();
		while (iterator.hasNext()) {
			if (iterator.next() == message) {
				iterator.remove();
				return;
			}
		}
	}

	private PendingMessage consumeUnsigned(String plainText, long nowMs) {
		while (!unsignedOrder.isEmpty()
				&& nowMs - unsignedOrder.peekFirst().registeredAtMs() > UNSIGNED_TTL_MS) {
			unsignedOrder.remove(0);
		}
		for (Iterator<PendingMessage> iterator = unsignedOrder.iterator(); iterator.hasNext();) {
			PendingMessage candidate = iterator.next();
			if (candidate.plainText().equals(plainText)) {
				iterator.remove();
				return candidate;
			}
		}
		return null;
	}

	public record PendingMessage(
			UUID messageId,
			MessageContext context,
			String plainText,
			long registeredAtMs
	) {
	}
}
