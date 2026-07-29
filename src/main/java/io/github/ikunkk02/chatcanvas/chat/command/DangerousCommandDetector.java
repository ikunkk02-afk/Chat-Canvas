package io.github.ikunkk02.chatcanvas.chat.command;

import java.util.Locale;
import java.util.Set;

public final class DangerousCommandDetector {
	private static final Set<String> RISKY = Set.of(
			"kill", "clear", "fill", "setblock", "execute", "op", "deop",
			"ban", "ban-ip", "kick", "whitelist", "stop");

	private DangerousCommandDetector() {
	}

	public static boolean mayChangeWorldOrPlayers(String command) {
		String name = CommandTextSanitizer.commandName(command).toLowerCase(Locale.ROOT);
		int namespace = name.lastIndexOf(':');
		if (namespace >= 0) name = name.substring(namespace + 1);
		return RISKY.contains(name);
	}
}
