package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.config.PlayerChatLayoutMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerChatLayoutStrategyTest {
	@Test
	void classicUsesFullWidthAndAlwaysStartsOnTheLeft() {
		PlayerChatLayoutStrategy strategy =
				PlayerChatLayoutStrategies.forMode(PlayerChatLayoutMode.CLASSIC);
		assertEquals(180, strategy.wrapWidth(192, 12, .75, false));
		assertEquals(180, strategy.wrapWidth(192, 12, .75, true));
		assertEquals(22, strategy.textX(10, 202, 80, 12, true));
		assertEquals(10, strategy.headX(10, 22, 12, true));
		assertTrue(strategy.reserveHead(true));
	}

	@Test
	void splitLimitsBothSidesAndAlignsEachSelfLineIndependently() {
		PlayerChatLayoutStrategy strategy =
				PlayerChatLayoutStrategies.forMode(PlayerChatLayoutMode.SPLIT_ALIGNMENT);
		assertEquals(132, strategy.wrapWidth(192, 12, .75, true));
		assertEquals(132, strategy.wrapWidth(192, 12, .75, false));
		assertEquals(122, strategy.textX(10, 202, 80, 0, true));
		assertEquals(152, strategy.textX(10, 202, 50, 0, true));
		assertEquals(22, strategy.textX(10, 202, 80, 12, false));
		assertEquals(110, strategy.headX(10, 122, 12, true));
		assertEquals(10, strategy.headX(10, 22, 12, false));
		assertTrue(strategy.reserveHead(true));
		assertTrue(strategy.reserveHead(false));
	}

	@Test
	void splitRatioIsClampedAtBothEnds() {
		PlayerChatLayoutStrategy strategy =
				PlayerChatLayoutStrategies.forMode(PlayerChatLayoutMode.SPLIT_ALIGNMENT);
		assertEquals(100, strategy.wrapWidth(200, 0, .25, true));
		assertEquals(200, strategy.wrapWidth(200, 0, 2.0, true));
	}
}
