package io.github.ikunkk02.chatcanvas.chat.command;

public enum CommandToolTab {
	RECENT,
	FAVORITES,
	CLIPBOARD;

	public CommandToolTab next(int direction) {
		CommandToolTab[] values = values();
		return values[Math.floorMod(ordinal() + direction, values.length)];
	}
}
