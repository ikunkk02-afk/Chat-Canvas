package io.github.ikunkk02.chatcanvas.chat.text;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;

public final class SpacedTextRenderer {
	private static final double EPSILON = 0.00001;

	private SpacedTextRenderer() {
	}

	public static int draw(
			GuiGraphicsExtractor context,
			Font renderer,
			FormattedCharSequence text,
			double x,
			int y,
			int color,
			boolean shadow,
			double spacing) {
		if (Math.abs(spacing) < EPSILON) {
			context.text(renderer, text, (int) Math.round(x), y, color, shadow);
			return renderer.width(text);
		}
		GlyphAdvanceCache.GlyphRun run = GlyphAdvanceCache.layout(renderer, text, spacing);
		try (SpacedDrawingContext.Scope ignored = SpacedDrawingContext.begin(run)) {
			context.text(renderer, text, (int) Math.round(x), y, color, shadow);
			return run.roundedWidth();
		}
	}
}
