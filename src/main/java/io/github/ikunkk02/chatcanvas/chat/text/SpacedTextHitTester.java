package io.github.ikunkk02.chatcanvas.chat.text;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public final class SpacedTextHitTester {
	private SpacedTextHitTester() {
	}

	@Nullable
	public static Style styleAt(
			Font renderer, FormattedCharSequence text, double spacing, double x) {
		GlyphAdvanceCache.GlyphRun run = GlyphAdvanceCache.layout(renderer, text, spacing);
		if (x < 0.0 || x > run.width()) return null;
		for (GlyphAdvanceCache.Glyph glyph : run.glyphs()) {
			if (x < glyph.x() + glyph.advance()) return glyph.style();
		}
		return null;
	}

	public static int utf16IndexAt(
			Font renderer, String text, double spacing, double x) {
		if (x <= 0.0 || text.isEmpty()) return 0;
		GlyphAdvanceCache.GlyphRun run = GlyphAdvanceCache.layout(
				renderer, FormattedCharSequence.forward(text, Style.EMPTY), spacing);
		int utf16 = 0;
		for (GlyphAdvanceCache.Glyph glyph : run.glyphs()) {
			double midpoint = glyph.x() + glyph.advance() * 0.5;
			if (x < midpoint) {
				return UnicodeTextNavigator.nearestGraphemeBoundary(text, utf16);
			}
			utf16 += glyph.utf16Length();
		}
		return text.length();
	}
}
