package io.github.ikunkk02.chatcanvas.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatTextConfigTest {
	@Test
	void sanitizesNonFiniteAndOutOfRangeValuesPerField() {
		ChatTextConfig safe = new ChatTextConfig(
				Double.POSITIVE_INFINITY,
				-3.0,
				Double.NaN,
				null,
				false
		).sanitized();

		assertEquals(1.0, safe.fontScale(), 0.00001);
		assertEquals(0.5, safe.lineSpacing(), 0.00001);
		assertEquals(1.0, safe.textOpacity(), 0.00001);
		assertEquals(ChatTextAlignment.LEFT, safe.alignment());
		assertEquals(false, safe.shadow());
	}
}
