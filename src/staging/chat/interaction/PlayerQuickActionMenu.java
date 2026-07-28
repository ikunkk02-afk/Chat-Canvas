package io.github.ikunkk02.chatcanvas.chat.interaction;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitbox;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitboxRegistry;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.mixin.ChatInputSuggestorAccessor;
import io.github.ikunkk02.chatcanvas.mixin.SuggestionWindowAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public final class PlayerQuickActionMenu {
	private static final int WIDTH = 116;
	private static final int ROW_HEIGHT = 20;
	private static final int HEIGHT = ROW_HEIGHT * 3;
	private ChatScreen owner;
	private PlayerNameHitbox target;
	private int x;
	private int y;
	private ChatFieldActions.InputSnapshot replacedInput;

	public boolean mouseClicked(
			ChatScreen screen, EditBox field, CommandSuggestions suggestor,
			double mouseX, double mouseY, int button) {
		if (owner == screen && target != null) {
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && contains(mouseX, mouseY)) {
				activate((int) ((mouseY - y) / ROW_HEIGHT), field, suggestor);
				closeMenu();
				return true;
			}
			closeMenu();
			return true;
		}
		if (button != GLFW.GLFW_MOUSE_BUTTON_RIGHT
				|| !ChatCanvasConfig.instance().mention().playerQuickActionsEnabled()
				|| field.isMouseOver(mouseX, mouseY)
				|| suggestionArea(suggestor).filter(area ->
						area.contains((int) mouseX, (int) mouseY)).isPresent()) {
			return false;
		}
		Optional<PlayerNameHitbox> hitbox = PlayerNameHitboxRegistry.findAt(mouseX, mouseY);
		if (hitbox.isEmpty()) return false;
		owner = screen;
		target = hitbox.get();
		position(screen, suggestor, (int) Math.round(mouseX), (int) Math.round(mouseY));
		return true;
	}

	public boolean keyPressed(
			ChatScreen screen, EditBox field, CommandSuggestions suggestor,
			int keyCode) {
		if (owner == screen && target != null && keyCode == GLFW.GLFW_KEY_ESCAPE) {
			closeMenu();
			return true;
		}
		if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_Z && replacedInput != null) {
			ChatFieldActions.restore(field, suggestor, replacedInput);
			replacedInput = null;
			return true;
		}
		return false;
	}

	public void render(ChatScreen screen, GuiGraphics context, int mouseX, int mouseY) {
		if (owner != screen || target == null) return;
		context.fill(x, y, x + WIDTH, y + HEIGHT, 0xEE171B24);
		context.drawBorder(x, y, WIDTH, HEIGHT, 0xFF59677C);
		for (int row = 0; row < 3; row++) {
			int rowY = y + row * ROW_HEIGHT;
			if (mouseX >= x && mouseX < x + WIDTH
					&& mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
				context.fill(x + 1, rowY + 1, x + WIDTH - 1,
						rowY + ROW_HEIGHT - 1, 0xAA3B4C66);
			}
			context.drawText(
					Minecraft.getInstance().textRenderer,
					Text.translatable(switch (row) {
						case 0 -> "chat_canvas.quick_action.mention";
						case 1 -> "chat_canvas.quick_action.private_message";
						default -> "chat_canvas.quick_action.copy_name";
					}),
					x + 7, rowY + 6, 0xFFF1F4FA, false);
		}
	}

	public void reset(ChatScreen screen) {
		if (owner == screen) {
			closeMenu();
			replacedInput = null;
		}
	}

	private void activate(int row, EditBox field, CommandSuggestions suggestor) {
		if (target == null) return;
		switch (row) {
			case 0 -> ChatFieldActions.insertMention(field, suggestor, target.playerName());
			case 1 -> replacedInput = ChatFieldActions.replace(
					field,
					suggestor,
					PrivateMessageTemplate.apply(
							ChatCanvasConfig.instance().mention().privateMessageTemplate(),
							target.playerName()));
			case 2 -> Minecraft.getInstance().keyboard.setClipboard(target.playerName());
			default -> {
			}
		}
	}

	private void position(ChatScreen screen, CommandSuggestions suggestor, int mouseX, int mouseY) {
		int maxX = Math.max(2, screen.width - WIDTH - 2);
		int maxY = Math.max(2, screen.height - HEIGHT - 2);
		int[][] candidates = {
				{mouseX + 4, mouseY + 4},
				{mouseX - WIDTH - 4, mouseY + 4},
				{mouseX + 4, mouseY - HEIGHT - 4},
				{mouseX - WIDTH - 4, mouseY - HEIGHT - 4}
		};
		ScreenRectangle suggestion = suggestionArea(suggestor).orElse(null);
		for (int[] candidate : candidates) {
			int candidateX = clamp(candidate[0], 2, maxX);
			int candidateY = clamp(candidate[1], 2, maxY);
			if (suggestion == null || !intersects(
					candidateX, candidateY, WIDTH, HEIGHT, suggestion)) {
				x = candidateX;
				y = candidateY;
				return;
			}
		}
		x = clamp(mouseX + 4, 2, maxX);
		y = clamp(mouseY + 4, 2, maxY);
	}

	private boolean contains(double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + WIDTH && mouseY >= y && mouseY < y + HEIGHT;
	}

	private void closeMenu() {
		owner = null;
		target = null;
	}

	private static Optional<ScreenRectangle> suggestionArea(CommandSuggestions suggestor) {
		CommandSuggestions.SuggestionWindow window =
				((ChatInputSuggestorAccessor) suggestor).chat_canvas$window();
		return window == null
				? Optional.empty()
				: Optional.of(((SuggestionWindowAccessor) window).chat_canvas$area());
	}

	private static boolean intersects(int x, int y, int width, int height, ScreenRectangle area) {
		return x < area.getX() + area.width() && x + width > area.getX()
				&& y < area.getY() + area.getHeight() && y + height > area.getY();
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
