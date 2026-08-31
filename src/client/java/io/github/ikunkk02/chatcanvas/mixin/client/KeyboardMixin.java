package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.ChatCanvasClient;
import io.github.ikunkk02.chatcanvas.voice.ChatCanvasVoiceShortcutHost;
import io.github.ikunkk02.chatcanvas.voice.VoiceInputManager;
import io.github.ikunkk02.chatcanvas.voice.VoiceKeyEdge;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
	@Unique
	private final VoiceKeyEdge chat_canvas$voiceKeyEdge = new VoiceKeyEdge();

	@Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$handleVoiceKeyEdge(long window, int action,
										KeyInput input, CallbackInfo ci) {
		int keyCode = input.getKeycode();
		int scanCode = input.scancode();
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.getWindow() == null || window != client.getWindow().getHandle()) return;
		KeyBinding voiceKey = ChatCanvasClient.voiceInputKey();
		if (voiceKey == null || !voiceKey.matchesKey(input)) return;

		if (action == GLFW.GLFW_RELEASE) {
			chat_canvas$voiceKeyEdge.release();
			return;
		}
		boolean eligible = client.currentScreen == null || client.currentScreen instanceof ChatScreen;
		if (action == GLFW.GLFW_REPEAT) {
			if (chat_canvas$voiceKeyEdge.repeat(eligible) == VoiceKeyEdge.Decision.CONSUME) ci.cancel();
			return;
		}
		if (action != GLFW.GLFW_PRESS) return;
		VoiceKeyEdge.Decision decision = chat_canvas$voiceKeyEdge.press(eligible);
		if (decision == VoiceKeyEdge.Decision.PASS) return;
		if (decision == VoiceKeyEdge.Decision.CONSUME) { ci.cancel(); return; }

		if (client.currentScreen == null) {
			if (client.player == null) { chat_canvas$voiceKeyEdge.release(); return; }
			VoiceInputManager.instance().prepareQuickStart();
			client.setScreen(new ChatScreen("", false));
		}
		if (client.currentScreen instanceof ChatCanvasVoiceShortcutHost host) {
			host.chat_canvas$onVoiceShortcutPressed(keyCode, scanCode);
			ci.cancel();
		}
	}
}
