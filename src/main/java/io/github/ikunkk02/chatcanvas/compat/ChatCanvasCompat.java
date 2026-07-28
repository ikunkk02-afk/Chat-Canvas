package io.github.ikunkk02.chatcanvas.compat;

import io.github.ikunkk02.chatcanvas.ChatCanvasForge;
import io.github.ikunkk02.chatcanvas.chat.text.ChatHeadsCompat;
import net.minecraftforge.fml.ModList;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ChatCanvasCompat {
    private static final Map<String, Boolean> DETECTED = new LinkedHashMap<>();

    private ChatCanvasCompat() {}

    public static void initialize() {
        if (!DETECTED.isEmpty()) return;
        for (String id : new String[]{"chat_heads", "morechathistory", "chatanimation", "smoothscroll"}) {
            boolean present = ModList.get().isLoaded(id);
            DETECTED.put(id, present);
            if (present) {
                if (id.equals("chat_heads")) {
                    if (ChatHeadsCompat.channelAdapterAvailable()) {
                        ChatCanvasForge.LOGGER.info("Chat Canvas Forge: chat_heads detected — avatar adapter active");
                    } else {
                        ChatCanvasForge.LOGGER.warn("Chat Heads detected but avatar API unavailable; using text-only");
                    }
                } else {
                    ChatCanvasForge.LOGGER.info("Chat Canvas Forge: {} detected — isolated custom channels remain", id);
                }
            }
        }
    }

    public static Map<String, Boolean> detected() {
        return Map.copyOf(DETECTED);
    }
}
