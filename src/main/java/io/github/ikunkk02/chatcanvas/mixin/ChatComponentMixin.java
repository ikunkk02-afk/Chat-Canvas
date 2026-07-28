package io.github.ikunkk02.chatcanvas.mixin;

import io.github.ikunkk02.chatcanvas.ChatCanvasForge;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLayoutRuntime;
import io.github.ikunkk02.chatcanvas.chat.render.DualChatHudRenderer;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitboxRegistry;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.client.GuiMessageTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * Minimal HUD injection — defers to DualChatHudRenderer for custom rendering.
 * Falls back to vanilla ChatComponent on any failure.
 */
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @Shadow
    private Minecraft minecraft;

    @Shadow
    public abstract void addMessage(Component message, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void chat_canvas$renderCustomHud(GuiGraphics context, int tickCount,
                                              int mouseX, int mouseY, CallbackInfo ci) {
        if (!ChatCanvasConfig.instance().enabled()) return;

        try {
            boolean chatFocused = minecraft.screen instanceof net.minecraft.client.gui.screens.ChatScreen;
            ChatLayoutRuntime.tick(minecraft);
            if (DualChatHudRenderer.instance().render(context, mouseX, mouseY, chatFocused)) {
                ci.cancel();
            }
        } catch (Throwable t) {
            ChatCanvasForge.LOGGER.error("Chat Canvas HUD renderer failed; falling back to vanilla chat", t);
        }
    }

    /**
     * Intercept addMessage to feed messages into the ChatCanvas ingress.
     */
    @Inject(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
        at = @At("HEAD")
    )
    private void chat_canvas$captureMessage(Component message, MessageSignature signature,
                                             GuiMessageTag indicator, CallbackInfo ci) {
        io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageIngress.instance()
                .acceptFromChatHud(message, signature);
    }
}
