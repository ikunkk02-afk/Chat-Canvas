package io.github.ikunkk02.chatcanvas.chat.emoji;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import net.minecraft.client.font.TextRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EmojiFontSupport {
	private static final Map<String, Boolean> CACHE = new HashMap<>();
	private static long epoch;
	private static long loggedEpoch = -1;
	private static boolean loggedFailure;

	private EmojiFontSupport() {
	}

	public static synchronized boolean supports(
			TextRenderer renderer, EmojiEntry entry) {
		if (renderer == null || entry == null) return false;
		Boolean cached = CACHE.get(entry.unicode());
		if (cached != null) return cached;
		boolean supported;
		try {
			// 1.21.11 no longer exposes TextRenderer font storage/glyph APIs.
			// A measurable width is the compatible runtime font-support heuristic.
			supported = renderer.getWidth(entry.unicode()) > 0;
		} catch (RuntimeException failure) {
			supported = false;
			if (!loggedFailure) {
				loggedFailure = true;
				ChatCanvas.LOGGER.error(
						"Emoji font support detection failed; unsupported entries will be hidden",
						failure);
			}
		}
		CACHE.put(entry.unicode(), supported);
		return supported;
	}

	public static List<EmojiEntry> supportedEntries(TextRenderer renderer) {
		List<EmojiEntry> supported = EmojiRegistry.instance().entries().stream()
				.filter(entry -> supports(renderer, entry))
				.toList();
		synchronized (EmojiFontSupport.class) {
			if (loggedEpoch != epoch) {
				loggedEpoch = epoch;
				ChatCanvas.LOGGER.info(
						"Emoji font evaluation epoch {}: {}/{} whitelist entries supported",
						epoch, supported.size(), EmojiRegistry.instance().entries().size());
			}
		}
		return supported;
	}

	public static synchronized void onFontResourcesReloaded() {
		epoch++;
		CACHE.clear();
		loggedFailure = false;
	}

	public static synchronized long epoch() {
		return epoch;
	}

	private static boolean ignorable(int codePoint) {
		if (codePoint == 0x200D || codePoint == 0xFE0E || codePoint == 0xFE0F) {
			return true;
		}
		int type = Character.getType(codePoint);
		return type == Character.FORMAT
				|| type == Character.NON_SPACING_MARK
				|| type == Character.COMBINING_SPACING_MARK
				|| type == Character.ENCLOSING_MARK;
	}
}
