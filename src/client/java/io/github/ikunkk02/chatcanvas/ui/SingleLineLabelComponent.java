package io.github.ikunkk02.chatcanvas.ui;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * A fixed-size label which is deliberately never word-wrapped.
 *
 * <p>Owo's {@code LabelComponent} wraps during inflation using its current
 * width. Some modpack UI/font combinations inflate fixed labels before that
 * width has been assigned, producing one glyph per line. Drawing the header
 * title directly avoids that lifecycle dependency.</p>
 */
public final class SingleLineLabelComponent extends BaseComponent {
	private final Text text;
	private final int color;

	public SingleLineLabelComponent(Text text, int color, int width, int height) {
		this.text = text;
		this.color = color;
		this.sizing(Sizing.fixed(width), Sizing.fixed(height));
	}

	@Override
	public void draw(OwoUIDrawContext context, int mouseX, int mouseY,
			float partialTicks, float delta) {
		var renderer = MinecraftClient.getInstance().textRenderer;
		Text visible = text;
		if (renderer.getWidth(text) > width()) {
			visible = Text.literal(ModernUiTheme.fitText(renderer, text, width()))
					.setStyle(text.getStyle());
		}
		int textY = y() + Math.max(0, (height() - renderer.fontHeight) / 2);
		context.drawText(renderer, visible, x(), textY, color, false);
	}
}
