package io.github.ikunkk02.chatcanvas.config;

public enum CommandInsertMode {
	REPLACE_INPUT,
	INSERT_AT_CURSOR;

	public CommandInsertMode opposite() {
		return this == REPLACE_INPUT ? INSERT_AT_CURSOR : REPLACE_INPUT;
	}
}
