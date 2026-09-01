package io.github.ikunkk02.chatcanvas.client;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

/** Small adapters for the structured input and text-field APIs introduced by 26.1. */
public final class MinecraftGuiCompat {
	private MinecraftGuiCompat() {
	}

	public static KeyEvent keyEvent(int key, int scancode, int modifiers) {
		return new KeyEvent(key, scancode, modifiers);
	}

	public static CharacterEvent characterEvent(int codepoint) {
		return new CharacterEvent(codepoint);
	}

	public static MouseButtonEvent mouseEvent(double x, double y, int button, int modifiers) {
		return new MouseButtonEvent(x, y, new MouseButtonInfo(button, modifiers));
	}

	public static int cursor(EditBox field) {
		return field.getCursorPosition();
	}

	public static void setSelection(EditBox field, int cursor, int selectionEnd) {
		field.moveCursorTo(cursor, false);
		field.setHighlightPos(selectionEnd);
	}

	public static void insert(EditBox field, String text) {
		field.insertText(text);
	}

	public static void refresh(CommandSuggestions suggestions) {
		suggestions.updateCommandInfo();
	}

	public static void setSuggestionsVisible(CommandSuggestions suggestions, boolean visible) {
		if (suggestions == null) return;
		// 26.1 renamed the old ChatInputSuggestor#setWindowActive state to
		// CommandSuggestions#setAllowSuggestions. Calling showSuggestions here
		// consumes the previous async result before the new input has been parsed.
		suggestions.setAllowSuggestions(visible);
	}
}
