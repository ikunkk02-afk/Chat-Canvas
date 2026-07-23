package io.github.ikunkk02.chatcanvas.chat.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpacedAdvanceMathTest {
	@Test
	void addsSpacingOnlyBetweenVisibleGlyphs() {
		assertEquals(20.0, SpacedAdvanceMath.width(
				new double[]{5.0, 5.0, 5.0}, 2.5), 0.00001);
		assertEquals(15.0, SpacedAdvanceMath.width(
				new double[]{5.0, 5.0, 5.0}, 0.0), 0.00001);
	}

	@Test
	void negativeSpacingCannotReverseGlyphOrder() {
		assertEquals(5.0, SpacedAdvanceMath.width(
				new double[]{0.5, 0.5, 5.0}, -20.0), 0.00001);
		assertEquals(0.0, SpacedAdvanceMath.advance(0.5, -1.0, true), 0.00001);
	}

	@Test
	void zeroWidthCombiningGlyphDoesNotInventNegativeAdvance() {
		assertEquals(8.0, SpacedAdvanceMath.width(
				new double[]{5.0, 0.0, 1.0}, 2.0), 0.00001);
	}
}
