package io.github.ikunkk02.chatcanvas.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LayoutConfigTest {
	@Test
	void defaultsProjectToExpectedArea() {
		PixelLayout pixels = LayoutConfig.DEFAULT.toPixels(1000, 800);
		assertEquals(40, pixels.x());
		assertEquals(544, pixels.y());
		assertEquals(350, pixels.width());
		assertEquals(224, pixels.height());
	}

	@Test
	void invalidAndOutOfRangeRatiosAreSanitized() {
		assertSame(LayoutConfig.DEFAULT,
				new LayoutConfig(Double.NaN, 0, 0.2, 0.2).sanitized());
		LayoutConfig safe = new LayoutConfig(-2, 4, 3, 2).sanitized();
		assertEquals(0.0, safe.chatXRatio());
		assertEquals(0.0, safe.chatYRatio());
		assertEquals(1.0, safe.chatWidthRatio());
		assertEquals(1.0, safe.chatHeightRatio());
	}

	@Test
	void tinyViewportNeverProducesOffscreenGeometry() {
		PixelLayout pixels = LayoutConfig.DEFAULT.toPixels(120, 70);
		assertTrue(pixels.x() >= 0);
		assertTrue(pixels.y() >= 0);
		assertTrue(pixels.right() <= 120);
		assertTrue(pixels.bottom() <= 70);
	}

	@Test
	void pixelRoundTripPreservesLogicalPlacement() {
		PixelLayout source = new PixelLayout(120, 260, 420, 180);
		LayoutConfig ratios = LayoutConfig.fromPixels(source, 1200, 800);
		assertEquals(source, ratios.toPixels(1200, 800));
	}
}
