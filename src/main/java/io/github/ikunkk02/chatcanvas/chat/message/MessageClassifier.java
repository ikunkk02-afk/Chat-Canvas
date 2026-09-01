package io.github.ikunkk02.chatcanvas.chat.message;

import net.minecraft.network.chat.Component;

public interface MessageClassifier {
	ClassifiedMessage classify(Component message, MessageContext context);
}
