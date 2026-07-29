package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.voice.ChatCanvasVoiceShortcutHost;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    @Inject(
            method = "onKey",
            at = @At("TAIL")
    )
    private void chat_canvas$handleVoiceKeyRelease(
            long window,
            int action,
            net.minecraft.client.input.KeyInput input,
            CallbackInfo ci
    ) {
        if (action != GLFW.GLFW_RELEASE) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.getWindow() == null) {
            return;
        }

        if (window != client.getWindow().getHandle()) {
            return;
        }

        if (!(client.currentScreen instanceof ChatScreen chatScreen)) {
            return;
        }

        if (chatScreen instanceof ChatCanvasVoiceShortcutHost host) {
            host.chat_canvas$onVoiceShortcutReleased(input.key(), input.scancode());
        }
    }
}
