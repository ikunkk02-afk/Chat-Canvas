package io.github.ikunkk02.chatcanvas.chat.command;

import java.util.Locale;
import java.util.Set;

public final class SensitiveCommandDetector {
	private static final Set<String> SENSITIVE = Set.of(
			"login", "l", "register", "reg", "auth", "password", "passwd",
			"changepassword", "changepass", "cp");

	private SensitiveCommandDetector() {
	}

	public static boolean isSensitive(String command) {
		if (command == null) return false;
		String trimmed = command.stripLeading();
		if (!trimmed.startsWith("/")) return false;
		int end = 1;
		while (end < trimmed.length() && !Character.isWhitespace(trimmed.charAt(end))) end++;
		String token = trimmed.substring(1, end).toLowerCase(Locale.ROOT);
		int namespace = token.lastIndexOf(':');
		if (namespace >= 0) token = token.substring(namespace + 1);
		return SENSITIVE.contains(token);
	}
}
