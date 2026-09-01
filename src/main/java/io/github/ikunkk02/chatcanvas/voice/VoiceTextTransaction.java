package io.github.ikunkk02.chatcanvas.voice;

/** Owns one replaceable provisional segment without overwriting later user edits. */
public final class VoiceTextTransaction {
	private String expectedText;
	private int start;
	private int end;
	private String replacedText;
	private String provisionalText = "";
	private boolean closed;

	public VoiceTextTransaction(String text, int cursor, int selectionEnd) {
		rebase(text, cursor, selectionEnd);
	}

	public Edit updatePartial(String currentText, int cursor, int selectionEnd,
						  String partial, int maximumLength) {
		return replace(currentText, cursor, selectionEnd, partial, maximumLength, false);
	}

	public Edit commit(String currentText, int cursor, int selectionEnd,
					   String result, int maximumLength) {
		Edit edit = replace(currentText, cursor, selectionEnd, result, maximumLength, true);
		if (!edit.limitExceeded()) closed = true;
		return edit;
	}

	public Edit cancel(String currentText, int cursor, int selectionEnd) {
		if (closed) return new Edit(currentText, cursor, selectionEnd, false);
		if (!currentText.equals(expectedText)) {
			Reconciled reconciled = reconcile(currentText, cursor, selectionEnd);
			currentText = reconciled.text();
			cursor = reconciled.cursor();
			selectionEnd = reconciled.selectionEnd();
			rebase(currentText, cursor, selectionEnd);
		}
		String restored = currentText.substring(0, start) + replacedText + currentText.substring(end);
		int restoredCursor = start + replacedText.length();
		closed = true;
		return new Edit(restored, restoredCursor, restoredCursor, false);
	}

	private Edit replace(String currentText, int cursor, int selectionEnd,
						 String value, int maximumLength, boolean finalValue) {
		if (closed) return new Edit(currentText, cursor, selectionEnd, false);
		String replacement = value == null ? "" : value;
		if (!currentText.equals(expectedText)) {
			Reconciled reconciled = reconcile(currentText, cursor, selectionEnd);
			currentText = reconciled.text();
			cursor = reconciled.cursor();
			selectionEnd = reconciled.selectionEnd();
			rebase(currentText, cursor, selectionEnd);
		}
		String result = currentText.substring(0, start) + replacement + currentText.substring(end);
		if (result.length() > maximumLength) {
			return new Edit(currentText, cursor, selectionEnd, true);
		}
		end = start + replacement.length();
		expectedText = result;
		provisionalText = replacement;
		int newCursor = end;
		if (finalValue) replacedText = replacement;
		return new Edit(result, newCursor, newCursor, false);
	}

	private void rebase(String text, int cursor, int selectionEnd) {
		expectedText = text == null ? "" : text;
		start = Math.max(0, Math.min(expectedText.length(), Math.min(cursor, selectionEnd)));
		end = Math.max(start, Math.min(expectedText.length(), Math.max(cursor, selectionEnd)));
		replacedText = expectedText.substring(start, end);
		provisionalText = "";
	}

	private Reconciled reconcile(String currentText, int cursor, int selectionEnd) {
		String text = currentText == null ? "" : currentText;
		int safeCursor = Math.max(0, Math.min(text.length(), cursor));
		int safeSelection = Math.max(0, Math.min(text.length(), selectionEnd));
		if (end <= text.length() && start <= end
				&& text.substring(start, end).equals(provisionalText)) {
			String cleaned = text.substring(0, start) + replacedText + text.substring(end);
			int delta = replacedText.length() - (end - start);
			safeCursor = adjustIndex(safeCursor, delta);
			safeSelection = adjustIndex(safeSelection, delta);
			return new Reconciled(cleaned, safeCursor, safeSelection);
		}
		return new Reconciled(text, safeCursor, safeSelection);
	}

	private int adjustIndex(int index, int delta) {
		if (index <= start) return index;
		if (index < end) return start + replacedText.length();
		return index + delta;
	}

	public record Edit(String text, int cursor, int selectionEnd, boolean limitExceeded) { }
	private record Reconciled(String text, int cursor, int selectionEnd) { }
}
