package io.github.ikunkk02.chatcanvas.chat.text;

import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputMode;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Marks only the real ChatScreen input field for spaced rendering. Command
 * clipboard dialog fields and every other TextFieldWidget retain vanilla
 * behavior.
 */
public final class ChatCanvasTextFieldRegistry {
	private static final Map<TextFieldWidget, ChatCanvasInputMode> CHAT_FIELDS =
			new WeakHashMap<>();

	private ChatCanvasTextFieldRegistry() {
	}

	public static synchronized void register(TextFieldWidget field) {
		register(field, ChatCanvasInputMode.PLAYER_CHAT);
	}

	public static synchronized void register(
			TextFieldWidget field, ChatCanvasInputMode mode) {
		if (field != null) {
			CHAT_FIELDS.put(field, mode == null
					? ChatCanvasInputMode.PLAYER_CHAT : mode);
		}
	}

	public static synchronized void unregister(TextFieldWidget field) {
		CHAT_FIELDS.remove(field);
	}

	public static synchronized boolean isChatField(TextFieldWidget field) {
		return CHAT_FIELDS.containsKey(field);
	}

	public static synchronized ChatCanvasInputMode modeOf(TextFieldWidget field) {
		return CHAT_FIELDS.get(field);
	}
}
