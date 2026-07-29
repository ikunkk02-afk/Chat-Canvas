package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputMode;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputScreenBridge;
import io.github.ikunkk02.chatcanvas.mixin.client.ChatHudAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;

public final class ChatLayoutRuntime {
	private static RefreshSignature lastRefreshSignature;

	private ChatLayoutRuntime() {
	}

	public static ChatHudTransform currentTransform() {
		return currentTransform(MinecraftClient.getInstance());
	}

	public static ChatHudTransform currentTransform(MinecraftClient client) {
		int width = Math.max(1, client.getWindow().getScaledWidth());
		int height = Math.max(1, client.getWindow().getScaledHeight());
		PixelLayout layout = ChatCanvasConfig.instance().layout().toPixels(width, height);
		double vanillaScale = client.options.getChatScale().getValue();
		double configuredScale = ChatCanvasConfig.instance().text().fontScale();
		boolean chatOpen = client.currentScreen instanceof ChatScreen;
		int inputHeight = 0;
		if (chatOpen
				&& client.currentScreen instanceof ChatCanvasInputScreenBridge bridge
				&& bridge.chat_canvas$inputMode() == ChatCanvasInputMode.PLAYER_CHAT) {
			TextFieldWidget chatField = bridge.chat_canvas$activeInputField();
			if (chatField != null) {
				inputHeight = chatField.getHeight();
			}
		}
		double vanillaLineSpacing = client.options.getChatLineSpacing().getValue();
		int vanillaLineHeight = Math.max(1,
				(int) (client.textRenderer.fontHeight * (vanillaLineSpacing + 1.0)));
		int internalLineHeight = ChatTextLayout.internalLineHeight(
				vanillaLineHeight, configuredScale,
				ChatCanvasConfig.instance().text().lineSpacing());
		int minimumMessageHeight = Math.max(1,
				(int) Math.ceil(internalLineHeight * vanillaScale));
		RuntimeChatBounds bounds = RuntimeChatBounds.calculate(
				layout,
				chatOpen,
				inputHeight,
				RuntimeChatBounds.DEFAULT_INPUT_GAP,
				minimumMessageHeight
		);
		return new ChatHudTransform(layout, height, vanillaScale, configuredScale, bounds);
	}

	public static void tick(MinecraftClient client) {
		if (client.inGameHud == null) return;
		ChatHudTransform transform = currentTransform(client);
		RefreshSignature signature = RefreshSignature.from(transform);
		if (lastRefreshSignature == null) {
			lastRefreshSignature = signature;
		} else if (!lastRefreshSignature.equals(signature)) {
			ChatTextLayoutEngine.instance().invalidateLayout();
			refresh(client.inGameHud.getChatHud());
			lastRefreshSignature = signature;
		}
	}

	public static void applySavedSettings() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.inGameHud == null) return;
		ChatHudTransform transform = currentTransform(client);
		RefreshSignature signature = RefreshSignature.from(transform);
		if (lastRefreshSignature == null || !lastRefreshSignature.equals(signature)) {
			ChatTextLayoutEngine.instance().invalidateLayout();
			refresh(client.inGameHud.getChatHud());
		}
		lastRefreshSignature = signature;
	}

	public static void applySavedLayout() {
		applySavedSettings();
	}

	public static void onFontResourcesReloaded() {
		lastRefreshSignature = null;
		ChatTextLayoutEngine.instance().invalidateLayout();
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.inGameHud != null) {
			refresh(client.inGameHud.getChatHud());
			lastRefreshSignature = RefreshSignature.from(currentTransform(client));
		}
	}

	private static void refresh(ChatHud chatHud) {
		((ChatHudAccessor) chatHud).chat_canvas$refresh();
	}

	private record RefreshSignature(int internalWrapWidth, int horizontalPadding,
									long effectiveScaleBits, long characterSpacingBits,
									long lineSpacingBits,
									String localPlayerName,
									boolean requireAtSymbol) {
		private static RefreshSignature from(ChatHudTransform transform) {
			int horizontalPadding = ChatCanvasConfig.instance().background().horizontalPadding();
			MinecraftClient client = MinecraftClient.getInstance();
			String localPlayerName = client.player == null
					? ""
					: client.player.getGameProfile().name().toLowerCase(java.util.Locale.ROOT);
			return new RefreshSignature(
					ChatTextLayout.glyphWrapWidth(
							transform.configuredWidth(),
							horizontalPadding,
							glyphSafetyPixels(),
							transform.vanillaChatScale(),
							transform.configuredFontScale()),
					horizontalPadding,
					Double.doubleToLongBits(transform.effectiveChatScale()),
					Double.doubleToLongBits(
							ChatCanvasConfig.instance().text().characterSpacing()),
					Double.doubleToLongBits(
							ChatCanvasConfig.instance().text().lineSpacing()),
					localPlayerName,
					ChatCanvasConfig.instance().mention().requireAtSymbol()
			);
		}

		private static int glyphSafetyPixels() {
			return ChatCanvasConfig.instance().text().shadow() ? 2 : 1;
		}
	}
}
