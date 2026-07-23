package io.github.ikunkk02.chatcanvas.config;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandClipboardConfigTest {
	@Test
	void clampsCapacityAndSanitizesMode() {
		CommandClipboardConfig low = new CommandClipboardConfig(
				true, true, null, false, true, 1, Set.of());
		CommandClipboardConfig high = low.withMaxCommands(5000);

		assertEquals(20, low.maxCommands());
		assertEquals(CommandInsertMode.REPLACE_INPUT, low.insertMode());
		assertEquals(1000, high.maxCommands());
	}
}
