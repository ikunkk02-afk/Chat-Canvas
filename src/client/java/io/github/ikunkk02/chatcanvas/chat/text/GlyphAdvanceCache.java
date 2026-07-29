package io.github.ikunkk02.chatcanvas.chat.text;

import com.ibm.icu.text.BreakIterator;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * Bounded identity cache for the glyph runs used by every spaced text operation.
 * OrderedText is intentionally keyed by identity because many implementations are
 * visitor lambdas without structural equality.
 */
public final class GlyphAdvanceCache {
	private static final int MAX_TEXTS = 512;
	private static final int MAX_SPACING_VARIANTS = 8;
	private static final Map<OrderedText, Map<Long, GlyphRun>> RUNS = new IdentityHashMap<>();
	private static long fontEpoch;

	private GlyphAdvanceCache() {
	}

	public static synchronized GlyphRun layout(
			TextRenderer renderer, OrderedText text, double spacing) {
		long key = spacingKey(spacing);
		Map<Long, GlyphRun> variants = RUNS.get(text);
		if (variants != null) {
			GlyphRun cached = variants.get(key);
			if (cached != null && cached.fontEpoch() == fontEpoch) return cached;
		}
		if (RUNS.size() >= MAX_TEXTS && !RUNS.containsKey(text)) {
			RUNS.clear();
		}
		GlyphRun built = build(renderer, text, spacing);
		Map<Long, GlyphRun> target = RUNS.computeIfAbsent(
				text, ignored -> new java.util.LinkedHashMap<>());
		if (target.size() >= MAX_SPACING_VARIANTS && !target.containsKey(key)) {
			target.clear();
		}
		target.put(key, built);
		return built;
	}

	public static synchronized void clear() {
		RUNS.clear();
	}

	public static synchronized void onFontResourcesReloaded() {
		fontEpoch++;
		RUNS.clear();
	}

	private static GlyphRun build(TextRenderer renderer, OrderedText text, double spacing) {
		List<MutableGlyph> captured = new ArrayList<>();
		int[] utf16 = {0};
		text.accept((sourceIndex, style, codePoint) -> {
			Style safeStyle = style == null ? Style.EMPTY : style;
			float vanilla = renderer.getTextHandler().getWidth(
					OrderedText.styled(codePoint, safeStyle));
			captured.add(new MutableGlyph(
					sourceIndex, utf16[0], codePoint, safeStyle,
					Math.max(0.0f, vanilla)));
			utf16[0] += Character.charCount(codePoint);
			return true;
		});

		boolean[] addSpacingAfter = clusterSpacingTargets(captured);
		List<Glyph> glyphs = new ArrayList<>(captured.size());
		double x = 0.0;
		double combiningX = 0.0;
		for (int index = 0; index < captured.size(); index++) {
			MutableGlyph glyph = captured.get(index);
			double advance = SpacedAdvanceMath.advance(
					glyph.vanillaAdvance(), spacing, addSpacingAfter[index]);
			double glyphX = glyph.vanillaAdvance() <= 0.0f ? combiningX : x;
			if (glyph.vanillaAdvance() > 0.0f) {
				combiningX = x + glyph.vanillaAdvance();
			}
			glyphs.add(new Glyph(
					glyph.sourceIndex(), glyph.codePoint(), glyph.style(),
					glyph.vanillaAdvance(), glyphX, advance));
			if (glyph.vanillaAdvance() > 0.0f) x += advance;
		}
		return new GlyphRun(List.copyOf(glyphs), x, fontEpoch);
	}

	private static boolean[] clusterSpacingTargets(List<MutableGlyph> glyphs) {
		boolean[] targets = new boolean[glyphs.size()];
		if (glyphs.isEmpty()) return targets;
		StringBuilder plain = new StringBuilder();
		for (MutableGlyph glyph : glyphs) plain.appendCodePoint(glyph.codePoint());
		BreakIterator iterator = BreakIterator.getCharacterInstance(Locale.ROOT);
		iterator.setText(plain.toString());
		List<Integer> lastVisiblePerCluster = new ArrayList<>();
		int glyphIndex = 0;
		int start = iterator.first();
		for (int end = iterator.next(); end != BreakIterator.DONE;
			 start = end, end = iterator.next()) {
			int lastVisible = -1;
			while (glyphIndex < glyphs.size()
					&& glyphs.get(glyphIndex).utf16Start() < end) {
				if (glyphs.get(glyphIndex).vanillaAdvance() > 0.0f) {
					lastVisible = glyphIndex;
				}
				glyphIndex++;
			}
			if (lastVisible >= 0) lastVisiblePerCluster.add(lastVisible);
		}
		for (int index = 0; index + 1 < lastVisiblePerCluster.size(); index++) {
			targets[lastVisiblePerCluster.get(index)] = true;
		}
		return targets;
	}

	private static long spacingKey(double spacing) {
		return Double.doubleToLongBits(Math.rint(spacing * 1000.0) / 1000.0);
	}

	private record MutableGlyph(
			int sourceIndex,
			int utf16Start,
			int codePoint,
			Style style,
			float vanillaAdvance) {
	}

	public record Glyph(
			int sourceIndex,
			int codePoint,
			Style style,
			float vanillaAdvance,
			double x,
			double advance
	) {
		public int utf16Length() {
			return Character.charCount(codePoint);
		}
	}

	public record GlyphRun(List<Glyph> glyphs, double width, long fontEpoch) {
		public GlyphRun {
			glyphs = List.copyOf(glyphs);
		}

		public int roundedWidth() {
			return (int) Math.ceil(Math.max(0.0, width - 0.00001));
		}
	}
}
