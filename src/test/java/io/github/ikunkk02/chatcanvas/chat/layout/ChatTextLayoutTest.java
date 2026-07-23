package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.config.ChatTextAlignment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatTextLayoutTest {
	@Test
	void effectiveScaleMultipliesVanillaAndConfiguredValues() {
		assertEquals(1.0, ChatTextLayout.effectiveScale(0.8, 1.25), 0.00001);
	}

	@Test
	void lineSpacingUsesVanillaHeightAndEnforcesMinimum() {
		assertEquals(9, ChatTextLayout.internalLineHeight(9, 1.0));
		assertEquals(18, ChatTextLayout.internalLineHeight(9, 2.0));
		assertEquals(6, ChatTextLayout.internalLineHeight(9, 0.5));
		assertEquals(9, ChatTextLayout.internalLineHeight(18, 0.5));
	}

	@Test
	void alignmentAccountsForRenderedWidthAndIndicatorReservation() {
		assertEquals(0.0, ChatTextLayout.metrics(
				0, 80, 200, 15, ChatTextAlignment.LEFT, 0, 9).drawX(), 0.00001);
		assertEquals(52.5, ChatTextLayout.metrics(
				0, 80, 200, 15, ChatTextAlignment.CENTER, 0, 9).drawX(), 0.00001);
		assertEquals(105.0, ChatTextLayout.metrics(
				0, 80, 200, 15, ChatTextAlignment.RIGHT, 0, 9).drawX(), 0.00001);
	}

	@Test
	void localHitCoordinateUsesTheSamePerLineOffset() {
		ChatLineMetrics metrics = ChatTextLayout.metrics(
				4, 60, 180, 0, ChatTextAlignment.RIGHT, 0, 9);
		assertEquals(12.0, metrics.localX(132.0), 0.00001);
	}

	@Test
	void boundedAlignmentKeepsEveryScaleInsideTheScreenPadding() {
		for (double scale : new double[]{0.5, 1.0, 1.5, 2.0}) {
			double configuredScreenWidth = 360.0;
			double screenPadding = 3.0;
			double vanillaOrigin = 4.0;
			double left = Math.max(0.0, screenPadding / scale - vanillaOrigin);
			double right = configuredScreenWidth / scale - screenPadding / scale - vanillaOrigin;
			ChatLineMetrics metrics = ChatTextLayout.metricsWithin(
					0, 80, left, right, 0, ChatTextAlignment.RIGHT, 0, 9);

			double finalScreenRight = scale * (vanillaOrigin
					+ metrics.drawX() + metrics.renderedWidth());
			assertEquals(configuredScreenWidth - screenPadding, finalScreenRight, 0.00001);
		}
	}

	@Test
	void opacityMultipliesExistingAlphaAndPreservesRgb() {
		assertEquals(0x30123456, ChatTextLayout.multiplyAlpha(0x60123456, 0.5));
		assertEquals(0x06123456, ChatTextLayout.multiplyAlpha(0x3C123456, 0.1));
	}
}
