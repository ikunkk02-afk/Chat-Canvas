package io.github.ikunkk02.chatcanvas.chat.emoji;

import io.github.ikunkk02.chatcanvas.ChatCanvasForge;
import io.github.ikunkk02.chatcanvas.mixin.TextRendererAccessor;
import net.minecraft.client.font.BuiltinEmptyGlyph;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;

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
			Font renderer, EmojiEntry entry) {
		if (renderer == null || entry == null) return false;
		Boolean cached = CACHE.get(entry.unicode());
		if (cached != null) return cached;
		boolean supported;
		try {
			FontStorage storage = ((TextRendererAccessor) renderer)
					.chat_canvas$getFontStorage(Style.DEFAULT_FONT_ID);
			supported = entry.unicode().codePoints()
					.filter(codePoint -> !ignorable(codePoint))
					.allMatch(codePoint ->
							storage.getGlyph(codePoint, false)
									!= BuiltinEmptyGlyph.MISSING);
		} catch (RuntimeException failure) {
			supported = false;
			if (!loggedFailure) {
				loggedFailure = true;
				ChatCanvasForge.LOGGER.error(
						"Emoji font support detection failed; unsupported entries will be hidden",
						failure);
			}
		}
		CACHE.put(entry.unicode(), supported);
		return supported;
	}

	public static List<EmojiEntry> supportedEntries(Font renderer) {
		List<EmojiEntry> supported = EmojiRegistry.instance().entries().stream()
				.filter(entry -> supports(renderer, entry))
				.toList();
		synchronized (EmojiFontSupport.class) {
			if (loggedEpoch != epoch) {
				loggedEpoch = epoch;
				ChatCanvasForge.LOGGER.info(
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
