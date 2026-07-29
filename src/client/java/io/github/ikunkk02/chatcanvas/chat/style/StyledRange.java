package io.github.ikunkk02.chatcanvas.chat.style;

import net.minecraft.text.Style;

import java.util.function.UnaryOperator;

public record StyledRange(TextRange range, int priority, UnaryOperator<Style> overlay) {
	public StyledRange {
		if (range == null) throw new IllegalArgumentException("range");
		if (overlay == null) throw new IllegalArgumentException("overlay");
	}
}
