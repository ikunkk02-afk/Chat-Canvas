package io.github.ikunkk02.chatcanvas.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SuggestionWindowPlacementTest {
	@Test
	void placesSuggestionsBelowAFieldNearTheTop() {
		assertEquals(52, SuggestionWindowPlacement.calculateY(40, 12, 120, 360));
	}

	@Test
	void placesSuggestionsAboveAFieldNearTheBottom() {
		assertEquals(228, SuggestionWindowPlacement.calculateY(348, 12, 120, 360));
	}

	@Test
	void clampsOversizedSuggestionsToTheScreen() {
		assertEquals(0, SuggestionWindowPlacement.calculateY(5, 12, 400, 360));
	}
}
