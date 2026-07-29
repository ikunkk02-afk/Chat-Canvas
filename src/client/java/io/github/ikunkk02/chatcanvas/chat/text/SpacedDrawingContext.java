package io.github.ikunkk02.chatcanvas.chat.text;

/**
 * Synchronous render scope consumed by TextRenderer.Drawer. Keeping the
 * original OrderedText in one draw call is important for style decorations and
 * for Chat Heads, which counts characters within one Drawer instance.
 */
public final class SpacedDrawingContext {
	private static final ThreadLocal<GlyphAdvanceCache.GlyphRun> ACTIVE = new ThreadLocal<>();

	private SpacedDrawingContext() {
	}

	public static Scope begin(GlyphAdvanceCache.GlyphRun run) {
		GlyphAdvanceCache.GlyphRun previous = ACTIVE.get();
		ACTIVE.set(run);
		return () -> {
			if (previous == null) ACTIVE.remove();
			else ACTIVE.set(previous);
		};
	}

	public static double extraAdvance(int glyphIndex) {
		GlyphAdvanceCache.GlyphRun run = ACTIVE.get();
		if (run == null || glyphIndex < 0 || glyphIndex >= run.glyphs().size()) return 0.0;
		GlyphAdvanceCache.Glyph glyph = run.glyphs().get(glyphIndex);
		return glyph.advance() - glyph.vanillaAdvance();
	}

	public static boolean active() {
		return ACTIVE.get() != null;
	}

	@FunctionalInterface
	public interface Scope extends AutoCloseable {
		@Override
		void close();
	}
}
