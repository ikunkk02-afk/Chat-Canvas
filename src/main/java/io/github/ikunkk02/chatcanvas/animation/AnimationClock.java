package io.github.ikunkk02.chatcanvas.animation;

public final class AnimationClock {
	private long lastNanos;

	public double tick() {
		long now = System.nanoTime();
		if (lastNanos == 0L) {
			lastNanos = now;
			return 0.0;
		}
		double delta = (now - lastNanos) / 1_000_000_000.0;
		lastNanos = now;
		return Math.max(0.0, Math.min(0.05, delta));
	}

	public void reset() {
		lastNanos = 0L;
	}
}
