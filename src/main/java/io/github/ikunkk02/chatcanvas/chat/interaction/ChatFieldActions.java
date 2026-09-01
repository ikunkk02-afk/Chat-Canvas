package io.github.ikunkk02.chatcanvas.chat.interaction;

import io.github.ikunkk02.chatcanvas.mixin.client.TextFieldWidgetAccessor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import io.github.ikunkk02.chatcanvas.config.CommandInsertMode;

public final class ChatFieldActions {
	private ChatFieldActions() {
	}

	public static boolean insertMention(
			EditBox field, CommandSuggestions suggestor, String playerName) {
		TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor) field;
		MentionInsertionController.Result insertion = MentionInsertionController.plan(
				field.getValue(), field.getCursorPosition(), accessor.chat_canvas$selectionEnd(),
				accessor.chat_canvas$maxLength(), playerName);
		if (!insertion.successful()) return false;
		String previous = field.getValue();
		int previousCursor = field.getCursorPosition();
		int previousSelection = accessor.chat_canvas$selectionEnd();
		field.setValue(insertion.text());
		if (!insertion.text().equals(field.getValue())) {
			field.setValue(previous);
			field.setCursorPosition(previousCursor);
			field.setHighlightPos(previousSelection);
			return false;
		}
		field.moveCursorTo(insertion.cursorUtf16(), false);
		field.setFocused(true);
		suggestor.updateCommandInfo();
		return true;
	}

	public static InputSnapshot replace(
			EditBox field, CommandSuggestions suggestor, String replacement) {
		TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor) field;
		InputSnapshot previous = new InputSnapshot(
				field.getValue(), field.getCursorPosition(), accessor.chat_canvas$selectionEnd());
		field.setValue(replacement);
		field.moveCursorToEnd(false);
		field.setFocused(true);
		suggestor.updateCommandInfo();
		return previous;
	}

	public static void applyCommand(EditBox field, CommandSuggestions suggestor,
									String command, CommandInsertMode mode) {
		if (mode == CommandInsertMode.REPLACE_INPUT) {
			field.setValue(command);
			field.moveCursorToEnd(false);
		} else {
			field.insertText(command);
		}
		field.setFocused(true);
		suggestor.updateCommandInfo();
	}

	public static void restore(
			EditBox field, CommandSuggestions suggestor, InputSnapshot snapshot) {
		field.setValue(snapshot.text());
		field.setCursorPosition(snapshot.cursor());
		field.setHighlightPos(snapshot.selectionEnd());
		field.setFocused(true);
		suggestor.updateCommandInfo();
	}

	public record InputSnapshot(String text, int cursor, int selectionEnd) {
	}
}
