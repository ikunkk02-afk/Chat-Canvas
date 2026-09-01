package io.github.ikunkk02.chatcanvas;

import com.mojang.blaze3d.platform.InputConstants;
import com.sun.jna.Native;
import io.github.ikunkk02.chatcanvas.chat.command.CommandToolRuntime;
import io.github.ikunkk02.chatcanvas.chat.emoji.EmojiFontSupport;
import io.github.ikunkk02.chatcanvas.chat.emoji.EmojiRuntime;
import io.github.ikunkk02.chatcanvas.chat.history.ChatLogConfigStorage;
import io.github.ikunkk02.chatcanvas.chat.history.LocalChatLogService;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatCapture;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputScreenBridge;
import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerNameDoubleClickHandler;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLayoutRuntime;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLineWidthCache;
import io.github.ikunkk02.chatcanvas.chat.notification.MentionNotificationController;
import io.github.ikunkk02.chatcanvas.chat.render.DualChatHudRenderer;
import io.github.ikunkk02.chatcanvas.chat.text.GlyphAdvanceCache;
import io.github.ikunkk02.chatcanvas.compat.ChatCanvasCompat;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.editor.ChatCanvasEditorScreen;
import io.github.ikunkk02.chatcanvas.editor.EditorScreenFactory;
import io.github.ikunkk02.chatcanvas.voice.VoiceEncodingDiagnostics;
import io.github.ikunkk02.chatcanvas.voice.VoiceInputManager;
import io.github.ikunkk02.chatcanvas.voice.VoskEncodingBootstrap;
import java.nio.charset.Charset;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.lwjgl.glfw.GLFW;

public final class ChatCanvasClient {
	private static KeyMapping openEditor;
	private static KeyMapping voiceInput;

	private ChatCanvasClient() {}

	public static void initialize(IEventBus modBus) {
		VoskEncodingBootstrap.initialize();
		if (VoiceEncodingDiagnostics.enabled()) {
			ChatCanvas.LOGGER.info(
					"Voice encoding bootstrap initialized: defaultCharset={}, file.encoding={}, "
							+ "native.encoding={}, jna.encoding={}, jna.defaultStringEncoding={}",
					Charset.defaultCharset(), System.getProperty("file.encoding"),
					System.getProperty("native.encoding"), System.getProperty("jna.encoding"),
					Native.getDefaultStringEncoding());
		}
		ChatCanvasConfig.initialize();
		EmojiRuntime.initialize();
		ChatCanvasCompat.initialize();
		MentionNotificationController.instance().register();
		PlayerChatCapture.register();
		LocalChatLogService.instance().updateConfig(new ChatLogConfigStorage().load());

		openEditor = new KeyMapping("key.chat_canvas.open_editor", InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_K, "key.category.chat_canvas");
		voiceInput = new KeyMapping("key.chat_canvas.voice_input", InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_V, "key.category.chat_canvas");
		modBus.addListener((RegisterKeyMappingsEvent event) -> {
			event.register(openEditor);
			event.register(voiceInput);
		});
		modBus.addListener((RegisterClientReloadListenersEvent event) ->
				event.registerReloadListener((ResourceManagerReloadListener) manager -> {
					ChatLineWidthCache.clear();
					GlyphAdvanceCache.onFontResourcesReloaded();
					EmojiFontSupport.onFontResourcesReloaded();
					ChatLayoutRuntime.onFontResourcesReloaded();
					DualChatHudRenderer.instance().invalidateLayouts();
				}));

		NeoForge.EVENT_BUS.addListener((GameShuttingDownEvent event) -> {
			CommandToolRuntime.manager().flush();
			EmojiRuntime.flush();
			VoiceInputManager.instance().shutdown();
			LocalChatLogService.instance().close();
		});
		NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) ->
				VoiceInputManager.instance().warmSelectedModel());
		NeoForge.EVENT_BUS.addListener(ChatCanvasClient::onClientTick);
	}

	private static void onClientTick(ClientTickEvent.Post event) {
		Minecraft client = Minecraft.getInstance();
		ChatLayoutRuntime.tick(client);
		CommandToolRuntime.manager().tick(System.currentTimeMillis());
		EmojiRuntime.tick(client);
		if (client.screen instanceof ChatCanvasInputScreenBridge bridge) {
			bridge.chat_canvas$voiceTick();
		} else if (VoiceInputManager.instance().isBusy()) {
			VoiceInputManager.instance().cancel();
		}
		if (client.screen instanceof ChatScreen chatScreen) {
			PlayerNameDoubleClickHandler.instance().tick(chatScreen);
		} else {
			PlayerNameDoubleClickHandler.instance().reset();
		}
		while (openEditor.consumeClick()) {
			if (!(client.screen instanceof ChatCanvasEditorScreen)) {
				client.setScreen(EditorScreenFactory.create(client.screen));
			}
		}
	}

	public static KeyMapping voiceInputKey() {
		return voiceInput;
	}
}
