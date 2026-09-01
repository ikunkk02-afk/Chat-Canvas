package io.github.ikunkk02.chatcanvas.chat.identity;

import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;

public final class PlayerColorRuntime {
	private static final PlayerNameColorProvider PROVIDER = new PlayerNameColorProvider();

	private PlayerColorRuntime() {
	}

	public static PlayerNameColorProvider provider() {
		PROVIDER.updateConfig(ChatCanvasConfig.instance().playerColors());
		return PROVIDER;
	}
}
