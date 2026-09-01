package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoiceInputOverlayLayoutTest {
	@Test
	void placesStatusBesideMicrophoneWhenRightSideHasRoom() {
		VoiceInputOverlay.StatusLayout layout = VoiceInputOverlay.calculateStatusLayout(
				398, 34, 86, 308, 85, 60);

		assertEquals(330, layout.x());
		assertEquals(85, layout.y());
		assertEquals(60, layout.width());
	}

	@Test
	void keepsFallbackStatusInsideNarrowScreen() {
		VoiceInputOverlay.StatusLayout layout = VoiceInputOverlay.calculateStatusLayout(
				320, 4, 86, 301, 85, 60);

		assertTrue(layout.x() >= 4);
		assertTrue(layout.x() + layout.width() <= 316);
		assertTrue(layout.y() < 85);
	}

	@Test
	void shrinksLongStatusIntoAvailableRightSideSpace() {
		VoiceInputOverlay.StatusLayout layout = VoiceInputOverlay.calculateStatusLayout(
				398, 34, 86, 308, 85, 120);

		assertEquals(330, layout.x());
		assertEquals(64, layout.width());
		assertEquals(85, layout.y());
	}
}
