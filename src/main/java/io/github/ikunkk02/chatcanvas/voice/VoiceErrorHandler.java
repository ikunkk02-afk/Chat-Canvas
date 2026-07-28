package io.github.ikunkk02.chatcanvas.voice;

import io.github.ikunkk02.chatcanvas.ChatCanvasForge;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageIngress;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

public final class VoiceErrorHandler {
	private final Map<String, Long> lastShown = new HashMap<>();

	public synchronized void report(String key, Throwable error) {
		long now = System.currentTimeMillis();
		if (now - lastShown.getOrDefault(key, 0L) < 5_000L) {
			if (error != null) ChatCanvasForge.LOGGER.debug("Repeated voice error: {}", key, error);
			return;
		}
		lastShown.put(key, now);
		ChatCanvasMessageIngress.instance().reportError(Component.translatable(key), error);
	}
}
