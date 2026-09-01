package io.github.ikunkk02.chatcanvas.config;

import java.util.ArrayList;
import java.util.List;

public final class RecentColorStore {
	public static final int MAX_COLORS = 8;

	private final List<Integer> colors = new ArrayList<>();

	public RecentColorStore(List<Integer> initialColors) {
		reset(initialColors);
	}

	public void add(int rgb) {
		int safeRgb = Math.max(0, Math.min(0xFFFFFF, rgb));
		colors.remove((Integer) safeRgb);
		colors.add(0, safeRgb);
		while (colors.size() > MAX_COLORS) {
			colors.remove(colors.size() - 1);
		}
	}

	public void reset(List<Integer> values) {
		colors.clear();
		for (Integer value : sanitizedCopy(values)) {
			add(value);
		}
		// add() is MRU-oriented, so restore the source order after loading
		java.util.Collections.reverse(colors);
	}

	public List<Integer> colors() {
		return List.copyOf(colors);
	}

	public static List<Integer> sanitizedCopy(List<Integer> values) {
		if (values == null || values.isEmpty()) {
			return List.of();
		}
		List<Integer> result = new ArrayList<>(Math.min(MAX_COLORS, values.size()));
		for (Integer value : values) {
			if (value == null || value < 0 || value > 0xFFFFFF || result.contains(value)) {
				continue;
			}
			result.add(value);
			if (result.size() == MAX_COLORS) {
				break;
			}
		}
		return List.copyOf(result);
	}
}
