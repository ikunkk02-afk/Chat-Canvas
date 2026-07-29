package io.github.ikunkk02.chatcanvas.chat.text;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;

public final class SpacedTextRenderer {
	private static final double EPSILON = 0.00001;

	private SpacedTextRenderer() {
	}

	public static void draw(
			DrawContext context,
			TextRenderer renderer,
			OrderedText text,
			double x,
			int y,
			int color,
			boolean shadow,
			double spacing) {
		if (Math.abs(spacing) < EPSILON) {
			context.drawText(renderer, text, (int) Math.round(x), y, color, shadow);
			return;
		}
		GlyphAdvanceCache.GlyphRun run = GlyphAdvanceCache.layout(renderer, text, spacing);
		try (SpacedDrawingContext.Scope ignored = SpacedDrawingContext.begin(run)) {
			context.drawText(
					renderer, text, (int) Math.round(x), y, color, shadow);
		}
	}
}
