package io.github.ikunkk02.chatcanvas.chat.interaction;

public final class MentionInsertionController {
	private MentionInsertionController() {
	}

	public static Result plan(
			String currentText,
			int selectionStart,
			int selectionEnd,
			int maxLength,
			String playerName
	) {
		String text = currentText == null ? "" : currentText;
		if (playerName == null || playerName.isBlank()) return Result.invalid();
		int start = Math.max(0, Math.min(text.length(), Math.min(selectionStart, selectionEnd)));
		int end = Math.max(start, Math.min(text.length(), Math.max(selectionStart, selectionEnd)));
		String before = text.substring(0, start);
		String after = text.substring(end);
		StringBuilder inserted = new StringBuilder();
		if (!before.isEmpty() && !Character.isWhitespace(before.charAt(before.length() - 1))) {
			inserted.append(' ');
		}
		inserted.append('@').append(playerName);
		if (after.isEmpty()) {
			inserted.append(' ');
		} else if (!Character.isWhitespace(after.charAt(0))) {
			inserted.append(' ');
		}
		String candidate = before + inserted + after;
		if (candidate.length() > Math.max(0, maxLength)) {
			return Result.tooLong();
		}
		return new Result(Status.SUCCESS, candidate, before.length() + inserted.length());
	}

	public enum Status {
		SUCCESS, TOO_LONG, INVALID
	}

	public record Result(Status status, String text, int cursorUtf16) {
		private static Result tooLong() {
			return new Result(Status.TOO_LONG, "", -1);
		}

		private static Result invalid() {
			return new Result(Status.INVALID, "", -1);
		}

		public boolean successful() {
			return status == Status.SUCCESS;
		}
	}
}
