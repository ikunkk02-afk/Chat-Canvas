package io.github.ikunkk02.chatcanvas.mixin;

import io.github.ikunkk02.chatcanvas.voice.ChatCanvasVoiceShortcutHost;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("TAIL"))
    private void chat_canvas$handleVoiceKeyRelease(
            long window, int keyCode, int scanCode, int action, int modifiers,
            CallbackInfo ci) {
        if (action != GLFW.GLFW_RELEASE) return;

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) return;
        if (window != client.getWindow().getWindow()) return;

        if (client.screen instanceof ChatCanvasVoiceShortcutHost host) {
            host.chat_canvas$onVoiceShortcutReleased(keyCode, scanCode);
        }
    }
}
