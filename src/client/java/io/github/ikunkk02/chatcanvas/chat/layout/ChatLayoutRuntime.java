package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import io.github.ikunkk02.chatcanvas.mixin.client.ChatHudAccessor;
import io.github.ikunkk02.chatcanvas.mixin.client.ChatScreenAccessor;
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
		double effectiveScale = ChatTextLayout.effectiveScale(vanillaScale, configuredScale);
		boolean chatOpen = client.currentScreen instanceof ChatScreen;
		int inputHeight = 0;
		if (chatOpen) {
			TextFieldWidget chatField = ((ChatScreenAccessor) client.currentScreen).chat_canvas$chatField();
			if (chatField != null) {
				inputHeight = chatField.getHeight();
			}
		}
		double vanillaLineSpacing = client.options.getChatLineSpacing().getValue();
		int vanillaLineHeight = Math.max(1,
				(int) (client.textRenderer.fontHeight * (vanillaLineSpacing + 1.0)));
		int internalLineHeight = ChatTextLayout.internalLineHeight(
				vanillaLineHeight, ChatCanvasConfig.instance().text().lineSpacing());
		int minimumMessageHeight = Math.max(1, (int) Math.ceil(internalLineHeight * effectiveScale));
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
			refresh(client.inGameHud.getChatHud());
		}
		lastRefreshSignature = signature;
	}

	public static void applySavedLayout() {
		applySavedSettings();
	}

	public static void onFontResourcesReloaded() {
		lastRefreshSignature = null;
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
									long effectiveScaleBits) {
		private static RefreshSignature from(ChatHudTransform transform) {
			int horizontalPadding = ChatCanvasConfig.instance().background().horizontalPadding();
			return new RefreshSignature(
					ChatBackgroundMetrics.wrapWidth(
							transform.internalWrapWidth(),
							horizontalPadding,
							transform.effectiveChatScale()
					),
					horizontalPadding,
					Double.doubleToLongBits(transform.effectiveChatScale())
			);
		}
	}
}
