package io.github.ikunkk02.chatcanvas.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MentionConfigTest {
	@Test
	void defaultsAndRangesMatchContract() {
		assertEquals(350, MentionConfig.DEFAULT.doubleClickIntervalMs());
		assertEquals(0xFF4FD8, MentionConfig.DEFAULT.highlightColor());
		assertTrue(MentionConfig.DEFAULT.doubleClickEnabled());
		assertTrue(MentionConfig.DEFAULT.highlightEnabled());
		assertTrue(MentionConfig.DEFAULT.highlightBold());
		assertTrue(MentionConfig.DEFAULT.requireAtSymbol());

		assertEquals(150, MentionConfig.DEFAULT.withDoubleClickIntervalMs(1)
				.doubleClickIntervalMs());
		assertEquals(600, MentionConfig.DEFAULT.withDoubleClickIntervalMs(999)
				.doubleClickIntervalMs());
		assertEquals(0, MentionConfig.DEFAULT.withHighlightColor(-1).highlightColor());
		assertEquals(0xFFFFFF, MentionConfig.DEFAULT.withHighlightColor(0x1FFFFFF)
				.highlightColor());
	}
}
