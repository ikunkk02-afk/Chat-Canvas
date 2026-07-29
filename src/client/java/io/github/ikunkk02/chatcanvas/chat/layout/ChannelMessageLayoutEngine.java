package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerIdentityResolver;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerColorRuntime;
import io.github.ikunkk02.chatcanvas.chat.mention.MentionMatcher;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasChannel;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessage;
import io.github.ikunkk02.chatcanvas.chat.style.StyledRangePipeline;
import io.github.ikunkk02.chatcanvas.chat.style.TextIndexing;
import io.github.ikunkk02.chatcanvas.chat.style.TextRange;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextMetrics;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextWrapper;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerChatLayoutMode;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;

public final class ChannelMessageLayoutEngine {
	private static final int MAX_ENTRIES = 1_024;
	private static final ChannelMessageLayoutEngine INSTANCE = new ChannelMessageLayoutEngine();
	private final Map<Key, Layout> cache = new LinkedHashMap<>(128, .75f, true);
	private final StyledRangePipeline styles = new StyledRangePipeline();
	private long resourceEpoch;

	private ChannelMessageLayoutEngine() {}

	public static ChannelMessageLayoutEngine instance() {
		return INSTANCE;
	}

	public synchronized Layout layout(ChatCanvasMessage message, TextRenderer renderer,
									  int availablePixels, ChatTextConfig config,
									  int lineHeight, int messageSpacing, long historyEpoch,
									  PlayerChatLayoutMode layoutMode, double splitRatio,
									  int visualSafetyPixels, double guiScale) {
		int glyphWidth = Math.max(1, (int) Math.floor(availablePixels / config.fontScale()));
		Key key = new Key(message.messageId(), message.channel(), glyphWidth,
				layoutMode == null ? PlayerChatLayoutMode.CLASSIC : layoutMode,
				Double.doubleToLongBits(splitRatio),
				Double.doubleToLongBits(config.fontScale()),
				Double.doubleToLongBits(config.characterSpacing()),
				Double.doubleToLongBits(config.lineSpacing()),
				lineHeight, messageSpacing, Math.max(0, visualSafetyPixels),
				Double.doubleToLongBits(guiScale), historyEpoch, resourceEpoch);
		Layout cached = cache.get(key);
		if (cached != null) return cached;
		StyledMessage styled = styled(message);
		List<OrderedText> wrapped = SpacedTextWrapper.wrap(
				renderer, List.of(styled.text()), glyphWidth, config.characterSpacing());
		List<Line> lines = mapLines(
				wrapped, message.content().getString(), styled.playerNameRange(),
				renderer, config);
		int width = 0;
		for (Line line : lines) {
			width = Math.max(width, line.width());
		}
		Layout result = new Layout(lines, width,
				lines.size() * lineHeight + Math.max(0, messageSpacing));
		cache.put(key, result);
		trim();
		return result;
	}

	public synchronized void invalidateResources() {
		resourceEpoch++;
		cache.clear();
	}

	public synchronized void clearWorld() {
		cache.clear();
	}

	public synchronized void invalidateChannel(ChatCanvasChannel channel) {
		cache.keySet().removeIf(key -> key.channel() == channel);
	}

	public synchronized int cacheSize(ChatCanvasChannel channel) {
		return (int) cache.keySet().stream()
				.filter(key -> key.channel() == channel)
				.count();
	}

	private StyledMessage styled(ChatCanvasMessage message) {
		OrderedText original = message.content().asOrderedText();
		if (message.channel() != ChatCanvasChannel.PLAYER_CHAT) {
			return new StyledMessage(original, null);
		}
		String plain = message.content().getString();
		String name = message.senderName() == null ? "" : message.senderName().getString();
		int nameStart = name.isEmpty() ? -1
				: PlayerIdentityResolver.boundedIndexOf(plain, name, 0);
		TextRange nameRange = nameStart < 0 ? null
				: TextIndexing.utf16RangeToCodePoints(
						plain, nameStart, nameStart + name.length());
		OptionalInt color = nameRange == null ? OptionalInt.empty()
				: PlayerColorRuntime.provider().colorFor(new PlayerChatIdentity(
				message.senderUuid(), name, true));
		List<TextRange> mentions = MentionMatcher.findMentions(
				plain,
				net.minecraft.client.Minecraft.getInstance().player == null ? ""
						: net.minecraft.client.Minecraft.getInstance().player
						.getGameProfile().getName(),
				ChatCanvasConfig.instance().mention().requireAtSymbol());
		return new StyledMessage(
				styles.apply(original, nameRange, color, mentions,
						ChatCanvasConfig.instance().mention()),
				nameRange);
	}

	private static List<Line> mapLines(
			List<OrderedText> wrapped, String plain, @Nullable TextRange nameRange,
			TextRenderer renderer, ChatTextConfig config) {
		int[] source = plain.codePoints().toArray();
		int sourceCursor = 0;
		List<Line> result = new ArrayList<>(wrapped.size());
		for (OrderedText text : wrapped) {
			LineMapping mapping = mapLine(text, source, sourceCursor);
			sourceCursor = Math.max(sourceCursor, mapping.nextSourceIndex());
			int width = (int) Math.ceil(
					SpacedTextMetrics.width(renderer, text, config.characterSpacing())
							* config.fontScale());
			result.add(new Line(text, width,
					nameRange == null ? null : mapping.localRange(nameRange)));
		}
		return List.copyOf(result);
	}

	private static LineMapping mapLine(OrderedText line, int[] source, int sourceCursor) {
		ArrayList<Integer> indices = new ArrayList<>();
		int[] cursor = {sourceCursor};
		line.accept((index, style, codePoint) -> {
			int match = nextSource(source, cursor[0], codePoint);
			indices.add(match);
			if (match >= 0) cursor[0] = match + 1;
			return true;
		});
		return new LineMapping(
				indices.stream().mapToInt(Integer::intValue).toArray(), cursor[0]);
	}

	private static int nextSource(int[] source, int from, int codePoint) {
		if (from < source.length && source[from] == codePoint) return from;
		for (int index = from; index < Math.min(source.length, from + 32); index++) {
			if (source[index] == codePoint) return index;
			if (source[index] != '\n' && source[index] != '\r'
					&& !Character.isWhitespace(source[index])) break;
		}
		return -1;
	}

	private void trim() {
		Iterator<Key> iterator = cache.keySet().iterator();
		while (cache.size() > MAX_ENTRIES && iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	public record Line(OrderedText text, int width, @Nullable TextRange playerNameRange) {}

	public record Layout(List<Line> lines, int width, int height) {}

	private record Key(UUID id, ChatCanvasChannel channel, int width,
					   PlayerChatLayoutMode layoutMode, long splitRatio,
					   long scale, long spacing, long lineSpacing, int lineHeight,
					   int messageSpacing, int visualSafetyPixels, long guiScale,
					   long historyEpoch, long resourceEpoch) {}

	private record StyledMessage(OrderedText text, @Nullable TextRange playerNameRange) {}

	private record LineMapping(int[] globalIndices, int nextSourceIndex) {
		private @Nullable TextRange localRange(TextRange globalRange) {
			int first = -1;
			int last = -1;
			for (int local = 0; local < globalIndices.length; local++) {
				if (globalRange.contains(globalIndices[local])) {
					if (first < 0) first = local;
					last = local + 1;
				}
			}
			return first < 0 ? null : new TextRange(first, last);
		}
	}
}
