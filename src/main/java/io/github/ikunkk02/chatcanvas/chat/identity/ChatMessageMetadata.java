package io.github.ikunkk02.chatcanvas.chat.identity;

public record ChatMessageMetadata(
		PlayerChatIdentity sender,
		int nameStart,
		int nameEnd
) {
	/**
	 * Name offsets are Unicode code point indices in the message's plain text.
	 */
	public ChatMessageMetadata {
		if (sender == null) throw new IllegalArgumentException("sender");
		nameStart = Math.max(0, nameStart);
		nameEnd = Math.max(nameStart, nameEnd);
	}
}
