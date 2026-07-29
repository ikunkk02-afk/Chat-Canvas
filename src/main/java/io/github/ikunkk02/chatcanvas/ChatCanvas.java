package io.github.ikunkk02.chatcanvas;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChatCanvas implements ModInitializer {
	public static final String MOD_ID = "chat_canvas";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Chat Canvas");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
