package io.github.ikunkk02.chatcanvas.chat.identity;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ChatMessageMetadataRegistry {
	public static final int CAPACITY = 1024;
	private static final int PENDING_CAPACITY = 128;
	private static final ChatMessageMetadataRegistry INSTANCE = new ChatMessageMetadataRegistry();

	private final LinkedHashMap<MessageSignatureData, ChatMessageMetadata> signatures =
			new LinkedHashMap<>(64, 0.75f, true);
	private final IdentityHashMap<Text, Deque<ChatMessageMetadata>> pendingByText =
			new IdentityHashMap<>();
	private final Deque<Text> pendingOrder = new ArrayDeque<>();
	private final IdentityHashMap<ChatHudLine, ChatMessageMetadata> messages =
			new IdentityHashMap<>();
	private final IdentityHashMap<OrderedText, VisibleMetadata> visible =
			new IdentityHashMap<>();

	private ChatMessageMetadataRegistry() {
	}

	public static ChatMessageMetadataRegistry instance() {
		return INSTANCE;
	}

	public synchronized void registerIncoming(Text text, MessageSignatureData signature,
											  ChatMessageMetadata metadata) {
		if (signature != null) {
			signatures.put(signature, metadata);
			trimSignatures();
		}
		pendingByText.computeIfAbsent(text, ignored -> new ArrayDeque<>()).addLast(metadata);
		pendingOrder.addLast(text);
		while (pendingOrder.size() > PENDING_CAPACITY) {
			removeOldestPending();
		}
	}

	public synchronized void registerVisibleLines(ChatHudLine message, List<OrderedText> lines) {
		ChatMessageMetadata metadata = metadataFor(message);
		if (metadata == null) return;
		String playerName = metadata.sender().playerName();
		boolean registered = false;
		for (OrderedText line : lines) {
			if (registered) break;
			Range range = findRange(line, playerName);
			if (range != null) {
				visible.put(line, new VisibleMetadata(metadata.sender(), range.start(), range.end()));
				registered = true;
			}
		}
	}

	public synchronized VisibleMetadata visibleMetadata(OrderedText line) {
		return visible.get(line);
	}

	public synchronized void clearVisible() {
		visible.clear();
	}

	public synchronized void retainMessages(Collection<ChatHudLine> retained) {
		IdentityHashMap<ChatHudLine, Boolean> live = new IdentityHashMap<>();
		for (ChatHudLine line : retained) live.put(line, Boolean.TRUE);
		messages.keySet().removeIf(line -> !live.containsKey(line));

		java.util.HashSet<MessageSignatureData> liveSignatures = new java.util.HashSet<>();
		for (ChatHudLine line : retained) {
			if (line.signature() != null) liveSignatures.add(line.signature());
		}
		signatures.keySet().removeIf(signature -> !liveSignatures.contains(signature));
	}

	public synchronized void clearAll() {
		signatures.clear();
		pendingByText.clear();
		pendingOrder.clear();
		messages.clear();
		visible.clear();
	}

	private ChatMessageMetadata metadataFor(ChatHudLine line) {
		ChatMessageMetadata existing = messages.get(line);
		if (existing != null) return existing;
		ChatMessageMetadata found = line.signature() == null
				? null
				: signatures.get(line.signature());
		if (found != null) {
			Deque<ChatMessageMetadata> queue = pendingByText.get(line.content());
			if (queue != null) {
				queue.pollFirst();
				if (queue.isEmpty()) pendingByText.remove(line.content());
				removePendingOrderReference(line.content());
			}
		}
		if (found == null) {
			Deque<ChatMessageMetadata> queue = pendingByText.get(line.content());
			if (queue != null) {
				found = queue.pollFirst();
				if (queue.isEmpty()) pendingByText.remove(line.content());
				removePendingOrderReference(line.content());
			}
		}
		if (found == null) return null;
		found = PlayerIdentityResolver.revalidate(line.content(), found).orElse(null);
		if (found != null) messages.put(line, found);
		return found;
	}

	private static Range findRange(OrderedText line, String name) {
		StringBuilder plain = new StringBuilder();
		line.accept((index, style, codePoint) -> {
			plain.appendCodePoint(codePoint);
			return true;
		});
		int match = PlayerIdentityResolver.boundedIndexOf(plain.toString(), name, 0);
		return match < 0 ? null : new Range(match, match + name.length());
	}

	private void trimSignatures() {
		Iterator<Map.Entry<MessageSignatureData, ChatMessageMetadata>> iterator =
				signatures.entrySet().iterator();
		while (signatures.size() > CAPACITY && iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	private void removeOldestPending() {
		Text oldest = pendingOrder.pollFirst();
		if (oldest == null) return;
		Deque<ChatMessageMetadata> queue = pendingByText.get(oldest);
		if (queue != null) {
			queue.pollFirst();
			if (queue.isEmpty()) pendingByText.remove(oldest);
		}
	}

	private void removePendingOrderReference(Text text) {
		Iterator<Text> iterator = pendingOrder.iterator();
		while (iterator.hasNext()) {
			if (iterator.next() == text) {
				iterator.remove();
				return;
			}
		}
	}

	public record VisibleMetadata(PlayerChatIdentity sender, int nameStart, int nameEnd) {
	}

	private record Range(int start, int end) {
	}
}
