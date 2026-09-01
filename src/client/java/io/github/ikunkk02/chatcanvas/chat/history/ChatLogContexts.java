package io.github.ikunkk02.chatcanvas.chat.history;

import net.minecraft.client.Minecraft;

public final class ChatLogContexts {

    private ChatLogContexts() {}

    public static ChatLogContext current(Minecraft client) {
        if (client == null) return null;
        if (client.isSingleplayer() && client.getSingleplayerServer() != null) {
            String worldLabel = client.getSingleplayerServer().getWorldData().getLevelName();
            return ChatLogContext.singleplayer(worldLabel, worldLabel);
        }
        var entry = client.getCurrentServer();
        if (entry != null) {
            String address = entry.ip != null ? entry.ip : "unknown";
            String label = entry.name != null ? entry.name : address;
            return ChatLogContext.multiplayer(address, label);
        }
        return null;
    }
}
