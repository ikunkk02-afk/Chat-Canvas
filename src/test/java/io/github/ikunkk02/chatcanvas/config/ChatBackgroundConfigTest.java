package io.github.ikunkk02.chatcanvas.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ChatBackgroundConfigTest {
	@Test
	void sanitizesEveryFieldIndependently() {
		ChatBackgroundConfig safe = new ChatBackgroundConfig(
				null,
				-20,
				Double.NaN,
				99,
				-4,
				0x1FFFFFF,
				Double.POSITIVE_INFINITY,
				false,
				-1,
				-5.0
		).sanitized();

		assertEquals(MessageBackgroundMode.FOLLOW_TEXT, safe.messageMode());
		assertEquals(0x000000, safe.messageColor());
		assertEquals(ChatBackgroundConfig.DEFAULT.messageOpacity(), safe.messageOpacity());
		assertEquals(12, safe.horizontalPadding());
		assertEquals(0, safe.verticalPadding());
		assertEquals(0xFFFFFF, safe.inputColor());
		assertEquals(ChatBackgroundConfig.DEFAULT.inputOpacity(), safe.inputOpacity());
		assertFalse(safe.inputBorderEnabled());
		assertEquals(0x000000, safe.inputBorderColor());
		assertEquals(0.0, safe.inputBorderOpacity());
	}
}
