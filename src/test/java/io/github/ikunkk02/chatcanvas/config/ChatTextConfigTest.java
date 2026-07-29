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
				false,
				99.0
		).sanitized();

		assertEquals(1.0, safe.fontScale(), 0.00001);
		assertEquals(0.5, safe.lineSpacing(), 0.00001);
		assertEquals(1.0, safe.textOpacity(), 0.00001);
		assertEquals(ChatTextAlignment.LEFT, safe.alignment());
		assertEquals(false, safe.shadow());
		assertEquals(6.0, safe.characterSpacing(), 0.00001);
	}

	@Test
	void preservesLegacyDefaultAndClampsNegativeSpacing() {
		assertEquals(0.0, new ChatTextConfig(
				1.0, 1.0, 1.0, ChatTextAlignment.LEFT, true).characterSpacing(), 0.00001);
		assertEquals(-1.0, ChatTextConfig.DEFAULT.withCharacterSpacing(-99.0)
				.characterSpacing(), 0.00001);
	}
}
