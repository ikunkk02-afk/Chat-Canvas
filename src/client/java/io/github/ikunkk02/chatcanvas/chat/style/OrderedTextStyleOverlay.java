package io.github.ikunkk02.chatcanvas.chat.style;

import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;

import java.util.Comparator;
import java.util.List;

public final class OrderedTextStyleOverlay {
	private OrderedTextStyleOverlay() {
	}

	public static OrderedText apply(OrderedText original, List<StyledRange> ranges) {
		if (original == null || ranges == null || ranges.isEmpty()) return original;
		List<StyledRange> ordered = ranges.stream()
				.sorted(Comparator.comparingInt(StyledRange::priority))
				.toList();
		return visitor -> {
			int[] codePointIndex = {0};
			return original.accept((index, style, codePoint) -> {
				Style result = style;
				for (StyledRange range : ordered) {
					if (range.range().contains(codePointIndex[0])) {
						result = range.overlay().apply(result);
					}
				}
				codePointIndex[0]++;
				return visitor.accept(index, result, codePoint);
			});
		};
	}

	public static OrderedText selectRange(OrderedText original, TextRange range) {
		return visitor -> {
			int[] codePointIndex = {0};
			return original.accept((index, style, codePoint) -> {
				boolean selected = range.contains(codePointIndex[0]++);
				return !selected || visitor.accept(index, style, codePoint);
			});
		};
	}
}
