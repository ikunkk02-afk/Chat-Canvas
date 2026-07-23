package io.github.ikunkk02.chatcanvas.editor;

public final class NumericScrubberMath {
	private NumericScrubberMath() {
	}

	public static int valueDelta(double totalMouseDelta, Sensitivity sensitivity) {
		return switch (sensitivity) {
			case NORMAL -> roundedUnits(totalMouseDelta, 2.0);
			case FINE -> roundedUnits(totalMouseDelta, 6.0);
			case FAST -> roundedUnits(totalMouseDelta, 2.0) * 5;
		};
	}

	private static int roundedUnits(double delta, double pixelsPerUnit) {
		return (int) Math.round(delta / pixelsPerUnit);
	}

	public enum Sensitivity {
		NORMAL,
		FINE,
		FAST;

		public static Sensitivity fromModifiers(boolean shiftDown, boolean controlDown) {
			if (controlDown) return FAST;
			if (shiftDown) return FINE;
			return NORMAL;
		}
	}
}
