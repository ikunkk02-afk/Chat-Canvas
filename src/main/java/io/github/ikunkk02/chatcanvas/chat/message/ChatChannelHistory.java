package io.github.ikunkk02.chatcanvas.chat.message;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

public final class ChatChannelHistory {
	private int maximumMessages;
	private final Deque<ChatCanvasMessage> messages = new ArrayDeque<>();
	private final BoundedMessageIdCache acceptedIds = new BoundedMessageIdCache();
	private double scrollOffsetPixels;
	private double targetScrollPixels;
	private boolean lockedToBottom = true;
	private boolean unread;
	private long layoutEpoch;
	private ScrollAnchor anchor;

	public ChatChannelHistory(int maximumMessages) {
		this.maximumMessages = Math.max(1, maximumMessages);
	}

	public synchronized boolean add(ChatCanvasMessage message) {
		if (message == null || !acceptedIds.accept(message.messageId(), message.receivedAt())) {
			return false;
		}
		messages.add(message);
		while (messages.size() > maximumMessages) messages.remove(0);
		if (lockedToBottom) {
			scrollOffsetPixels = 0.0;
			targetScrollPixels = 0.0;
			anchor = null;
		} else {
			unread = true;
		}
		return true;
	}

	public synchronized List<ChatCanvasMessage> messages() {
		return List.copyOf(messages);
	}

	public synchronized void scrollBy(double pixels, double maximumOffset) {
		double maximum = Math.max(0.0, maximumOffset);
		targetScrollPixels = clamp(targetScrollPixels + pixels, 0.0, maximum);
		scrollOffsetPixels = targetScrollPixels;
		lockedToBottom = targetScrollPixels <= 0.001;
		if (lockedToBottom) unread = false;
	}

	public synchronized void setScrollOffset(double pixels, double maximumOffset) {
		double maximum = Math.max(0.0, maximumOffset);
		scrollOffsetPixels = clamp(pixels, 0.0, maximum);
		targetScrollPixels = scrollOffsetPixels;
		lockedToBottom = scrollOffsetPixels <= 0.001;
		if (lockedToBottom) unread = false;
	}

	public synchronized double scrollOffsetPixels() {
		return scrollOffsetPixels;
	}

	public synchronized double targetScrollPixels() {
		return targetScrollPixels;
	}

	public synchronized boolean lockedToBottom() {
		return lockedToBottom;
	}

	public synchronized boolean unread() {
		return unread;
	}

	public synchronized long layoutEpoch() {
		return layoutEpoch;
	}

	public synchronized void invalidateLayout() {
		layoutEpoch++;
	}

	public synchronized ScrollAnchor anchor() {
		return anchor;
	}

	public synchronized void anchor(UUID messageId, double offsetWithinMessage) {
		anchor = messageId == null ? null : new ScrollAnchor(messageId, offsetWithinMessage);
	}

	public synchronized void clear() {
		messages.clear();
		acceptedIds.clear();
		scrollOffsetPixels = 0.0;
		targetScrollPixels = 0.0;
		lockedToBottom = true;
		unread = false;
		anchor = null;
		layoutEpoch++;
	}

	public int maximumMessages() {
		return maximumMessages;
	}

	public synchronized void setMaximumMessages(int value) {
		maximumMessages = Math.max(1, value);
		while (messages.size() > maximumMessages) messages.remove(0);
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	public record ScrollAnchor(UUID messageId, double offsetWithinMessage) {
	}
}
