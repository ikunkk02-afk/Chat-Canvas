package io.github.ikunkk02.chatcanvas.animation;

public final class AnimatedFloat {
	private float value;
	private float target;
	private final float speed;

	public AnimatedFloat(float initialValue, float speed) {
		this.value = initialValue;
		this.target = initialValue;
		this.speed = Math.max(0.01f, speed);
	}

	public float update(double deltaSeconds) {
		float difference = target - value;
		float factor = 1.0f - (float) Math.exp(-speed * Math.max(0.0, Math.min(0.05, deltaSeconds)));
		value += difference * factor;
		if (Math.abs(target - value) < 0.001f) {
			value = target;
		}
		return value;
	}

	public void setTarget(float target) {
		this.target = target;
	}

	public float value() {
		return value;
	}
}
