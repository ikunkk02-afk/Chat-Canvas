package io.github.ikunkk02.chatcanvas.chat.identity;

import io.github.ikunkk02.chatcanvas.ChatCanvasForge;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageIngress;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Hooks Minecraft events → ChatCanvas internal models.
 * Replaces Fabric's ClientReceiveMessageEvents / ClientPlayConnectionEvents.
 */
@Mod.EventBusSubscriber(modid = ChatCanvasForge.MOD_ID, value = Dist.CLIENT)
public final class PlayerChatCapture {

    private PlayerChatCapture() {}

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        ChatCanvasMessageIngress.instance().acceptSentChat(
                event.getMessage());
    }

    @SubscribeEvent
    public static void onChatReceived(ClientChatReceivedEvent event) {
        if (event.getPacketType() == null) return;
        switch (event.getPacketType()) {
            case CHAT -> {
                ChatCanvasMessageIngress.instance().acceptReceivedChat(
                        event.getMessage(), event.getPacketSignature());
            }
            case SYSTEM -> {
                if (!event.isOverlay()) {
                    ChatCanvasMessageIngress.instance().acceptSystemMessage(
                            event.getMessage());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            ChatCanvasMessageIngress.instance().clearWorld();
            PlayerRosterTracker.clear();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        // Re-register event bus listener on world join
        // (auto-registered via @EventBusSubscriber)
    }

    static void init() {
        // Called from ChatCanvasForge to ensure class is loaded
    }
}
