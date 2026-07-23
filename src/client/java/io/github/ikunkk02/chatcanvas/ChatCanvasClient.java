package io.github.ikunkk02.chatcanvas;

import io.github.ikunkk02.chatcanvas.chat.layout.ChatLineWidthCache;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLayoutRuntime;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatCapture;
import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerNameDoubleClickHandler;
import io.github.ikunkk02.chatcanvas.chat.notification.MentionNotificationController;
import io.github.ikunkk02.chatcanvas.chat.command.CommandClipboardManager;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.editor.ChatCanvasEditorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ChatCanvasClient implements ClientModInitializer {
	private static KeyBinding openEditor;

	@Override
	public void onInitializeClient() {
		ChatCanvasConfig.initialize();
		MentionNotificationController.instance().register();
		PlayerChatCapture.register();
		ClientLifecycleEvents.CLIENT_STOPPING.register(
				client -> CommandClipboardManager.instance().flush());
		openEditor = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.chat_canvas.open_editor",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_K,
				"key.category.chat_canvas"
		));
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
				.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return Identifier.of(ChatCanvas.MOD_ID, "chat_text_metrics");
					}

					@Override
					public void reload(ResourceManager manager) {
						ChatLineWidthCache.clear();
						ChatLayoutRuntime.onFontResourcesReloaded();
					}
				});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ChatLayoutRuntime.tick(client);
			CommandClipboardManager.instance().tick(System.currentTimeMillis());
			if (client.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen chatScreen) {
				PlayerNameDoubleClickHandler.instance().tick(chatScreen);
			} else {
				PlayerNameDoubleClickHandler.instance().reset();
			}
			while (openEditor.wasPressed()) {
				if (!(client.currentScreen instanceof ChatCanvasEditorScreen)) {
					client.setScreen(new ChatCanvasEditorScreen(client.currentScreen));
				}
			}
		});
	}
}
