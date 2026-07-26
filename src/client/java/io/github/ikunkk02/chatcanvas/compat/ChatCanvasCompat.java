package io.github.ikunkk02.chatcanvas.compat;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import net.fabricmc.loader.api.FabricLoader;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ChatCanvasCompat {
	private static final Map<String, Boolean> DETECTED = new LinkedHashMap<>();

	private ChatCanvasCompat() {}

	public static void initialize() {
		if (!DETECTED.isEmpty()) return;
		FabricLoader loader = FabricLoader.getInstance();
		for (String id : new String[]{"chat_heads", "morechathistory", "chatanimation", "smoothscroll"}) {
			boolean present = loader.isModLoaded(id);
			DETECTED.put(id, present);
			if (present) {
				ChatCanvas.LOGGER.info("Chat Canvas compatibility active for {}: vanilla history/resources retained; custom channels remain isolated", id);
			}
		}
	}

	public static Map<String, Boolean> detected() {
		return Map.copyOf(DETECTED);
	}
}
