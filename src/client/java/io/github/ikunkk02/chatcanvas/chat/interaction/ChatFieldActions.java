package io.github.ikunkk02.chatcanvas.chat.interaction;

import io.github.ikunkk02.chatcanvas.mixin.client.TextFieldWidgetAccessor;
import io.github.ikunkk02.chatcanvas.config.CommandInsertMode;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;

public final class ChatFieldActions {
	private ChatFieldActions() {
	}

	public static boolean insertMention(
			TextFieldWidget field, ChatInputSuggestor suggestor, String playerName) {
		TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor) field;
		MentionInsertionController.Result insertion = MentionInsertionController.plan(
				field.getText(), field.getCursor(), accessor.chat_canvas$selectionEnd(),
				accessor.chat_canvas$maxLength(), playerName);
		if (!insertion.successful()) return false;
		String previous = field.getText();
		int previousCursor = field.getCursor();
		int previousSelection = accessor.chat_canvas$selectionEnd();
		field.setText(insertion.text());
		if (!insertion.text().equals(field.getText())) {
			field.setText(previous);
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
			TextFieldWidget field, ChatInputSuggestor suggestor, String replacement) {
		TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor) field;
		InputSnapshot previous = new InputSnapshot(
				field.getText(), field.getCursor(), accessor.chat_canvas$selectionEnd());
		field.setText(replacement);
		field.setCursorToEnd(false);
		field.setFocused(true);
		suggestor.refresh();
		return previous;
	}

	public static void applyCommand(TextFieldWidget field, ChatInputSuggestor suggestor,
									String command, CommandInsertMode mode) {
		if (mode == CommandInsertMode.REPLACE_INPUT) {
			field.setText(command);
			field.setCursorToEnd(false);
		} else {
			field.write(command);
		}
		field.setFocused(true);
		suggestor.refresh();
	}

	public static void restore(
			TextFieldWidget field, ChatInputSuggestor suggestor, InputSnapshot snapshot) {
		field.setText(snapshot.text());
		field.setSelectionStart(snapshot.cursor());
		field.setSelectionEnd(snapshot.selectionEnd());
		field.setFocused(true);
		suggestor.refresh();
	}

	public record InputSnapshot(String text, int cursor, int selectionEnd) {
	}
}
