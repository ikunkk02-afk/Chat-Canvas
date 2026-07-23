package io.github.ikunkk02.chatcanvas.chat.render;

import net.minecraft.text.OrderedText;

public final class PlayerColoredOrderedText {
	private PlayerColoredOrderedText() {
	}

	public static OrderedText colorRange(OrderedText original, int start, int end, int rgb) {
		return visitor -> {
			int[] offset = {0};
			return original.accept((index, style, codePoint) -> {
				int glyphStart = offset[0];
				int glyphEnd = glyphStart + Character.charCount(codePoint);
				offset[0] = glyphEnd;
				return visitor.accept(index,
						glyphEnd > start && glyphStart < end ? style.withColor(rgb) : style,
						codePoint);
			});
		};
	}

	public static OrderedText selectRange(OrderedText original, int start, int end) {
		return visitor -> {
			int[] offset = {0};
			return original.accept((index, style, codePoint) -> {
				int glyphStart = offset[0];
				int glyphEnd = glyphStart + Character.charCount(codePoint);
				offset[0] = glyphEnd;
				return glyphEnd > start && glyphStart < end
						? visitor.accept(index, style, codePoint)
						: true;
			});
		};
	}
}
