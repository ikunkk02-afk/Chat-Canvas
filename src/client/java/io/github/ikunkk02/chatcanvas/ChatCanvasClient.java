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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ChatCanvasClient implements ClientModInitializer {
	private static KeyMapping openEditor;
	private static KeyMapping voiceInput;
	private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(ChatCanvas.id("key_category"));

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
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
				VoiceInputManager.instance().warmSelectedModel());
		openEditor = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.chat_canvas.open_editor",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_K,
				KEY_CATEGORY
		));
		voiceInput = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.chat_canvas.voice_input",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_V,
				KEY_CATEGORY
		));
		ResourceLoader.get(PackType.CLIENT_RESOURCES)
				.registerReloadListener(ChatCanvas.id("chat_text_metrics"),
						new SimpleReloadListener<Void>() {
							@Override
							protected Void prepare(
									net.minecraft.server.packs.resources.PreparableReloadListener.SharedState state) {
								return null;
							}

							@Override
							protected void apply(Void ignored,
									net.minecraft.server.packs.resources.PreparableReloadListener.SharedState state) {
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
			if (client.screen instanceof ChatCanvasInputScreenBridge bridge) {
				bridge.chat_canvas$voiceTick();
			} else if (VoiceInputManager.instance().isBusy()) {
				VoiceInputManager.instance().cancel();
			}
			if (client.screen instanceof net.minecraft.client.gui.screens.ChatScreen chatScreen) {
				PlayerNameDoubleClickHandler.instance().tick(chatScreen);
			} else {
				PlayerNameDoubleClickHandler.instance().reset();
			}
			while (openEditor.consumeClick()) {
				if (!(client.screen instanceof ChatCanvasEditorScreen)) {
					client.setScreen(EditorScreenFactory.create(client.screen));
				}
			}
		});
	}

	public static KeyMapping voiceInputKey() {
		return voiceInput;
	}
}
