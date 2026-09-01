package io.github.ikunkk02.chatcanvas.chat.identity;

import io.github.ikunkk02.chatcanvas.chat.mention.MentionMatcher;
import io.github.ikunkk02.chatcanvas.chat.style.TextRange;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerColorConfig;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.util.FormattedCharSequence;

public final class ChatMessageMetadataRegistry {
	public static final int CAPACITY = 1024;
	private static final int PENDING_CAPACITY = 128;
	private static final ChatMessageMetadataRegistry INSTANCE = new ChatMessageMetadataRegistry();

	private final LinkedHashMap<MessageSignature, ChatMessageMetadata> signatures =
			new LinkedHashMap<>(64, 0.75f, true);
	private final IdentityHashMap<Component, Deque<ChatMessageMetadata>> pendingByText =
			new IdentityHashMap<>();
	private final Deque<Component> pendingOrder = new ArrayDeque<>();
	private final IdentityHashMap<GuiMessage, ChatMessageMetadata> messages =
			new IdentityHashMap<>();
	private final IdentityHashMap<GuiMessage, MentionAnalysis> mentions =
			new IdentityHashMap<>();
	private final Deque<GuiMessage> mentionOrder = new ArrayDeque<>();
	private final IdentityHashMap<FormattedCharSequence, VisibleMetadata> visible =
			new IdentityHashMap<>();

	private ChatMessageMetadataRegistry() {
	}

	public static ChatMessageMetadataRegistry instance() {
		return INSTANCE;
	}

