package io.github.ikunkk02.chatcanvas.chat.text;

import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputMode;
import net.minecraft.client.gui.components.EditBox;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Marks only the real ChatScreen input field for spaced rendering. Command
 * clipboard dialog fields and every other EditBox retain vanilla
 * behavior.
 */
public final class ChatCanvasTextFieldRegistry {
	private static final Map<EditBox, ChatCanvasInputMode> CHAT_FIELDS =
			new WeakHashMap<>();

	private ChatCanvasTextFieldRegistry() {
	}

	public static synchronized void register(EditBox field) {
		register(field, ChatCanvasInputMode.PLAYER_CHAT);
	}

	public static synchronized void register(
			EditBox field, ChatCanvasInputMode mode) {
		if (field != null) {
			CHAT_FIELDS.put(field, mode == null
					? ChatCanvasInputMode.PLAYER_CHAT : mode);
		}
	}

	public static synchronized void unregister(EditBox field) {
		CHAT_FIELDS.remove(field);
	}

	public static synchronized boolean isChatField(EditBox field) {
		return CHAT_FIELDS.containsKey(field);
	}

	public static synchronized ChatCanvasInputMode modeOf(EditBox field) {
		return CHAT_FIELDS.get(field);
	}
}
