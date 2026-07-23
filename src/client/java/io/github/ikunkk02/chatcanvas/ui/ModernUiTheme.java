package io.github.ikunkk02.chatcanvas.ui;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public final class ModernUiTheme {
	public static final int PANEL_BACKGROUND = 0xE6191C26;
	public static final int PANEL_BORDER = 0x664C566A;
	public static final int ACCENT = 0xFF70A7FF;
	public static final int TEXT_PRIMARY = 0xFFF2F4F8;
	public static final int TEXT_SECONDARY = 0xFF9EA8BA;
	public static final Surface PANEL_SURFACE = (context, component) -> {
		shadow(context, component.x(), component.y(), component.width(), component.height());
		roundedRect(context, component.x(), component.y(), component.width(), component.height(), 7, PANEL_BACKGROUND);
		border(context, component.x(), component.y(), component.width(), component.height(), PANEL_BORDER);
	};

	private static final Map<ButtonComponent, Long> PRESSED_AT = new WeakHashMap<>();

	private ModernUiTheme() {
	}

	public static ButtonComponent button(Text text, Consumer<ButtonComponent> action) {
		ButtonComponent button = Components.button(text, clicked -> {
			PRESSED_AT.put(clicked, System.nanoTime());
			action.accept(clicked);
		});
		button.renderer(ModernUiTheme::drawButton);
		button.textShadow(false);
		return button;
	}

	private static void drawButton(OwoUIDrawContext context, ButtonComponent button, float delta) {
		int color;
		if (!button.active()) {
			color = 0x55343A48;
		} else if (button.isHovered()) {
			color = 0xE04B5970;
		} else {
			color = 0xC8374256;
		}
		Long pressedAt = PRESSED_AT.get(button);
		boolean pressed = pressedAt != null && System.nanoTime() - pressedAt < 90_000_000L;
		int inset = pressed ? 1 : 0;
		roundedRect(context, button.getX() + inset, button.getY() + inset,
				button.getWidth() - inset * 2, button.getHeight() - inset * 2, 5, color);
		border(context, button.getX() + inset, button.getY() + inset,
				button.getWidth() - inset * 2, button.getHeight() - inset * 2,
				button.active() ? 0x554F6079 : 0x223C4452);
	}

	public static void shadow(DrawContext context, int x, int y, int width, int height) {
		roundedRect(context, x - 3, y + 4, width + 6, height + 4, 8, 0x32000000);
		roundedRect(context, x - 1, y + 2, width + 2, height + 2, 7, 0x45000000);
	}

	public static void roundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
		if (width <= 0 || height <= 0) return;
		int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
		context.fill(x + r, y, x + width - r, y + height, color);
		context.fill(x, y + r, x + width, y + height - r, color);
		for (int i = 0; i < r; i++) {
			int inset = r - (int) Math.sqrt(Math.max(0, r * r - (r - i) * (r - i)));
			context.fill(x + inset, y + i, x + width - inset, y + i + 1, color);
			context.fill(x + inset, y + height - i - 1, x + width - inset, y + height - i, color);
		}
	}

	public static void border(DrawContext context, int x, int y, int width, int height, int color) {
		if (width <= 1 || height <= 1) return;
		context.fill(x + 5, y, x + width - 5, y + 1, color);
		context.fill(x + 5, y + height - 1, x + width - 5, y + height, color);
		context.fill(x, y + 5, x + 1, y + height - 5, color);
		context.fill(x + width - 1, y + 5, x + width, y + height - 5, color);
	}
}
