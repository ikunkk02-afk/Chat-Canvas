package io.github.ikunkk02.chatcanvas.chat.emoji;

import io.github.ikunkk02.chatcanvas.ChatCanvasForge;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects whether the current Minecraft font supports a given emoji glyph.
 * Uses Font.width() as a proxy — 0 width means unsupported glyph.
 */
public final class EmojiFontSupport {
    private static final Map<String, Boolean> CACHE = new HashMap<>();
    private static long epoch;
    private static long loggedEpoch = -1;
    private static boolean loggedFailure;

    private EmojiFontSupport() {}

    public static synchronized boolean supports(Font font, EmojiEntry entry) {
        if (font == null || entry == null) return false;
        Boolean cached = CACHE.get(entry.unicode());
        if (cached != null) return cached;
        boolean supported;
        try {
            // Simple heuristic: if font.width() returns > 0, the glyph exists
            supported = font.width(entry.unicode()) > 0;
        } catch (RuntimeException failure) {
            supported = false;
            if (!loggedFailure) {
                loggedFailure = true;
                ChatCanvasForge.LOGGER.error("Emoji font support detection failed", failure);
            }
        }
        CACHE.put(entry.unicode(), supported);
        return supported;
    }

    public static List<EmojiEntry> supportedEntries(Font font) {
        List<EmojiEntry> supported = EmojiRegistry.instance().entries().stream()
                .filter(entry -> supports(font, entry))
                .toList();
        synchronized (EmojiFontSupport.class) {
            if (loggedEpoch != epoch) {
                loggedEpoch = epoch;
                ChatCanvasForge.LOGGER.info(
                        "Emoji font epoch {}: {}/{} entries supported",
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

    public static synchronized long epoch() { return epoch; }
}
