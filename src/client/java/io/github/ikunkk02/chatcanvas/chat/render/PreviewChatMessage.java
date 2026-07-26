package io.github.ikunkk02.chatcanvas.chat.render;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public record PreviewChatMessage(
		Text text,
		@Nullable PlayerChatIdentity sender,
		boolean selfMessage
) {
	public PreviewChatMessage(Text text) {
		this(text, null, false);
	}

	public PreviewChatMessage(Text text, @Nullable PlayerChatIdentity sender) {
		this(text, sender, false);
	}
}
