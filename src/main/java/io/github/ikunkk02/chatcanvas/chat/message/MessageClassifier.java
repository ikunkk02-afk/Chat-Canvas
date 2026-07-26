package io.github.ikunkk02.chatcanvas.chat.message;

import net.minecraft.text.Text;

public interface MessageClassifier {
	ClassifiedMessage classify(Text message, MessageContext context);
}
