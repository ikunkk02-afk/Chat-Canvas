package io.github.ikunkk02.chatcanvas.animation;

public record MotionPreset(double stiffness, double damping) {
	public static final MotionPreset PANEL_SLIDE = new MotionPreset(520.0, 38.0);
	public static final MotionPreset CATEGORY_SLIDE = new MotionPreset(320.0, 36.0);
}
