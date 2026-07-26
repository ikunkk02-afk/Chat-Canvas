package io.github.ikunkk02.chatcanvas.chat.input;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;

public interface ChatCanvasInputScreenBridge {
	ChatCanvasInputMode chat_canvas$inputMode();

	TextFieldWidget chat_canvas$activeInputField();

	ChatInputSuggestor chat_canvas$activeInputSuggestor();

	void chat_canvas$openPlayerInput();

	boolean chat_canvas$dispatchUnicodeChar(char character, int modifiers);
}
