package io.github.ikunkk02.chatcanvas.chat.text;

public final class SpacedAdvanceMath {
	private SpacedAdvanceMath() {
	}

	public static double advance(double vanillaAdvance, double spacing, boolean hasFollowing) {
		double safeVanilla = Double.isFinite(vanillaAdvance)
				? Math.max(0.0, vanillaAdvance)
				: 0.0;
		if (!hasFollowing || safeVanilla == 0.0) return safeVanilla;
		double safeSpacing = Double.isFinite(spacing) ? spacing : 0.0;
		return Math.max(0.0, safeVanilla + safeSpacing);
	}

	public static double width(double[] vanillaAdvances, double spacing) {
		if (vanillaAdvances == null || vanillaAdvances.length == 0) return 0.0;
		double width = 0.0;
		for (int index = 0; index < vanillaAdvances.length; index++) {
			width += advance(
					vanillaAdvances[index], spacing,
					hasFollowingVisibleAdvance(vanillaAdvances, index + 1));
		}
		return width;
	}

	private static boolean hasFollowingVisibleAdvance(double[] advances, int from) {
		for (int index = from; index < advances.length; index++) {
			if (Double.isFinite(advances[index]) && advances[index] > 0.0) return true;
		}
		return false;
	}
}
