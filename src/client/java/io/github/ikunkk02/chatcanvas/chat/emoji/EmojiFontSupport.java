package io.github.ikunkk02.chatcanvas.chat.emoji;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import net.minecraft.client.gui.Font;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Font filtering adapted to the 26.1 Font/GlyphSource pipeline. */
public final class EmojiFontSupport {
	private static final Map<String, Boolean> CACHE = new HashMap<>();
	private static long epoch;
	private static long loggedEpoch = -1;

	private EmojiFontSupport() {
	}

	public static synchronized boolean supports(Font renderer, EmojiEntry entry) {
		if (renderer == null || entry == null) return false;
		Boolean cached = CACHE.get(entry.unicode());
		if (cached != null) return cached;
		/*
		 * 26.1 resolves glyphs through Font.Provider/GlyphSource and no longer
		 * exposes the old FontStorage/BuiltinEmptyGlyph pair. Font.width is the
		 * supported public query and also accounts for the active font options.
		 */
		boolean supported = entry.unicode().codePoints()
				.filter(codePoint -> !ignorable(codePoint))
				.allMatch(codePoint -> renderer.width(new String(Character.toChars(codePoint))) > 0);
		CACHE.put(entry.unicode(), supported);
		return supported;
	}

	public static List<EmojiEntry> supportedEntries(Font renderer) {
		List<EmojiEntry> supported = EmojiRegistry.instance().entries().stream()
				.filter(entry -> supports(renderer, entry)).toList();
		synchronized (EmojiFontSupport.class) {
			if (loggedEpoch != epoch) {
				loggedEpoch = epoch;
				ChatCanvas.LOGGER.info("Emoji font evaluation epoch {}: {}/{} whitelist entries supported",
						epoch, supported.size(), EmojiRegistry.instance().entries().size());
			}
		}
		return supported;
	}

	public static synchronized void onFontResourcesReloaded() {
		epoch++;
		CACHE.clear();
	}

	public static synchronized long epoch() {
		return epoch;
	}

	private static boolean ignorable(int codePoint) {
		if (codePoint == 0x200D || codePoint == 0xFE0E || codePoint == 0xFE0F) return true;
		int type = Character.getType(codePoint);
		return type == Character.FORMAT || type == Character.NON_SPACING_MARK
				|| type == Character.COMBINING_SPACING_MARK || type == Character.ENCLOSING_MARK;
	}
}
