package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.identity.ChatMessageMetadataRegistry;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitboxRegistry;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLineWidthCache;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatTextLayoutEngine;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageIngress;
import io.github.ikunkk02.chatcanvas.chat.render.DualChatHudRenderer;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bridges vanilla chat messages into Chat Canvas and replaces the 26.1 chat
 * extraction pass with the mod's dual-channel renderer while enabled.
 */
@Mixin(ChatComponent.class)
public abstract class ChatHudMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void chat_canvas$extractChatCanvas(
            GuiGraphicsExtractor context, Font font, int currentTick,
            int mouseX, int mouseY, ChatComponent.DisplayMode displayMode,
            boolean focused, CallbackInfo ci) {
        if (ChatCanvasConfig.instance().enabled()
                && DualChatHudRenderer.instance().render(context, mouseX, mouseY, focused)) {
            ci.cancel();
        }
    }

    @Inject(method = "addPlayerMessage", at = @At("HEAD"))
    private void chat_canvas$captureMessage(
            Component message, MessageSignature signature, GuiMessageTag indicator,
            CallbackInfo ci) {
        ChatCanvasMessageIngress.instance().acceptFromChatHud(message, signature);
    }

    @Inject(method = "addClientSystemMessage", at = @At("HEAD"))
    private void chat_canvas$captureClientSystemMessage(Component message, CallbackInfo ci) {
        ChatCanvasMessageIngress.instance().acceptFromChatHud(message, null);
    }

    @Inject(method = "addServerSystemMessage", at = @At("HEAD"))
    private void chat_canvas$captureServerSystemMessage(Component message, CallbackInfo ci) {
        ChatCanvasMessageIngress.instance().acceptFromChatHud(message, null);
    }

    @Inject(method = "clearMessages", at = @At("HEAD"))
    private void chat_canvas$clearMessageMetadata(boolean clearHistory, CallbackInfo ci) {
        ChatLineWidthCache.clear();
        ChatTextLayoutEngine.instance().clearWorld();
        ChatMessageMetadataRegistry.instance().clearAll();
        PlayerNameHitboxRegistry.clear();
        DualChatHudRenderer.instance().resetWorld();
    }

    @Inject(method = "rescaleChat", at = @At("HEAD"))
    private void chat_canvas$clearLayoutCaches(CallbackInfo ci) {
        ChatLineWidthCache.clear();
        ChatMessageMetadataRegistry.instance().clearVisible();
        DualChatHudRenderer.instance().invalidateLayouts();
    }
}
