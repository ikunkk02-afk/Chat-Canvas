package io.github.ikunkk02.chatcanvas.chat.command;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class ClipboardCommandParser {
	private static final int MAX_CANDIDATES = 64;
	private static final int PREVIEW_LENGTH = 96;
	private static final Pattern COMMAND_HEAD =
			Pattern.compile("[A-Za-z0-9_.:+-]{1,64}(?:\\s+.*)?");

	private ClipboardCommandParser() {
	}

	public static ClipboardCommandParseResult parse(String clipboard) {
		if (clipboard == null || clipboard.isBlank()) {
			return new ClipboardCommandParseResult(List.of(), false, false);
		}
		String[] lines = clipboard.split("\\R", -1);
		List<ClipboardCommandCandidate> candidates = new ArrayList<>();
		boolean truncated = false;
		for (String line : lines) {
			String cleaned = CommandTextSanitizer.normalizeForClipboard(line);
			if (cleaned.isBlank()) continue;
			boolean slash = cleaned.startsWith("/");
			String withoutSlash = slash ? cleaned.substring(1) : cleaned;
			if (withoutSlash.isBlank() || !COMMAND_HEAD.matcher(withoutSlash).matches()) {
				continue;
			}
			if (candidates.size() >= MAX_CANDIDATES) {
				truncated = true;
				break;
			}
			String command = CommandTextSanitizer.normalizeClipboardCommand(cleaned);
			String preview = command.length() <= PREVIEW_LENGTH
					? command : command.substring(0, PREVIEW_LENGTH - 1) + "…";
			candidates.add(new ClipboardCommandCandidate(command, slash, preview));
		}
		return new ClipboardCommandParseResult(
				candidates, lines.length > 1, truncated);
	}
}
