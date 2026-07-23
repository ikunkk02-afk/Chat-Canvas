package io.github.ikunkk02.chatcanvas.chat.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerIdentityResolver;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ChatLayoutCalculator {
	private final List<ChatLine> cachedLines = new ArrayList<>();
	private List<PreviewChatMessage> cachedMessages = List.of();
	private int cachedWidth = -1;

	public List<ChatLine> calculate(TextRenderer renderer, List<PreviewChatMessage> messages, int width) {
		int safeWidth = Math.max(1, width);
		if (messages == cachedMessages && safeWidth == cachedWidth) {
			return cachedLines;
		}

		cachedMessages = messages;
		cachedWidth = safeWidth;
		cachedLines.clear();
		for (PreviewChatMessage message : messages) {
			boolean foundName = false;
			for (OrderedText line : renderer.wrapLines(message.text(), safeWidth)) {
				Range range = !foundName && message.sender() != null
						? findRange(line, message.sender().playerName())
						: null;
				if (range != null) foundName = true;
				cachedLines.add(new ChatLine(
						line,
						renderer.getWidth(line),
						range == null ? null : message.sender(),
						range == null ? -1 : range.start(),
						range == null ? -1 : range.end()
				));
			}
		}
		return cachedLines;
	}

	public void invalidate() {
		cachedWidth = -1;
		cachedMessages = List.of();
	}

	private static Range findRange(OrderedText line, String name) {
		StringBuilder plain = new StringBuilder();
		java.util.ArrayList<Integer> indices = new java.util.ArrayList<>();
		java.util.ArrayList<Integer> offsets = new java.util.ArrayList<>();
		line.accept((index, style, codePoint) -> {
			offsets.add(plain.length());
			indices.add(index);
			plain.appendCodePoint(codePoint);
			return true;
		});
		int match = PlayerIdentityResolver.boundedIndexOf(plain.toString(), name, 0);
		if (match < 0) return null;
		int end = match + name.length();
		int first = -1;
		int last = -1;
		for (int i = 0; i < offsets.size(); i++) {
			int glyphStart = offsets.get(i);
			int glyphEnd = i + 1 < offsets.size() ? offsets.get(i + 1) : plain.length();
			if (glyphEnd > match && glyphStart < end) {
				if (first < 0) first = indices.get(i);
				last = indices.get(i) + glyphEnd - glyphStart;
			}
		}
		return first < 0 ? null : new Range(first, last);
	}

	public record ChatLine(
			OrderedText text,
			int width,
			@Nullable PlayerChatIdentity sender,
			int nameStart,
			int nameEnd
	) {
	}

	private record Range(int start, int end) {
	}
}
