package io.github.ikunkk02.chatcanvas.chat.text;

import net.minecraft.client.gui.widget.TextFieldWidget;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Marks only the real ChatScreen input field for spaced rendering. Command
 * clipboard dialog fields and every other TextFieldWidget retain vanilla
 * behavior.
 */
public final class ChatCanvasTextFieldRegistry {
	private static final Set<TextFieldWidget> CHAT_FIELDS =
			Collections.newSetFromMap(new WeakHashMap<>());

	private ChatCanvasTextFieldRegistry() {
	}

	public static synchronized void register(TextFieldWidget field) {
		if (field != null) CHAT_FIELDS.add(field);
	}

	public static synchronized void unregister(TextFieldWidget field) {
		CHAT_FIELDS.remove(field);
	}

	public static synchronized boolean isChatField(TextFieldWidget field) {
		return CHAT_FIELDS.contains(field);
	}
}
