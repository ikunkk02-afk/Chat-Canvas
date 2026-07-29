package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class AlignmentGuideRenderer {
	private AlignmentGuideRenderer() {
	}

	public static void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight,
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

		Component geometry = Component.translatable("chat_canvas.editor.geometry",
				layout.x(), layout.y(), layout.width(), layout.height());
		int textWidth = Minecraft.getInstance().font.width(geometry);
		int x = Math.max(4, Math.min(screenWidth - textWidth - 8, layout.x()));
		int y = Math.max(4, layout.y() - 18);
		ModernUiTheme.roundedRect(context, x, y, textWidth + 8, 15, 4, 0xD91A1E28);
		context.drawText(Minecraft.getInstance().font, geometry, x + 4, y + 3, 0xFFE9EDF4, false);
	}
}
