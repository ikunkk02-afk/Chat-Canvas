package io.github.ikunkk02.chatcanvas.config;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameColorProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerColorConfigTest {
	private static final UUID UUID_VALUE = UUID.fromString("12345678-1234-5678-9abc-123456789abc");

	@Test
	void automaticColorIsStableAndUuidBased() {
		PlayerNameColorProvider provider = new PlayerNameColorProvider();
		provider.updateConfig(PlayerColorConfig.DEFAULT);
		int beforeRename = provider.colorFor(
				new PlayerChatIdentity(UUID_VALUE, "Steve", true)).orElseThrow();
		int afterRename = provider.colorFor(
				new PlayerChatIdentity(UUID_VALUE, "RenamedSteve", true)).orElseThrow();
		assertEquals(beforeRename, afterRename);
		assertEquals(PlayerColorConfig.DEFAULT.palette().get(
				Math.floorMod(UUID_VALUE.hashCode(), PlayerColorConfig.DEFAULT.palette().size())),
				beforeRename);
	}

	@Test
	void overridePriorityAndNameNormalizationAreDeterministic() {
		PlayerColorConfig config = new PlayerColorConfig(
				true,
				PlayerColorMode.AUTOMATIC,
				List.of(0x111111, 0x222222),
				Map.of(UUID_VALUE.toString(), 0xABCDEF),
				Map.of("STEVE", 0x123456),
				false
		);
		PlayerNameColorProvider provider = new PlayerNameColorProvider();
		provider.updateConfig(config);
		assertEquals(0xABCDEF, provider.colorFor(
				new PlayerChatIdentity(UUID_VALUE, "steve", true)).orElseThrow());
		assertEquals(0x123456, provider.colorFor(
				new PlayerChatIdentity(null, "StEvE", false)).orElseThrow());
	}

	@Test
	void emptyPaletteFallsBackAndVanillaModeDoesNotColor() {
		PlayerColorConfig config = new PlayerColorConfig(
				true, PlayerColorMode.VANILLA, List.of(), Map.of(), Map.of(), false);
		assertEquals(PlayerColorConfig.DEFAULT_PALETTE, config.palette());
		PlayerNameColorProvider provider = new PlayerNameColorProvider();
		provider.updateConfig(config);
		assertTrue(provider.colorFor(
				new PlayerChatIdentity(UUID_VALUE, "Steve", true)).isEmpty());
	}

	@Test
	void malformedOverrideKeysAndColorsAreDiscarded() {
		PlayerColorConfig config = new PlayerColorConfig(
				true,
				PlayerColorMode.AUTOMATIC,
				List.of(-1, 0x123456, 0x1000000),
				Map.of("not-a-uuid", 0x123456),
				Map.of("  Steve  ", 0x654321),
				false
		);
		assertEquals(List.of(0x123456), config.palette());
		assertTrue(config.uuidOverrides().isEmpty());
		assertEquals(Map.of("steve", 0x654321), config.nameOverrides());
	}
}
