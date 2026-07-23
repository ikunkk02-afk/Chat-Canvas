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
		assertTrue(MentionConfig.DEFAULT.soundEnabled());
		assertEquals(MentionSound.EXPERIENCE_ORB, MentionConfig.DEFAULT.sound());
		assertEquals(0.8, MentionConfig.DEFAULT.soundVolume());
		assertEquals(1.0, MentionConfig.DEFAULT.soundPitch());
		assertTrue(MentionConfig.DEFAULT.toastEnabled());
		assertFalse(MentionConfig.DEFAULT.flashEnabled());
		assertTrue(MentionConfig.DEFAULT.ignoreOwnMessages());
		assertTrue(MentionConfig.DEFAULT.playerQuickActionsEnabled());

		assertEquals(150, MentionConfig.DEFAULT.withDoubleClickIntervalMs(1)
				.doubleClickIntervalMs());
		assertEquals(600, MentionConfig.DEFAULT.withDoubleClickIntervalMs(999)
				.doubleClickIntervalMs());
		assertEquals(0, MentionConfig.DEFAULT.withHighlightColor(-1).highlightColor());
		assertEquals(0xFFFFFF, MentionConfig.DEFAULT.withHighlightColor(0x1FFFFFF)
				.highlightColor());
		assertEquals(1.0, MentionConfig.DEFAULT.withSoundVolume(10).soundVolume());
		assertEquals(0.5, MentionConfig.DEFAULT.withSoundPitch(0).soundPitch());
		assertEquals(160, MentionConfig.DEFAULT.withToastMessageLength(1_000)
				.toastMessageLength());
		assertEquals(0.6, MentionConfig.DEFAULT.withFlashOpacity(2).flashOpacity());
		assertEquals(1_500, MentionConfig.DEFAULT.withFlashDurationMs(9_000)
				.flashDurationMs());
	}
}
