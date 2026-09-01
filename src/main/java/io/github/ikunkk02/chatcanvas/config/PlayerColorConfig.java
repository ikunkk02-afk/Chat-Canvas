package io.github.ikunkk02.chatcanvas.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public record PlayerColorConfig(
		boolean enabled,
		PlayerColorMode mode,
		List<Integer> palette,
		Map<String, Integer> uuidOverrides,
		Map<String, Integer> nameOverrides,
		boolean showNameHitboxes
) {
	public static final List<Integer> DEFAULT_PALETTE = List.of(
			0x4DA6FF, 0x29D3E8, 0x25C9A8, 0x55D96B,
			0xA2D85A, 0xF2C14E, 0xFF963D, 0xFF6B5F,
			0xFF4F70, 0xF06AAF, 0xC875F4, 0xA78BFA
	);

	public static final PlayerColorConfig DEFAULT = new PlayerColorConfig(
			true,
			PlayerColorMode.AUTOMATIC,
			DEFAULT_PALETTE,
			Map.of(),
			Map.of(),
			false
	);

	public PlayerColorConfig {
		mode = mode == null ? PlayerColorMode.AUTOMATIC : mode;
		palette = sanitizePalette(palette);
		uuidOverrides = sanitizeUuidOverrides(uuidOverrides);
		nameOverrides = sanitizeNameOverrides(nameOverrides);
	}

	public PlayerColorConfig sanitized() {
		return new PlayerColorConfig(
				enabled, mode, palette, uuidOverrides, nameOverrides, showNameHitboxes);
	}

	public PlayerColorConfig withEnabled(boolean value) {
		return new PlayerColorConfig(value, mode, palette, uuidOverrides, nameOverrides, showNameHitboxes);
	}

	public PlayerColorConfig withMode(PlayerColorMode value) {
		return new PlayerColorConfig(enabled, value, palette, uuidOverrides, nameOverrides, showNameHitboxes);
	}

	public PlayerColorConfig withPaletteColor(int index, int rgb) {
		if (index < 0 || index >= palette.size()) return this;
		List<Integer> updated = new ArrayList<>(palette);
		updated.set(index, sanitizeRgb(rgb));
		return new PlayerColorConfig(enabled, mode, updated, uuidOverrides, nameOverrides, showNameHitboxes);
	}

	public PlayerColorConfig withDefaultPalette() {
		return new PlayerColorConfig(enabled, mode, DEFAULT_PALETTE,
				uuidOverrides, nameOverrides, showNameHitboxes);
	}

	public PlayerColorConfig withUuidOverride(UUID uuid, int rgb) {
		if (uuid == null) return this;
		Map<String, Integer> updated = new LinkedHashMap<>(uuidOverrides);
		updated.put(uuid.toString(), sanitizeRgb(rgb));
		return new PlayerColorConfig(enabled, mode, palette, updated, nameOverrides, showNameHitboxes);
	}

	public PlayerColorConfig withoutOverrides(UUID uuid, String playerName) {
		Map<String, Integer> updatedUuids = new LinkedHashMap<>(uuidOverrides);
		if (uuid != null) updatedUuids.remove(uuid.toString());
		Map<String, Integer> updatedNames = new LinkedHashMap<>(nameOverrides);
		if (playerName != null) updatedNames.remove(normalizeName(playerName));
		return new PlayerColorConfig(enabled, mode, palette,
				updatedUuids, updatedNames, showNameHitboxes);
	}

	public PlayerColorConfig withShowNameHitboxes(boolean value) {
		return new PlayerColorConfig(enabled, mode, palette, uuidOverrides, nameOverrides, value);
	}

	public boolean hasOverride(UUID uuid, String playerName) {
		return uuid != null && uuidOverrides.containsKey(uuid.toString())
				|| playerName != null && nameOverrides.containsKey(normalizeName(playerName));
	}

	public static String normalizeName(String name) {
		return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
	}

	private static List<Integer> sanitizePalette(List<Integer> values) {
		if (values == null || values.isEmpty()) return DEFAULT_PALETTE;
		List<Integer> result = new ArrayList<>(values.size());
		for (Integer value : values) {
			if (value != null && value >= 0 && value <= 0xFFFFFF) {
				result.add(value);
			}
		}
		return result.isEmpty() ? DEFAULT_PALETTE : List.copyOf(result);
	}

	private static Map<String, Integer> sanitizeUuidOverrides(Map<String, Integer> values) {
		if (values == null || values.isEmpty()) return Map.of();
		Map<String, Integer> result = new LinkedHashMap<>();
		values.forEach((key, value) -> {
			if (key == null || value == null || value < 0 || value > 0xFFFFFF) return;
			try {
				result.put(UUID.fromString(key.trim()).toString(), value);
			} catch (IllegalArgumentException ignored) {
				// Ignore malformed UUID keys without rejecting the remaining config.
			}
		});
		return Map.copyOf(result);
	}

	private static Map<String, Integer> sanitizeNameOverrides(Map<String, Integer> values) {
		if (values == null || values.isEmpty()) return Map.of();
		Map<String, Integer> result = new LinkedHashMap<>();
		values.forEach((key, value) -> {
			String normalized = normalizeName(key);
			if (!normalized.isEmpty() && value != null && value >= 0 && value <= 0xFFFFFF) {
				result.put(normalized, value);
			}
		});
		return Map.copyOf(result);
	}

	private static int sanitizeRgb(int value) {
		return Math.max(0, Math.min(0xFFFFFF, value));
	}
}
