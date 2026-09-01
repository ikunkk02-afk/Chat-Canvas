package io.github.ikunkk02.chatcanvas.chat.command;

public record ClipboardCommandCandidate(
		String command,
		boolean hadLeadingSlash,
	String preview
) {
	public ClipboardCommandCandidate {
		command = CommandTextSanitizer.normalizeClipboardCommand(command);
		preview = preview == null ? command : preview;
	}
}
