package io.github.ikunkk02.chatcanvas;

import com.sun.jna.Native;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLineWidthCache;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLayoutRuntime;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatCapture;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputScreenBridge;
import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerNameDoubleClickHandler;
import io.github.ikunkk02.chatcanvas.chat.notification.MentionNotificationController;
import io.github.ikunkk02.chatcanvas.chat.render.DualChatHudRenderer;
import io.github.ikunkk02.chatcanvas.chat.command.CommandToolRuntime;
import io.github.ikunkk02.chatcanvas.chat.history.ChatLogConfigStorage;
import io.github.ikunkk02.chatcanvas.chat.history.LocalChatLogService;
import io.github.ikunkk02.chatcanvas.chat.emoji.EmojiFontSupport;
import io.github.ikunkk02.chatcanvas.chat.emoji.EmojiRuntime;
import io.github.ikunkk02.chatcanvas.chat.text.GlyphAdvanceCache;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.compat.ChatCanvasCompat;
import io.github.ikunkk02.chatcanvas.editor.ChatCanvasEditorScreen;
import io.github.ikunkk02.chatcanvas.editor.EditorScreenFactory;
import io.github.ikunkk02.chatcanvas.voice.VoiceInputManager;
import io.github.ikunkk02.chatcanvas.voice.VoskEncodingBootstrap;
import io.github.ikunkk02.chatcanvas.voice.VoiceEncodingDiagnostics;
import java.nio.charset.Charset;
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
	private static KeyBinding voiceInput;

	@Override
	public void onInitializeClient() {
		VoskEncodingBootstrap.initialize();
		if (VoiceEncodingDiagnostics.enabled()) {
			ChatCanvas.LOGGER.info(
					"Voice encoding bootstrap initialized: defaultCharset={}, "
							+ "file.encoding={}, native.encoding={}, jna.encoding={}, "
							+ "jna.defaultStringEncoding={}",
					Charset.defaultCharset(),
					System.getProperty("file.encoding"),
					System.getProperty("native.encoding"),
					System.getProperty("jna.encoding"),
					Native.getDefaultStringEncoding());
		}
		ChatCanvasConfig.initialize();
		EmojiRuntime.initialize();
		ChatCanvasCompat.initialize();
		MentionNotificationController.instance().register();
		PlayerChatCapture.register();
		{
			ChatLogConfigStorage logConfigStorage = new ChatLogConfigStorage();
			LocalChatLogService.instance().updateConfig(logConfigStorage.load());
		}
		ClientLifecycleEvents.CLIENT_STOPPING.register(
				client -> {
					CommandToolRuntime.manager().flush();
					EmojiRuntime.flush();
					VoiceInputManager.instance().shutdown();
					LocalChatLogService.instance().close();
				});
		openEditor = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.chat_canvas.open_editor",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_K,
				"key.category.chat_canvas"
		));
		voiceInput = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.chat_canvas.voice_input",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_V,
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
						GlyphAdvanceCache.onFontResourcesReloaded();
						EmojiFontSupport.onFontResourcesReloaded();
						ChatLayoutRuntime.onFontResourcesReloaded();
						DualChatHudRenderer.instance().invalidateLayouts();
					}
				});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ChatLayoutRuntime.tick(client);
			CommandToolRuntime.manager().tick(System.currentTimeMillis());
			EmojiRuntime.tick(client);
			if (client.currentScreen instanceof ChatCanvasInputScreenBridge bridge) {
				bridge.chat_canvas$voiceTick();
			} else if (VoiceInputManager.instance().isBusy()) {
				VoiceInputManager.instance().cancel();
			}
			if (client.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen chatScreen) {
				PlayerNameDoubleClickHandler.instance().tick(chatScreen);
			} else {
				PlayerNameDoubleClickHandler.instance().reset();
			}
			while (openEditor.wasPressed()) {
				if (!(client.currentScreen instanceof ChatCanvasEditorScreen)) {
					client.setScreen(EditorScreenFactory.create(client.currentScreen));
				}
			}
		});
	}

	public static KeyBinding voiceInputKey() {
		return voiceInput;
	}
}
