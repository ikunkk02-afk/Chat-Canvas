package io.github.ikunkk02.chatcanvas.animation;

public final class SpringValue {
	private static final double MAX_DELTA_SECONDS = 0.05;
	private static final double MAX_STEP_SECONDS = 1.0 / 120.0;
	private static final double POSITION_EPSILON = 0.5;
	private static final double VELOCITY_EPSILON = 10.0;

	private final MotionPreset preset;
	private double value;
	private double target;
	private double velocity;

	public SpringValue(double initialValue, MotionPreset preset) {
		this.value = initialValue;
		this.target = initialValue;
		this.preset = preset;
	}

	public double update(double deltaSeconds) {
		double remaining = Math.max(0.0, Math.min(MAX_DELTA_SECONDS, deltaSeconds));
		while (remaining > 0.0) {
			double step = Math.min(MAX_STEP_SECONDS, remaining);
			double acceleration = preset.stiffness() * (target - value) - preset.damping() * velocity;
			velocity += acceleration * step;
			value += velocity * step;
			remaining -= step;
		}
		if (Math.abs(target - value) < POSITION_EPSILON && Math.abs(velocity) < VELOCITY_EPSILON) {
			snapToTarget();
		}
		return value;
	}

	public void setTarget(double target) {
		this.target = target;
	}

	public void setValue(double value) {
		this.value = value;
		this.target = value;
		this.velocity = 0.0;
	}

	public void snapToTarget() {
		value = target;
		velocity = 0.0;
	}

	public double value() {
		return value;
	}

	public double target() {
		return target;
	}

	public double velocity() {
		return velocity;
	}

	public boolean settled() {
		return value == target && velocity == 0.0;
	}
}
