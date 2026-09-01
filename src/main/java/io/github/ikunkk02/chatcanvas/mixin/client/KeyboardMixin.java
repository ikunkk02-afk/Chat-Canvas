package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.ChatCanvasClient;
import io.github.ikunkk02.chatcanvas.voice.ChatCanvasVoiceShortcutHost;
import io.github.ikunkk02.chatcanvas.voice.VoiceInputManager;
import io.github.ikunkk02.chatcanvas.voice.VoiceKeyEdge;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardMixin {
	@Unique
	private final VoiceKeyEdge chat_canvas$voiceKeyEdge = new VoiceKeyEdge();

	@Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$handleVoiceKeyEdge(long window, int keyCode, int scanCode,
											int action, int modifiers, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.getWindow() == null || window != client.getWindow().getWindow()) return;
		KeyMapping voiceKey = ChatCanvasClient.voiceInputKey();
		if (voiceKey == null || !voiceKey.matches(keyCode, scanCode)) return;

		if (action == GLFW.GLFW_RELEASE) {
			chat_canvas$voiceKeyEdge.release();
			return;
		}
		boolean eligible = client.screen == null || client.screen instanceof ChatScreen;
		if (action == GLFW.GLFW_REPEAT) {
			if (chat_canvas$voiceKeyEdge.repeat(eligible) == VoiceKeyEdge.Decision.CONSUME) ci.cancel();
			return;
		}
		if (action != GLFW.GLFW_PRESS) return;
		VoiceKeyEdge.Decision decision = chat_canvas$voiceKeyEdge.press(eligible);
		if (decision == VoiceKeyEdge.Decision.PASS) return;
		if (decision == VoiceKeyEdge.Decision.CONSUME) { ci.cancel(); return; }

		if (client.screen == null) {
			if (client.player == null) { chat_canvas$voiceKeyEdge.release(); return; }
			VoiceInputManager.instance().prepareQuickStart();
			client.setScreen(new ChatScreen(""));
		}
		if (client.screen instanceof ChatCanvasVoiceShortcutHost host) {
			host.chat_canvas$onVoiceShortcutPressed(keyCode, scanCode);
			ci.cancel();
		}
	}
}
