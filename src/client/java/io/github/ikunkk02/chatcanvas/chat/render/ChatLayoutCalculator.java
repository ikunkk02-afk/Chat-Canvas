package io.github.ikunkk02.chatcanvas.chat.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerIdentityResolver;
import io.github.ikunkk02.chatcanvas.chat.mention.MentionMatcher;
import io.github.ikunkk02.chatcanvas.chat.style.TextIndexing;
import io.github.ikunkk02.chatcanvas.chat.style.TextRange;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextMetrics;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextWrapper;
import io.github.ikunkk02.chatcanvas.chat.layout.PlayerChatLayoutStrategies;
import io.github.ikunkk02.chatcanvas.config.PlayerChatLayoutMode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ChatLayoutCalculator {
	private final List<ChatLine> cachedLines = new ArrayList<>();
	private List<PreviewChatMessage> cachedMessages = List.of();
	private int cachedWidth = -1;
	private String cachedMentionName = "";
	private boolean cachedRequireAt;
	private long cachedSpacingBits;
	private PlayerChatLayoutMode cachedLayoutMode = PlayerChatLayoutMode.CLASSIC;
	private long cachedSplitRatioBits;

	public List<ChatLine> calculate(TextRenderer renderer, List<PreviewChatMessage> messages, int width,
									String localPlayerName, boolean requireAtSymbol,
									double characterSpacing,
									PlayerChatLayoutMode layoutMode,
									double splitRatio) {
		int safeWidth = Math.max(1, width);
		PlayerChatLayoutMode safeMode = layoutMode == null
				? PlayerChatLayoutMode.CLASSIC : layoutMode;
		String mentionName = localPlayerName == null ? "" : localPlayerName;
		if (messages == cachedMessages && safeWidth == cachedWidth
				&& mentionName.equals(cachedMentionName) && requireAtSymbol == cachedRequireAt
				&& cachedSpacingBits == Double.doubleToLongBits(characterSpacing)
				&& cachedLayoutMode == safeMode
				&& cachedSplitRatioBits == Double.doubleToLongBits(splitRatio)) {
			return cachedLines;
		}

		cachedMessages = messages;
		cachedWidth = safeWidth;
		cachedMentionName = mentionName;
		cachedRequireAt = requireAtSymbol;
		cachedSpacingBits = Double.doubleToLongBits(characterSpacing);
		cachedLayoutMode = safeMode;
		cachedSplitRatioBits = Double.doubleToLongBits(splitRatio);
		cachedLines.clear();
		for (PreviewChatMessage message : messages) {
			String plain = message.text().getString();
			TextRange globalNameRange = null;
			if (message.sender() != null) {
				int nameStart = PlayerIdentityResolver.boundedIndexOf(
						plain, message.sender().playerName(), 0);
				if (nameStart >= 0) {
					globalNameRange = TextIndexing.utf16RangeToCodePoints(
							plain, nameStart, nameStart + message.sender().playerName().length());
				}
			}
			List<TextRange> globalMentions = MentionMatcher.findMentions(
					plain, mentionName, requireAtSymbol);
			int messageWidth = PlayerChatLayoutStrategies.forMode(safeMode)
					.wrapWidth(safeWidth, 0, splitRatio, message.selfMessage());
			List<OrderedText> wrapped = Math.abs(characterSpacing) < 0.00001
					? renderer.wrapLines(message.text(), messageWidth)
					: SpacedTextWrapper.wrap(
							renderer,
							renderer.wrapLines(message.text(), Integer.MAX_VALUE / 4),
							messageWidth,
							characterSpacing);
			int[] source = plain.codePoints().toArray();
			int sourceCursor = 0;
			for (OrderedText line : wrapped) {
				LineMapping mapping = mapLine(line, source, sourceCursor);
				sourceCursor = Math.max(sourceCursor, mapping.nextSourceIndex());
				TextRange nameRange = globalNameRange == null
						? null
						: mapping.localRange(globalNameRange);
				cachedLines.add(new ChatLine(
						line,
						SpacedTextMetrics.width(renderer, line, characterSpacing),
						nameRange == null ? null : message.sender(),
						nameRange,
						mapping.localRanges(globalMentions),
						message.selfMessage()
				));
			}
		}
		return cachedLines;
	}

	public void invalidate() {
		cachedWidth = -1;
		cachedMessages = List.of();
		cachedMentionName = "";
	}

	private static LineMapping mapLine(OrderedText line, int[] source, int sourceCursor) {
		java.util.ArrayList<Integer> indices = new java.util.ArrayList<>();
		int[] cursor = {sourceCursor};
		line.accept((index, style, codePoint) -> {
			int match = nextSource(source, cursor[0], codePoint);
			indices.add(match);
			if (match >= 0) cursor[0] = match + 1;
			return true;
		});
		return new LineMapping(indices.stream().mapToInt(Integer::intValue).toArray(), cursor[0]);
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

	public record ChatLine(
			OrderedText text,
			int width,
			@Nullable PlayerChatIdentity sender,
			@Nullable TextRange playerNameRange,
			List<TextRange> mentionRanges,
			boolean selfMessage
	) {
		public ChatLine {
			mentionRanges = mentionRanges == null ? List.of() : List.copyOf(mentionRanges);
		}
	}

	private record LineMapping(int[] globalIndices, int nextSourceIndex) {
		private TextRange localRange(TextRange globalRange) {
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

		private List<TextRange> localRanges(List<TextRange> ranges) {
			List<TextRange> result = new ArrayList<>();
			for (TextRange range : ranges) {
				TextRange local = localRange(range);
				if (local != null) result.add(local);
			}
			return List.copyOf(result);
		}
	}
}
