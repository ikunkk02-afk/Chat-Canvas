package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.config.MessageBackgroundMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChatBackgroundMetricsTest {
	@Test
	void alphaCompositionMultipliesOpacitiesAndPreservesRgb() {
		assertEquals(0x30123456, ChatBackgroundMetrics.composeBackgroundColor(
				0x123456, 0.5, 96.0 / 255.0));
		assertEquals(0x00123456, ChatBackgroundMetrics.composeBackgroundColor(
				0x123456, 0.0, 1.0));
		assertEquals(0x60123456, ChatBackgroundMetrics.composeBackgroundColor(
				0x123456, 1.0, 96.0 / 255.0));
	}

	@Test
	void alphaCompositionHandlesNonFiniteValuesDeterministically() {
		assertEquals(0x00123456, ChatBackgroundMetrics.composeBackgroundColor(
				0x123456, Double.NaN, 1.0));
		assertEquals(0xFF123456, ChatBackgroundMetrics.composeBackgroundColor(
				0x123456, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
		assertEquals(0x00123456, ChatBackgroundMetrics.composeBackgroundColor(
				0x123456, 1.0, Double.NEGATIVE_INFINITY));
	}

	@Test
	void followTextUsesAlignedWidthAndPadding() {
		ChatBackgroundBounds bounds = ChatBackgroundMetrics.messageBounds(
				MessageBackgroundMode.FOLLOW_TEXT,
				0, 200, 50, 40,
				0, 9, 12,
				2, 1, 1.0
		);

		assertEquals(new ChatBackgroundBounds(48, -1, 92, 10), bounds);
	}

	@Test
	void fullWidthIgnoresRenderedLineWidthAndHiddenSkipsDrawing() {
		assertEquals(
				new ChatBackgroundBounds(-4, 0, 196, 8),
				ChatBackgroundMetrics.messageBounds(
						MessageBackgroundMode.FULL_WIDTH,
						-4, 196, 70, 20,
						0, 9, 9,
						12, 0, 1.0
				)
		);
		assertNull(ChatBackgroundMetrics.messageBounds(
				MessageBackgroundMode.HIDDEN,
				0, 200, 20, 40,
				0, 9, 9,
				2, 1, 1.0
		));
	}

	@Test
	void lowLineSpacingCapsBackgroundHeightAndLeavesAGap() {
		ChatBackgroundBounds bounds = ChatBackgroundMetrics.messageBounds(
				MessageBackgroundMode.FOLLOW_TEXT,
				0, 200, 10, 30,
				0, 9, 6,
				2, 6, 1.0
		);

		assertEquals(5, bounds.height());
		assertEquals(1, 6 - bounds.height());
	}

	@Test
	void logicalPaddingScalesToInternalCoordinatesAndWrapWidth() {
		ChatBackgroundBounds bounds = ChatBackgroundMetrics.messageBounds(
				MessageBackgroundMode.FOLLOW_TEXT,
				0, 100, 20, 20,
				0, 9, 12,
				4, 2, 2.0
		);

		assertEquals(18, bounds.left());
		assertEquals(42, bounds.right());
		assertEquals(96, ChatBackgroundMetrics.wrapWidth(100, 4, 2.0));
	}
}
