package io.github.ikunkk02.chatcanvas.chat.render;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public record PreviewChatMessage(
		Component text,
		@Nullable PlayerChatIdentity sender,
		boolean selfMessage
) {
	public PreviewChatMessage(Component text) {
		this(text, null, false);
	}

	public PreviewChatMessage(Component text, @Nullable PlayerChatIdentity sender) {
		this(text, sender, false);
	}
}
