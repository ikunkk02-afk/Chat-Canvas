package io.github.ikunkk02.chatcanvas.chat.text;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public final class SpacedTextMetrics {
	private static final double EPSILON = 0.00001;

	private SpacedTextMetrics() {
	}

	public static int width(Font renderer, FormattedCharSequence text, double spacing) {
		if (Math.abs(spacing) < EPSILON) return renderer.width(text);
		return GlyphAdvanceCache.layout(renderer, text, spacing).roundedWidth();
	}

	public static int width(Font renderer, String text, double spacing) {
		if (Math.abs(spacing) < EPSILON) return renderer.width(text);
		return width(renderer, FormattedCharSequence.forward(text, Style.EMPTY), spacing);
	}

	public static double xAtCodePoint(
			Font renderer, FormattedCharSequence text, double spacing, int codePointIndex) {
		GlyphAdvanceCache.GlyphRun run = GlyphAdvanceCache.layout(renderer, text, spacing);
		int clamped = Math.max(0, Math.min(codePointIndex, run.glyphs().size()));
		return clamped == run.glyphs().size()
				? run.width()
				: run.glyphs().get(clamped).x();
	}

	public static double xAtUtf16(
			Font renderer, String text, double spacing, int utf16Index) {
		int clamped = UnicodeTextNavigator.floorGraphemeBoundary(
				text, Math.max(0, Math.min(utf16Index, text.length())));
		int codePoints = text.codePointCount(0, clamped);
		return xAtCodePoint(
				renderer,
				FormattedCharSequence.forward(text, Style.EMPTY),
				spacing,
				codePoints);
	}

	public static String trimToWidth(
			Font renderer, String text, int maxWidth, double spacing) {
		if (maxWidth <= 0 || text.isEmpty()) return "";
		if (Math.abs(spacing) < EPSILON) {
			String candidate = renderer.plainSubstrByWidth(text, maxWidth);
			return text.substring(0, UnicodeTextNavigator.floorGraphemeBoundary(
					text, candidate.length()));
		}
		GlyphAdvanceCache.GlyphRun run = GlyphAdvanceCache.layout(
				renderer, FormattedCharSequence.forward(text, Style.EMPTY), spacing);
		int utf16End = 0;
		for (GlyphAdvanceCache.Glyph glyph : run.glyphs()) {
			double right = glyph.x() + glyph.advance();
			if (right > maxWidth + EPSILON) break;
			utf16End += glyph.utf16Length();
		}
		return text.substring(0, UnicodeTextNavigator.floorGraphemeBoundary(
				text, Math.min(text.length(), utf16End)));
	}

	public static int firstVisibleIndex(
			Font renderer, String text, int cursor, int maxWidth, double spacing) {
		int safeCursor = UnicodeTextNavigator.floorGraphemeBoundary(
				text, Math.max(0, Math.min(cursor, text.length())));
		int start = safeCursor;
		while (start > 0) {
			int previous = UnicodeTextNavigator.previousGraphemeBoundary(text, start);
			if (width(renderer, text.substring(previous, safeCursor), spacing) > maxWidth) break;
			start = previous;
		}
		return start;
	}
}
