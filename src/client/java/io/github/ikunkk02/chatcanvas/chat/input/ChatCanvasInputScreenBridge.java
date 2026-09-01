package io.github.ikunkk02.chatcanvas.chat.input;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;

public interface ChatCanvasInputScreenBridge {
	ChatCanvasInputMode chat_canvas$inputMode();

	EditBox chat_canvas$activeInputField();

	CommandSuggestions chat_canvas$activeInputSuggestor();

	void chat_canvas$openPlayerInput();

	boolean chat_canvas$dispatchUnicodeChar(char character, int modifiers);

	void chat_canvas$voiceTick();
}
