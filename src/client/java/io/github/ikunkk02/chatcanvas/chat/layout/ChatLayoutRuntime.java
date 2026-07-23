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
	private static int lastInternalWrapWidth = -1;

	private ChatLayoutRuntime() {
	}

	public static ChatHudTransform currentTransform() {
		return currentTransform(MinecraftClient.getInstance());
	}

	public static ChatHudTransform currentTransform(MinecraftClient client) {
		int width = Math.max(1, client.getWindow().getScaledWidth());
		int height = Math.max(1, client.getWindow().getScaledHeight());
		PixelLayout layout = ChatCanvasConfig.instance().layout().toPixels(width, height);
		double scale = client.options.getChatScale().getValue();
		boolean chatOpen = client.currentScreen instanceof ChatScreen;
		int inputHeight = 0;
		if (chatOpen) {
			TextFieldWidget chatField = ((ChatScreenAccessor) client.currentScreen).chat_canvas$chatField();
			if (chatField != null) {
				inputHeight = chatField.getHeight();
			}
		}
		double lineSpacing = client.options.getChatLineSpacing().getValue();
		int internalLineHeight = Math.max(1,
				(int) (client.textRenderer.fontHeight * (lineSpacing + 1.0)));
		int minimumMessageHeight = Math.max(1, (int) Math.ceil(internalLineHeight * scale));
		RuntimeChatBounds bounds = RuntimeChatBounds.calculate(
				layout,
				chatOpen,
				inputHeight,
				RuntimeChatBounds.DEFAULT_INPUT_GAP,
				minimumMessageHeight
		);
		return new ChatHudTransform(layout, height, scale, bounds);
	}

	public static void tick(MinecraftClient client) {
		if (client.inGameHud == null) return;
		ChatHudTransform transform = currentTransform(client);
		int wrapWidth = transform.internalWrapWidth();
		if (lastInternalWrapWidth == -1) {
			lastInternalWrapWidth = wrapWidth;
		} else if (lastInternalWrapWidth != wrapWidth) {
			refresh(client.inGameHud.getChatHud());
			lastInternalWrapWidth = wrapWidth;
		}
	}

	public static void applySavedLayout() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.inGameHud == null) return;
		ChatHudTransform transform = currentTransform(client);
		refresh(client.inGameHud.getChatHud());
		lastInternalWrapWidth = transform.internalWrapWidth();
	}

	private static void refresh(ChatHud chatHud) {
		((ChatHudAccessor) chatHud).chat_canvas$refresh();
	}
}
