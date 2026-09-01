package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.config.CommandClipboardConfig;
import io.github.ikunkk02.chatcanvas.editor.EditorSession;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CommandMaxScrubberComponent extends BaseUIComponent implements NumericScrubber {
	private final EditorSession session;
	private final Runnable changed;
	private final Runnable committed;
	private CommandClipboardConfig dragStart;
	private double startX;
	private int startValue;
	private boolean dragging;
	private boolean hovered;

	public CommandMaxScrubberComponent(EditorSession session, Runnable changed, Runnable committed) {
		this.session = session;
		this.changed = changed;
		this.committed = committed;
		sizing(Sizing.fill(100), Sizing.fixed(24));
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		super.update(delta, mouseX, mouseY);
		hovered = valueRegionContains(mouseX, mouseY);
		cursorStyle(hovered || dragging ? CursorStyle.HORIZONTAL_RESIZE : CursorStyle.NONE);
	}

	@Override
	public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
		var renderer = Minecraft.getInstance().font;
		int valueLeft = x() + width() - 92;
		context.fill(valueLeft, y() + 2, x() + width(), y() + height() - 2,
				ModernUiTheme.CONTROL_BACKGROUND);
		double progress = (session.commandClipboard().maxCommands()
				- CommandClipboardConfig.MIN_COMMANDS)
				/ (double) (CommandClipboardConfig.MAX_COMMANDS
				- CommandClipboardConfig.MIN_COMMANDS);
		context.fill(valueLeft, y() + height() - 3,
				valueLeft + (int) Math.round(92 * progress), y() + height() - 2,
				ModernUiTheme.ACCENT_MUTED);
		int ty = y() + (height() - renderer.lineHeight) / 2;
		Component label = Component.translatable("chat_canvas.command.max_commands");
		context.text(renderer,
				ModernUiTheme.fitText(renderer, label, Math.max(1, valueLeft - x() - 8)),
				x() + 2, ty, ModernUiTheme.TEXT_SECONDARY, false);
		String value = Integer.toString(session.commandClipboard().maxCommands());
		context.text(renderer, value, x() + width() - 8 - renderer.width(value),
				ty, ModernUiTheme.TEXT_PRIMARY, false);
	}

	@Override
	public boolean valueRegionContains(double mouseX, double mouseY) {
		return mouseX >= x() + width() - 92 && mouseX <= x() + width()
				&& mouseY >= y() && mouseY <= y() + height();
	}

	@Override
	public boolean beginPointerInteraction(double mouseX, double mouseY, int button,
										   boolean shiftDown, boolean controlDown) {
		if (button != 0 || !valueRegionContains(mouseX, mouseY)) return false;
		dragStart = session.commandClipboard();
		startX = mouseX;
		startValue = dragStart.maxCommands();
		dragging = true;
		return true;
	}

	@Override
	public boolean dragPointer(double mouseX, double mouseY, int button) {
		if (!dragging || button != 0) return false;
		double step = Minecraft.getInstance().hasShiftDown() ? 1.0 : Minecraft.getInstance().hasControlDown() ? 20.0 : 5.0;
		apply((int) Math.round(startValue + (mouseX - startX) * step));
		return true;
	}

	@Override
	public boolean endPointerInteraction(double mouseX, double mouseY, int button) {
		if (!dragging || button != 0) return false;
		dragging = false;
		dragStart = null;
		session.commit();
		committed.run();
		return true;
	}

	@Override
	public void cancelPointerInteraction() {
		if (dragging && dragStart != null) {
			session.setCommandClipboard(dragStart);
			changed.run();
		}
		dragging = false;
		dragStart = null;
	}

	@Override
	public boolean scroll(double amount) {
		if (!hovered || amount == 0) return false;
		int step = Minecraft.getInstance().hasShiftDown() ? 1 : Minecraft.getInstance().hasControlDown() ? 100 : 20;
		apply(session.commandClipboard().maxCommands() + (amount > 0 ? step : -step));
		session.commit();
		committed.run();
		return true;
	}

	@Override
	public boolean restoreDefault() {
		apply(CommandClipboardConfig.DEFAULT.maxCommands());
		session.commit();
		committed.run();
		return true;
	}

	@Override
	public void resizeViewport(int width, int height) {
	}

	private void apply(int value) {
		session.setCommandClipboard(session.commandClipboard().withMaxCommands(value));
		changed.run();
	}
}
