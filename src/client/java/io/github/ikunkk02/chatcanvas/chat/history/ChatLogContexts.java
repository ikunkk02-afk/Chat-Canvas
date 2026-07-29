package io.github.ikunkk02.chatcanvas.chat.history;

import net.minecraft.client.Minecraft;

public final class ChatLogContexts {

    private ChatLogContexts() {}

    public static ChatLogContext current(MinecraftClient client) {
        if (client == null) return null;
        if (client.isInSingleplayer() && client.getServer() != null) {
            String worldLabel = client.getServer().getSaveProperties().getLevelName();
            return ChatLogContext.singleplayer(worldLabel, worldLabel);
        }
        var entry = client.getCurrentServerEntry();
        if (entry != null) {
            String address = entry.address != null ? entry.address : "unknown";
            String label = entry.name != null ? entry.name : address;
            return ChatLogContext.multiplayer(address, label);
        }
        return null;
    }
}
