package io.github.ikunkk02.chatcanvas.chat.interaction;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitbox;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitboxRegistry;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerColorConfig;
import io.github.ikunkk02.chatcanvas.mixin.client.ChatInputSuggestorAccessor;
import io.github.ikunkk02.chatcanvas.mixin.client.SuggestionWindowAccessor;
import io.github.ikunkk02.chatcanvas.mixin.client.TextFieldWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public final class PlayerNameDoubleClickHandler {
	private static final long FEEDBACK_DURATION_MS = 1_200L;
	private static final PlayerNameDoubleClickHandler INSTANCE =
			new PlayerNameDoubleClickHandler();

	private final MentionInteractionState state = new MentionInteractionState();
	private Text feedback;
	private long feedbackUntilMs;

	private PlayerNameDoubleClickHandler() {
	}

	public static PlayerNameDoubleClickHandler instance() {
		return INSTANCE;
	}

	public boolean mouseClicked(
			ChatScreen screen,
			TextFieldWidget chatField,
			ChatInputSuggestor suggestor,
			double mouseX,
			double mouseY,
			int button
	) {
		MentionConfig config = ChatCanvasConfig.instance().mention();
		if (!config.doubleClickEnabled()) {
			reset();
			return false;
		}
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			state.reset();
			return false;
		}
		if (chatField.isMouseOver(mouseX, mouseY)
				|| isSuggestionWindowAt(suggestor, mouseX, mouseY)) {
			state.reset();
			return false;
		}
		Optional<PlayerNameHitbox> hitbox = PlayerNameHitboxRegistry.findAt(mouseX, mouseY);
		if (hitbox.isEmpty()) {
			state.reset();
			return false;
		}
		long now = Util.getMeasuringTimeMs();
		if (!state.click(hitbox.get(), now, mouseX, mouseY,
				config.doubleClickIntervalMs(), screen)) {
			return false;
		}
		if (isLocalPlayer(hitbox.get())) {
			showFeedback("chat_canvas.mention.cannot_mention_self", now);
			return true;
		}
		TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor) chatField;
		MentionInsertionController.Result insertion = MentionInsertionController.plan(
				chatField.getText(),
				chatField.getCursor(),
				accessor.chat_canvas$selectionEnd(),
				accessor.chat_canvas$maxLength(),
				hitbox.get().playerName());
		if (!insertion.successful()) {
			showFeedback("chat_canvas.mention.input_too_long", now);
			return true;
		}
		String previous = chatField.getText();
		int previousCursor = chatField.getCursor();
		int previousSelectionEnd = accessor.chat_canvas$selectionEnd();
		chatField.setText(insertion.text());
		if (!insertion.text().equals(chatField.getText())) {
			chatField.setText(previous);
			chatField.setSelectionStart(previousCursor);
			chatField.setSelectionEnd(previousSelectionEnd);
			showFeedback("chat_canvas.mention.input_too_long", now);
			return true;
		}
		chatField.setCursor(insertion.cursorUtf16(), false);
		chatField.setFocused(true);
		suggestor.refresh();
		clearFeedback();
		return true;
	}

	public void tick(ChatScreen screen) {
		MentionConfig config = ChatCanvasConfig.instance().mention();
		long now = Util.getMeasuringTimeMs();
		if (!config.doubleClickEnabled()
				|| MinecraftClient.getInstance().currentScreen != screen
				|| !MinecraftClient.getInstance().isWindowFocused()) {
			state.reset();
		} else {
			state.expire(now, config.doubleClickIntervalMs());
		}
		if (feedback != null && now > feedbackUntilMs) clearFeedback();
	}

	public Optional<Text> feedback() {
		if (feedback == null || Util.getMeasuringTimeMs() > feedbackUntilMs) {
			clearFeedback();
			return Optional.empty();
		}
		return Optional.of(feedback);
	}

	public void reset() {
		state.reset();
		clearFeedback();
	}

	private static boolean isSuggestionWindowAt(
			ChatInputSuggestor suggestor, double mouseX, double mouseY) {
		ChatInputSuggestor.SuggestionWindow window =
				((ChatInputSuggestorAccessor) suggestor).chat_canvas$window();
		return window != null && ((SuggestionWindowAccessor) window).chat_canvas$area()
				.contains((int) Math.floor(mouseX), (int) Math.floor(mouseY));
	}

	private static boolean isLocalPlayer(PlayerNameHitbox hitbox) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) return false;
		if (hitbox.playerUuid() != null) {
			return hitbox.playerUuid().equals(client.player.getUuid());
		}
		return PlayerColorConfig.normalizeName(hitbox.playerName()).equals(
				PlayerColorConfig.normalizeName(client.player.getGameProfile().getName()));
	}

	private void showFeedback(String translationKey, long now) {
		feedback = Text.translatable(translationKey);
		feedbackUntilMs = now + FEEDBACK_DURATION_MS;
	}

	private void clearFeedback() {
		feedback = null;
		feedbackUntilMs = 0L;
	}
}
