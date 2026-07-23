package io.github.ikunkk02.chatcanvas;

import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.editor.ChatCanvasEditorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ChatCanvasClient implements ClientModInitializer {
	private static KeyBinding openEditor;

	@Override
	public void onInitializeClient() {
		ChatCanvasConfig.initialize();
		openEditor = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.chat_canvas.open_editor",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_K,
				"key.category.chat_canvas"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openEditor.wasPressed()) {
				if (!(client.currentScreen instanceof ChatCanvasEditorScreen)) {
					client.setScreen(new ChatCanvasEditorScreen(client.currentScreen));
				}
			}
		});
	}
}
