package io.github.ikunkk02.chatcanvas.chat.style;

import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import java.util.function.UnaryOperator;
import net.minecraft.network.chat.Style;

public final class MentionStyleProvider {
	public UnaryOperator<Style> overlay(MentionConfig value) {
		MentionConfig config = value == null ? MentionConfig.DEFAULT : value.sanitized();
		return style -> {
			Style result = style.withColor(config.highlightColor());
			return config.highlightBold() ? result.withBold(true) : result;
		};
	}
}
