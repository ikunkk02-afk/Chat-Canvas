package io.github.ikunkk02.chatcanvas.chat.interaction;

import io.github.ikunkk02.chatcanvas.mixin.client.TextFieldWidgetAccessor;
import io.github.ikunkk02.chatcanvas.client.MinecraftGuiCompat;
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
			field.getValue(), MinecraftGuiCompat.cursor(field), accessor.chat_canvas$selectionEnd(),
				accessor.chat_canvas$maxLength(), playerName);
		if (!insertion.successful()) return false;
		String previous = field.getValue();
		int previousCursor = MinecraftGuiCompat.cursor(field);
		int previousSelection = accessor.chat_canvas$selectionEnd();
		field.setValue(insertion.text());
		if (!insertion.text().equals(field.getValue())) {
			field.setValue(previous);
			MinecraftGuiCompat.setSelection(field, previousCursor, previousSelection);
			return false;
		}
		field.moveCursorTo(insertion.cursorUtf16(), false);
		field.setFocused(true);
		MinecraftGuiCompat.refresh(suggestor);
		return true;
	}

	public static InputSnapshot replace(
			EditBox field, CommandSuggestions suggestor, String replacement) {
		TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor) field;
		InputSnapshot previous = new InputSnapshot(
			field.getValue(), MinecraftGuiCompat.cursor(field), accessor.chat_canvas$selectionEnd());
		field.setValue(replacement);
		field.moveCursorToEnd(false);
		field.setFocused(true);
		MinecraftGuiCompat.refresh(suggestor);
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
		MinecraftGuiCompat.refresh(suggestor);
	}

	public static void restore(
			EditBox field, CommandSuggestions suggestor, InputSnapshot snapshot) {
		field.setValue(snapshot.text());
		MinecraftGuiCompat.setSelection(field, snapshot.cursor(), snapshot.selectionEnd());
		field.setFocused(true);
		MinecraftGuiCompat.refresh(suggestor);
	}

	public record InputSnapshot(String text, int cursor, int selectionEnd) {
	}
}
