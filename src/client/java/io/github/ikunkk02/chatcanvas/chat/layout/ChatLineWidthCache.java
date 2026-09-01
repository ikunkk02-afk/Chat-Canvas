package io.github.ikunkk02.chatcanvas.chat.layout;

import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextMetrics;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ChatLineWidthCache {
	private static final int MAX_CACHED_LINES = 512;
	private static final Map<FormattedCharSequence, CachedWidth> WIDTHS = new IdentityHashMap<>();

	private ChatLineWidthCache() {
	}

	public static int width(Font renderer, FormattedCharSequence text) {
		return width(renderer, text, 0.0);
	}

	public static int width(Font renderer, FormattedCharSequence text, double spacing) {
		if (WIDTHS.size() >= MAX_CACHED_LINES && !WIDTHS.containsKey(text)) {
			WIDTHS.clear();
		}
		long spacingBits = Double.doubleToLongBits(spacing);
		CachedWidth cached = WIDTHS.get(text);
		if (cached != null && cached.spacingBits() == spacingBits) return cached.width();
		int width = SpacedTextMetrics.width(renderer, text, spacing);
		WIDTHS.put(text, new CachedWidth(spacingBits, width));
		return width;
	}

	public static void clear() {
		WIDTHS.clear();
	}

	private record CachedWidth(long spacingBits, int width) {
	}
}
