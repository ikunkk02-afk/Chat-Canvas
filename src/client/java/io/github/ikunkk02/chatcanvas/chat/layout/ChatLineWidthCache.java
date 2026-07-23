package io.github.ikunkk02.chatcanvas.chat.layout;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ChatLineWidthCache {
	private static final int MAX_CACHED_LINES = 512;
	private static final Map<OrderedText, Integer> WIDTHS = new IdentityHashMap<>();

	private ChatLineWidthCache() {
	}

	public static int width(TextRenderer renderer, OrderedText text) {
		if (WIDTHS.size() >= MAX_CACHED_LINES && !WIDTHS.containsKey(text)) {
			WIDTHS.clear();
		}
		return WIDTHS.computeIfAbsent(text, renderer::getWidth);
	}

	public static void clear() {
		WIDTHS.clear();
	}
}
