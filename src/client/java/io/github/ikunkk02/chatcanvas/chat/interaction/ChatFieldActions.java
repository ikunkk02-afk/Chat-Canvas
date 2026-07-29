package io.github.ikunkk02.chatcanvas.chat.interaction;

import io.github.ikunkk02.chatcanvas.mixin.client.TextFieldWidgetAccessor;
import io.github.ikunkk02.chatcanvas.config.CommandInsertMode;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;

public final class ChatFieldActions {
	private ChatFieldActions() {
	}

	public static boolean insertMention(
			EditBox field, CommandSuggestions suggestor, String playerName) {
		TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor) field;
		MentionInsertionController.Result insertion = MentionInsertionController.plan(
				field.getValue(), field.getCursor(), accessor.chat_canvas$selectionEnd(),
				accessor.chat_canvas$maxLength(), playerName);
		if (!insertion.successful()) return false;
		String previous = field.getValue();
		int previousCursor = field.getCursor();
		int previousSelection = accessor.chat_canvas$selectionEnd();
		field.setValue(insertion.text());
		if (!insertion.text().equals(field.getValue())) {
			field.setValue(previous);
			field.setSelectionStart(previousCursor);
			field.setSelectionEnd(previousSelection);
			return false;
		}
		field.setCursor(insertion.cursorUtf16(), false);
		field.setFocused(true);
		suggestor.refresh();
		return true;
	}

	public static InputSnapshot replace(
			EditBox field, CommandSuggestions suggestor, String replacement) {
		TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor) field;
		InputSnapshot previous = new InputSnapshot(
				field.getValue(), field.getCursor(), accessor.chat_canvas$selectionEnd());
		field.setValue(replacement);
		field.setCursorToEnd(false);
		field.setFocused(true);
		suggestor.refresh();
		return previous;
	}

	public static void applyCommand(EditBox field, CommandSuggestions suggestor,
									String command, CommandInsertMode mode) {
		if (mode == CommandInsertMode.REPLACE_INPUT) {
			field.setValue(command);
			field.setCursorToEnd(false);
		} else {
			field.write(command);
		}
		field.setFocused(true);
		suggestor.refresh();
	}

	public static void restore(
			EditBox field, CommandSuggestions suggestor, InputSnapshot snapshot) {
		field.setValue(snapshot.text());
		field.setSelectionStart(snapshot.cursor());
		field.setSelectionEnd(snapshot.selectionEnd());
		field.setFocused(true);
		suggestor.refresh();
	}

	public record InputSnapshot(String text, int cursor, int selectionEnd) {
	}
}