	public synchronized void registerIncoming(Component text, MessageSignature signature,
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

	public synchronized void registerVisibleLines(GuiMessage message, List<FormattedCharSequence> lines) {
		ChatMessageMetadata sender = metadataFor(message);
		String plain = message.content().getString();
		MentionKey key = currentMentionKey();
		MentionAnalysis mentionAnalysis = mentions.get(message);
		if (mentionAnalysis == null || !mentionAnalysis.key().equals(key)) {
			mentionAnalysis = new MentionAnalysis(
					key,
					MentionMatcher.findMentions(plain, key.playerName(), key.requireAtSymbol()));
			if (!mentions.containsKey(message)) mentionOrder.addLast(message);
			mentions.put(message, mentionAnalysis);
			while (mentionOrder.size() > CAPACITY) {
				GuiMessage oldest = mentionOrder.pollFirst();
				if (oldest != null) mentions.remove(oldest);
			}
		}
		List<LineIndex> indices = mapLines(plain, lines);
		for (int index = 0; index < lines.size(); index++) {
			LineIndex lineIndex = indices.get(index);
			TextRange senderRange = sender == null
					? null
					: lineIndex.localRange(sender.nameStart(), sender.nameEnd());
			List<TextRange> mentionRanges = lineIndex.localRanges(mentionAnalysis.ranges());
			if (senderRange != null || !mentionRanges.isEmpty()) {
				visible.put(lines.get(index), new VisibleMetadata(
						sender == null ? null : sender.sender(), senderRange, mentionRanges));
			}
		}
	}

	public synchronized VisibleMetadata visibleMetadata(FormattedCharSequence line) {
		return visible.get(line);
	}

	public synchronized void clearVisible() {
		visible.clear();
	}

	public synchronized void retainMessages(Collection<GuiMessage> retained) {
		IdentityHashMap<GuiMessage, Boolean> live = new IdentityHashMap<>();
		for (GuiMessage line : retained) live.put(line, Boolean.TRUE);
		messages.keySet().removeIf(line -> !live.containsKey(line));
		mentions.keySet().removeIf(line -> !live.containsKey(line));
		mentionOrder.removeIf(line -> !live.containsKey(line));

		java.util.HashSet<MessageSignature> liveSignatures = new java.util.HashSet<>();
		for (GuiMessage line : retained) {
			if (line.signature() != null) liveSignatures.add(line.signature());
		}
		signatures.keySet().removeIf(signature -> !liveSignatures.contains(signature));
	}

	public synchronized void clearAll() {
		signatures.clear();
		pendingByText.clear();
		pendingOrder.clear();
		messages.clear();
		mentions.clear();
		mentionOrder.clear();
		visible.clear();
	}

	private ChatMessageMetadata metadataFor(GuiMessage line) {
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

	private void trimSignatures() {
		Iterator<Map.Entry<MessageSignature, ChatMessageMetadata>> iterator =
				signatures.entrySet().iterator();
		while (signatures.size() > CAPACITY && iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	private void removeOldestPending() {
		Component oldest = pendingOrder.pollFirst();
		if (oldest == null) return;
		Deque<ChatMessageMetadata> queue = pendingByText.get(oldest);
		if (queue != null) {
			queue.pollFirst();
			if (queue.isEmpty()) pendingByText.remove(oldest);
		}
	}

	private void removePendingOrderReference(Component text) {
		Iterator<Component> iterator = pendingOrder.iterator();
		while (iterator.hasNext()) {
			if (iterator.next() == text) {
				iterator.remove();
				return;
			}
		}
	}

	private static MentionKey currentMentionKey() {
		Minecraft client = Minecraft.getInstance();
		String playerName = client.player == null
				? ""
				: client.player.getGameProfile().getName();
		return new MentionKey(
				PlayerColorConfig.normalizeName(playerName),
				ChatCanvasConfig.instance().mention().requireAtSymbol());
	}

	private static List<LineIndex> mapLines(String source, List<FormattedCharSequence> lines) {
		int[] sourceCodePoints = source.codePoints().toArray();
		int[] cursor = {0};
		List<LineIndex> result = new java.util.ArrayList<>(lines.size());
		for (FormattedCharSequence line : lines) {
			List<Integer> globalIndices = new java.util.ArrayList<>();
			line.accept((ignored, style, codePoint) -> {
				int mapped = findNextSourceIndex(sourceCodePoints, cursor[0], codePoint);
				if (mapped >= 0) {
					globalIndices.add(mapped);
					cursor[0] = mapped + 1;
				} else {
					globalIndices.add(-1);
				}
				return true;
			});
			result.add(new LineIndex(globalIndices.stream().mapToInt(Integer::intValue).toArray()));
		}
		return result;
	}

	private static int findNextSourceIndex(int[] source, int from, int codePoint) {
		if (from < source.length && source[from] == codePoint) return from;
		int limit = Math.min(source.length, from + 32);
		for (int index = from; index < limit; index++) {
			int candidate = source[index];
			if (candidate == codePoint) return index;
			if (candidate != '\n' && candidate != '\r' && !Character.isWhitespace(candidate)) {
				break;
			}
		}
		return -1;
	}

	public record VisibleMetadata(
			PlayerChatIdentity sender,
			TextRange playerNameRange,
			List<TextRange> mentionRanges
	) {
		public VisibleMetadata {
			mentionRanges = mentionRanges == null ? List.of() : List.copyOf(mentionRanges);
		}
	}

	private record MentionKey(String playerName, boolean requireAtSymbol) {
	}

	private record MentionAnalysis(MentionKey key, List<TextRange> ranges) {
	}

	private record LineIndex(int[] globalCodePointIndices) {
		private TextRange localRange(int globalStart, int globalEnd) {
			int first = -1;
			int last = -1;
			for (int local = 0; local < globalCodePointIndices.length; local++) {
				int global = globalCodePointIndices[local];
				if (global >= globalStart && global < globalEnd) {
					if (first < 0) first = local;
					last = local + 1;
				}
			}
			return first < 0 ? null : new TextRange(first, last);
		}

		private List<TextRange> localRanges(List<TextRange> globalRanges) {
			if (globalRanges == null || globalRanges.isEmpty()) return List.of();
			List<TextRange> result = new java.util.ArrayList<>();
			for (TextRange range : globalRanges) {
				TextRange local = localRange(range.startCodePoint(), range.endCodePoint());
				if (local != null) result.add(local);
			}
			return List.copyOf(result);
		}
	}
}
