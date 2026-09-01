package io.github.ikunkk02.chatcanvas.chat.notification;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

/**
 * Development-only escape hatch for exercising the complete mention delivery
 * path with one client. Production multiplayer behavior still rejects own
 * messages.
 */
public final class MentionDebugPolicy {
	private static final String SELF_MENTION_PROPERTY =
			"chatcanvas.debug.allowSelfMentionInSingleplayer";

	private MentionDebugPolicy() {}

	public static boolean allowsSelfMention(Minecraft client) {
		if (client == null || !client.isSingleplayer()) return false;
		return FabricLoader.getInstance().isDevelopmentEnvironment()
				|| Boolean.getBoolean(SELF_MENTION_PROPERTY);
	}
}
