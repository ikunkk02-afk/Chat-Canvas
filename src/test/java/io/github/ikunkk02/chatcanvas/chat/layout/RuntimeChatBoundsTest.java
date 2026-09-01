package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeChatBoundsTest {
	private static final PixelLayout LAYOUT = new PixelLayout(100, 200, 360, 180);

	@Test
	void closedChatGivesMessagesTheWholeConfiguredHeight() {
		RuntimeChatBounds bounds = RuntimeChatBounds.calculate(
				LAYOUT, false, 12, RuntimeChatBounds.DEFAULT_INPUT_GAP, 9);

		assertEquals(LAYOUT.y(), bounds.messageTop());
		assertEquals(LAYOUT.bottom(), bounds.messageBottom());
		assertEquals(LAYOUT.height(), bounds.messageHeight());
		assertEquals(0, bounds.inputHeight());
	}

	@Test
	void openChatReservesActualInputHeightAndGapInsideTotalBounds() {
		RuntimeChatBounds bounds = RuntimeChatBounds.calculate(
				LAYOUT, true, 12, RuntimeChatBounds.DEFAULT_INPUT_GAP, 9);

		assertEquals(LAYOUT.bottom(), bounds.inputBottom());
		assertEquals(LAYOUT.bottom() - 12, bounds.inputTop());
		assertEquals(3, bounds.inputGap());
		assertEquals(bounds.inputTop() - 3, bounds.messageBottom());
		assertEquals(LAYOUT.height() - 12 - 3, bounds.messageHeight());
	}

	@Test
	void tinyBoundsKeepAtLeastTheRequestedMessageLineWhenPossible() {
		PixelLayout tiny = new PixelLayout(0, 0, 100, 20);
		RuntimeChatBounds bounds = RuntimeChatBounds.calculate(tiny, true, 12, 3, 9);

		assertTrue(bounds.messageHeight() >= 9);
		assertEquals(20, bounds.inputBottom());
		assertTrue(bounds.messageBottom() <= bounds.inputTop());
	}
}
