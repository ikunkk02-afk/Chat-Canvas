package io.github.ikunkk02.chatcanvas.editor;

import java.util.Locale;
import java.util.OptionalInt;

public final class ColorPickerState {
	private final int initialRgb;
	private int rgb;
	private float hue;
	private float saturation;
	private float value;
	private String hexInput;
	private boolean hexValid = true;

	public ColorPickerState(int initialRgb) {
		this.initialRgb = sanitizeRgb(initialRgb);
		setRgb(this.initialRgb);
	}

	public int initialRgb() {
		return initialRgb;
	}

	public int rgb() {
		return rgb;
	}

	public float hue() {
		return hue;
	}

	public float saturation() {
		return saturation;
	}

	public float value() {
		return value;
	}

	public String hexInput() {
		return hexInput;
	}

	public boolean hexValid() {
		return hexValid;
	}

	public void setRgb(int value) {
		rgb = sanitizeRgb(value);
		float[] hsv = rgbToHsv(rgb);
		hue = hsv[0];
		saturation = hsv[1];
		this.value = hsv[2];
		hexInput = normalizedHex(rgb);
		hexValid = true;
	}

	public void setHsv(float hue, float saturation, float value) {
		this.hue = clamp01(hue);
		this.saturation = clamp01(saturation);
		this.value = clamp01(value);
		rgb = rgbFromHsv(this.hue, this.saturation, this.value);
		hexInput = normalizedHex(rgb);
		hexValid = true;
	}

	public boolean updateHexInput(String input) {
		hexInput = input == null ? "" : input.strip();
		OptionalInt parsed = parseRgb(hexInput);
		hexValid = parsed.isPresent();
		if (parsed.isEmpty()) {
			return false;
		}
		rgb = parsed.getAsInt();
		float[] hsv = rgbToHsv(rgb);
		hue = hsv[0];
		saturation = hsv[1];
		value = hsv[2];
		return true;
	}

	public void normalizeHexInput() {
		hexInput = normalizedHex(rgb);
		hexValid = true;
	}

	public static OptionalInt parseRgb(String input) {
		if (input == null) {
			return OptionalInt.empty();
		}
		String value = input.strip();
		if (value.startsWith("#")) {
			value = value.substring(1);
		}
		if (!value.matches("[0-9a-fA-F]{6}")) {
			return OptionalInt.empty();
		}
		try {
			return OptionalInt.of(Integer.parseInt(value, 16));
		} catch (NumberFormatException ignored) {
			return OptionalInt.empty();
		}
	}

	public static String normalizedHex(int rgb) {
		return String.format(Locale.ROOT, "#%06X", sanitizeRgb(rgb));
	}

	private static float[] rgbToHsv(int rgb) {
		float red = (rgb >> 16 & 0xFF) / 255.0f;
		float green = (rgb >> 8 & 0xFF) / 255.0f;
		float blue = (rgb & 0xFF) / 255.0f;
		float max = Math.max(red, Math.max(green, blue));
		float min = Math.min(red, Math.min(green, blue));
		float delta = max - min;
		float hue;
		if (delta == 0.0f) {
			hue = 0.0f;
		} else if (max == red) {
			hue = ((green - blue) / delta) % 6.0f;
		} else if (max == green) {
			hue = (blue - red) / delta + 2.0f;
		} else {
			hue = (red - green) / delta + 4.0f;
		}
		hue /= 6.0f;
		if (hue < 0.0f) {
			hue += 1.0f;
		}
		float saturation = max == 0.0f ? 0.0f : delta / max;
		return new float[]{hue, saturation, max};
	}

	public static int rgbFromHsv(float hue, float saturation, float value) {
		float safeHue = (clamp01(hue) % 1.0f) * 6.0f;
		int sector = (int) Math.floor(safeHue);
		float fraction = safeHue - sector;
		float p = value * (1.0f - saturation);
		float q = value * (1.0f - fraction * saturation);
		float t = value * (1.0f - (1.0f - fraction) * saturation);
		float red;
		float green;
		float blue;
		switch (sector % 6) {
			case 0 -> {
				red = value;
				green = t;
				blue = p;
			}
			case 1 -> {
				red = q;
				green = value;
				blue = p;
			}
			case 2 -> {
				red = p;
				green = value;
				blue = t;
			}
			case 3 -> {
				red = p;
				green = q;
				blue = value;
			}
			case 4 -> {
				red = t;
				green = p;
				blue = value;
			}
			default -> {
				red = value;
				green = p;
				blue = q;
			}
		}
		return Math.round(red * 255.0f) << 16
				| Math.round(green * 255.0f) << 8
				| Math.round(blue * 255.0f);
	}

	private static float clamp01(float value) {
		if (!Float.isFinite(value)) {
			return 0.0f;
		}
		return Math.max(0.0f, Math.min(1.0f, value));
	}

	private static int sanitizeRgb(int value) {
		return Math.max(0, Math.min(0xFFFFFF, value));
	}
}
