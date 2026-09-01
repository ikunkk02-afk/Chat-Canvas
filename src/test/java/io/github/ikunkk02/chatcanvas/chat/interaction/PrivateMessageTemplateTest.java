package io.github.ikunkk02.chatcanvas.chat.interaction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrivateMessageTemplateTest {
	@Test
	void replacesEveryPlayerPlaceholder() {
		assertEquals("/tell Steve Steve ",
				PrivateMessageTemplate.apply("/tell {player} {player} ", "Steve"));
	}

	@Test
	void missingPlaceholderFallsBackToDefault() {
		assertEquals("/msg Alex ",
				PrivateMessageTemplate.apply("/tell somebody ", "Alex"));
	}
}
