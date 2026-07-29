package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class AlignmentGuideRenderer {
	private AlignmentGuideRenderer() {
	}

	public static void render(DrawContext context, int screenWidth, int screenHeight,
							  PixelLayout layout, PreviewChatWidget preview) {
		if (!preview.dragging()) return;
		int margin = PixelLayout.DEFAULT_SAFE_MARGIN;
		int guide = 0x5570A7FF;
		int active = 0xCC8EB8FF;
		context.fill(screenWidth / 2, 0, screenWidth / 2 + 1, screenHeight, preview.snappedX() ? active : guide);
		context.fill(0, screenHeight / 2, screenWidth, screenHeight / 2 + 1, preview.snappedY() ? active : guide);
		context.fill(margin, 0, margin + 1, screenHeight, guide);
		context.fill(screenWidth - margin, 0, screenWidth - margin + 1, screenHeight, guide);
		context.fill(0, margin, screenWidth, margin + 1, guide);
		context.fill(0, screenHeight - margin, screenWidth, screenHeight - margin + 1, guide);

		Text geometry = Text.translatable("chat_canvas.editor.geometry",
				layout.x(), layout.y(), layout.width(), layout.height());
		int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(geometry);
		int x = Math.max(4, Math.min(screenWidth - textWidth - 8, layout.x()));
		int y = Math.max(4, layout.y() - 18);
		ModernUiTheme.roundedRect(context, x, y, textWidth + 8, 15, 4, 0xD91A1E28);
		context.drawText(MinecraftClient.getInstance().textRenderer, geometry, x + 4, y + 3, 0xFFE9EDF4, false);
	}
}
