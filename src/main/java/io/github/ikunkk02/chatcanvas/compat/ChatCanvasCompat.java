package io.github.ikunkk02.chatcanvas.compat;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import io.github.ikunkk02.chatcanvas.chat.text.ChatHeadsCompat;
import net.neoforged.fml.ModList;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ChatCanvasCompat {
	private static final Map<String, Boolean> DETECTED = new LinkedHashMap<>();

	private ChatCanvasCompat() {}

	public static void initialize() {
		if (!DETECTED.isEmpty()) return;
		ModList loader = ModList.get();
		for (String id : new String[]{"chat_heads", "morechathistory", "chatanimation", "smoothscroll"}) {
			boolean present = loader.isLoaded(id);
			DETECTED.put(id, present);
			if (present) {
				if (id.equals("chat_heads")) {
					if (ChatHeadsCompat.channelAdapterAvailable()) {
						ChatCanvas.LOGGER.info(
								"Chat Canvas compatibility active for chat_heads: custom player channel avatar adapter ready");
					} else {
						ChatCanvas.LOGGER.warn(
								"Chat Heads detected, but its avatar API is unavailable; using text-only player chat");
					}
				} else {
					ChatCanvas.LOGGER.info(
							"Chat Canvas compatibility active for {}: vanilla history/resources retained; custom channels remain isolated",
							id);
				}
			}
		}
	}

	public static Map<String, Boolean> detected() {
		return Map.copyOf(DETECTED);
	}
}
