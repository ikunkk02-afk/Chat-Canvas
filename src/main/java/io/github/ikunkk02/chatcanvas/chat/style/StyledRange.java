package io.github.ikunkk02.chatcanvas.chat.style;

import java.util.function.UnaryOperator;
import net.minecraft.network.chat.Style;

public record StyledRange(TextRange range, int priority, UnaryOperator<Style> overlay) {
	public StyledRange {
		if (range == null) throw new IllegalArgumentException("range");
		if (overlay == null) throw new IllegalArgumentException("overlay");
	}
}
