package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import io.github.ikunkk02.chatcanvas.editor.EditorUiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class AlignmentGuideRenderer {
	private AlignmentGuideRenderer() {
	}

	public static void render(GuiGraphics context, int screenWidth, int screenHeight,
							  PixelLayout layout, PreviewChatWidget preview) {
		if (!preview.dragging()) return;
		int margin = PixelLayout.DEFAULT_SAFE_MARGIN;
		boolean vanilla = ModernUiTheme.currentStyle() == EditorUiStyle.VANILLA;
		int guide = vanilla ? 0x5570A7FF : ModernUiTheme.ACCENT_MUTED;
		int active = vanilla ? 0xCC8EB8FF : ModernUiTheme.ACCENT;
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
		ModernUiTheme.roundedRect(context, x, y, textWidth + 8, 15,
				vanilla ? 4 : 1, vanilla ? 0xD91A1E28 : ModernUiTheme.PANEL_BACKGROUND);
		context.drawString(Minecraft.getInstance().font, geometry, x + 4, y + 3,
				vanilla ? 0xFFE9EDF4 : ModernUiTheme.TEXT_PRIMARY, false);
	}
}
