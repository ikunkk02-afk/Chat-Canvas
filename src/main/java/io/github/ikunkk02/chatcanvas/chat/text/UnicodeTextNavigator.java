package io.github.ikunkk02.chatcanvas.chat.text;

import com.ibm.icu.text.BreakIterator;

import java.util.Locale;

public final class UnicodeTextNavigator {
	private UnicodeTextNavigator() {
	}

	public static int previousGraphemeBoundary(String text, int index) {
		String safe = safe(text);
		int cursor = clamp(index, safe.length());
		if (cursor == 0) return 0;
		BreakIterator iterator = iterator(safe);
		int previous = iterator.preceding(cursor);
		return previous == BreakIterator.DONE ? 0 : previous;
	}

	public static int nextGraphemeBoundary(String text, int index) {
		String safe = safe(text);
		int cursor = clamp(index, safe.length());
		if (cursor == safe.length()) return safe.length();
		BreakIterator iterator = iterator(safe);
		int next = iterator.following(cursor);
		return next == BreakIterator.DONE ? safe.length() : next;
	}

	public static int floorGraphemeBoundary(String text, int index) {
		String safe = safe(text);
		int cursor = clamp(index, safe.length());
		BreakIterator iterator = iterator(safe);
		if (iterator.isBoundary(cursor)) return cursor;
		int previous = iterator.preceding(cursor);
		return previous == BreakIterator.DONE ? 0 : previous;
	}

	public static int ceilGraphemeBoundary(String text, int index) {
		String safe = safe(text);
		int cursor = clamp(index, safe.length());
		BreakIterator iterator = iterator(safe);
		if (iterator.isBoundary(cursor)) return cursor;
		int next = iterator.following(cursor);
		return next == BreakIterator.DONE ? safe.length() : next;
	}

	public static int nearestGraphemeBoundary(String text, int index) {
		int floor = floorGraphemeBoundary(text, index);
		int ceil = ceilGraphemeBoundary(text, index);
		return index - floor <= ceil - index ? floor : ceil;
	}

	public static int graphemeCount(String text) {
		String safe = safe(text);
		if (safe.isEmpty()) return 0;
		BreakIterator iterator = iterator(safe);
		int count = 0;
		for (int end = iterator.first(); (end = iterator.next()) != BreakIterator.DONE;) {
			count++;
		}
		return count;
	}

	public static EditResult replaceSelection(
			String text, int cursor, int selectionEnd,
			String replacement, int maxUtf16Length) {
		String safe = safe(text);
		String insert = safe(replacement);
		int a = Math.min(clamp(cursor, safe.length()), clamp(selectionEnd, safe.length()));
		int b = Math.max(clamp(cursor, safe.length()), clamp(selectionEnd, safe.length()));
		int start;
		int end;
		if (a == b) {
			start = floorGraphemeBoundary(safe, a);
			end = start;
		} else {
			start = floorGraphemeBoundary(safe, a);
			end = ceilGraphemeBoundary(safe, b);
		}
		String candidate = safe.substring(0, start) + insert + safe.substring(end);
		int limit = Math.max(0, maxUtf16Length);
		if (candidate.length() > limit) {
			int normalized = floorGraphemeBoundary(safe, cursor);
			return new EditResult(safe, normalized, normalized, false, true);
		}
		int nextCursor = start + insert.length();
		return new EditResult(candidate, nextCursor, nextCursor,
				!candidate.equals(safe), false);
	}

	public static EditResult deletePreviousGrapheme(
			String text, int cursor, int selectionEnd) {
		return delete(text, cursor, selectionEnd, true);
	}

	public static EditResult deleteNextGrapheme(
			String text, int cursor, int selectionEnd) {
		return delete(text, cursor, selectionEnd, false);
	}

	public static String truncateAtGraphemeBoundary(String text, int maxUtf16Length) {
		String safe = safe(text);
		int limit = Math.max(0, maxUtf16Length);
		if (safe.length() <= limit) return safe;
		return safe.substring(0, floorGraphemeBoundary(safe, limit));
	}

	public static boolean isWellFormedUtf16(String text) {
		String safe = safe(text);
		for (int index = 0; index < safe.length(); index++) {
			char current = safe.charAt(index);
			if (Character.isHighSurrogate(current)) {
				if (index + 1 >= safe.length()
						|| !Character.isLowSurrogate(safe.charAt(index + 1))) return false;
				index++;
			} else if (Character.isLowSurrogate(current)) {
				return false;
			}
		}
		return true;
	}

	private static EditResult delete(
			String text, int cursor, int selectionEnd, boolean backwards) {
		String safe = safe(text);
		int a = Math.min(clamp(cursor, safe.length()), clamp(selectionEnd, safe.length()));
		int b = Math.max(clamp(cursor, safe.length()), clamp(selectionEnd, safe.length()));
		int start;
		int end;
		if (a != b) {
			start = floorGraphemeBoundary(safe, a);
			end = ceilGraphemeBoundary(safe, b);
		} else {
			int normalized = backwards
					? ceilGraphemeBoundary(safe, a)
					: floorGraphemeBoundary(safe, a);
			start = backwards ? previousGraphemeBoundary(safe, normalized) : normalized;
			end = backwards ? normalized : nextGraphemeBoundary(safe, normalized);
		}
		if (start == end) return new EditResult(safe, start, start, false, false);
		String result = safe.substring(0, start) + safe.substring(end);
		return new EditResult(result, start, start, true, false);
	}

	private static BreakIterator iterator(String text) {
		BreakIterator iterator = BreakIterator.getCharacterInstance(Locale.ROOT);
		iterator.setText(text);
		return iterator;
	}

	private static String safe(String value) {
		return value == null ? "" : value;
	}

	private static int clamp(int value, int maximum) {
		return Math.max(0, Math.min(maximum, value));
	}

	public record EditResult(
			String text,
			int cursor,
			int selectionEnd,
			boolean changed,
			boolean limitExceeded
	) {
	}
}
