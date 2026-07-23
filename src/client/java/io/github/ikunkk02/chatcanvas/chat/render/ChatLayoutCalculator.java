package io.github.ikunkk02.chatcanvas.chat.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;

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
			for (OrderedText line : renderer.wrapLines(message.text(), safeWidth)) {
				cachedLines.add(new ChatLine(line, renderer.getWidth(line)));
			}
		}
		return cachedLines;
	}

	public void invalidate() {
		cachedWidth = -1;
		cachedMessages = List.of();
	}

	public record ChatLine(OrderedText text, int width) {
	}
}
