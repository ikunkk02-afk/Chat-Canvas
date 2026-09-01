package io.github.ikunkk02.chatcanvas.animation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpringValueTest {
	@Test
	void settlesAtSimilarTimesAcrossFrameRates() {
		double at30 = settleTime(30);
		double at60 = settleTime(60);
		double at144 = settleTime(144);
		assertTrue(at30 >= 0.25 && at30 <= 0.50, "30 FPS settle time: " + at30);
		assertTrue(Math.abs(at30 - at60) < 0.08);
		assertTrue(Math.abs(at60 - at144) < 0.08);
	}

	@Test
	void hugeDeltaIsClampedAndTargetReversalKeepsMotionFinite() {
		SpringValue spring = new SpringValue(0, MotionPreset.PANEL_SLIDE);
		spring.setTarget(800);
		double afterLongPause = spring.update(20);
		assertTrue(afterLongPause > 0 && afterLongPause < 800);
		double velocityBefore = spring.velocity();
		spring.setTarget(-100);
		assertEquals(velocityBefore, spring.velocity());
		for (int i = 0; i < 240; i++) spring.update(1.0 / 120.0);
		assertEquals(-100, spring.value(), 0.1);
		assertTrue(Double.isFinite(spring.velocity()));
	}

	private static double settleTime(int fps) {
		SpringValue spring = new SpringValue(0, MotionPreset.PANEL_SLIDE);
		spring.setTarget(800);
		double elapsed = 0;
		for (int frame = 0; frame < fps * 3 && !spring.settled(); frame++) {
			spring.update(1.0 / fps);
			elapsed += 1.0 / fps;
		}
		assertTrue(spring.settled(), "spring did not settle at " + fps + " FPS");
		return elapsed;
	}
}
