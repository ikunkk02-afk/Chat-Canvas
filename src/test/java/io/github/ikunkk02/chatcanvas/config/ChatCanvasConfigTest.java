package io.github.ikunkk02.chatcanvas.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ChatCanvasConfigTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void missingConfigIsGeneratedAndCanRoundTrip() throws IOException {
		Path path = temporaryDirectory.resolve("chat_canvas.json");
		ChatCanvasConfig config = new ChatCanvasConfig(path);
		config.load();
		assertTrue(Files.exists(path));
		assertEquals(LayoutConfig.DEFAULT, config.layout());
		assertEquals(ChatTextConfig.DEFAULT, config.text());
		assertEquals(ChatBackgroundConfig.DEFAULT, config.background());
		assertEquals(PlayerColorConfig.DEFAULT, config.playerColors());
		assertEquals(MentionConfig.DEFAULT, config.mention());
		assertEquals(PlayerChatLayoutMode.CLASSIC, config.playerChatLayoutMode());
		assertEquals(.75, config.splitMessageMaxWidthRatio(), .00001);
		assertTrue(config.recentColors().isEmpty());

		LayoutConfig custom = new LayoutConfig(0.12, 0.31, 0.42, 0.22);
		ChatTextConfig text = new ChatTextConfig(
				1.25, 0.8, 0.55, ChatTextAlignment.RIGHT, false, 1.4);
		ChatBackgroundConfig background = new ChatBackgroundConfig(
				MessageBackgroundMode.FULL_WIDTH,
				0x123456, 0.25, 7, 3,
				0x654321, 0.4, true, 0xABCDEF, 0.9);
		PlayerColorConfig playerColors = new PlayerColorConfig(
				false, PlayerColorMode.VANILLA, java.util.List.of(0x123456),
				java.util.Map.of("12345678-1234-5678-9abc-123456789abc", 0x654321),
				java.util.Map.of("Steve", 0xABCDEF), true);
		MentionConfig mention = new MentionConfig(false, 480, true, 0x123456, false, false);
		assertTrue(config.save(new ChatCanvasSettings(
				custom, text, background, playerColors, mention,
				java.util.List.of(0x123456, 0x654321))));
		ChatCanvasConfig reloaded = new ChatCanvasConfig(path);
		reloaded.load();
		assertEquals(custom, reloaded.layout());
		assertEquals(text, reloaded.text());
		assertEquals(background, reloaded.background());
		assertEquals(playerColors, reloaded.playerColors());
		assertEquals(mention, reloaded.mention());
		assertEquals(java.util.List.of(0x123456, 0x654321), reloaded.recentColors());
	}

	@Test
	void damagedConfigFallsBackWithoutOverwritingDamage() throws IOException {
		Path path = temporaryDirectory.resolve("chat_canvas.json");
		String damaged = "{ definitely not json";
		Files.writeString(path, damaged);
		ChatCanvasConfig config = new ChatCanvasConfig(path);
		config.load();
		assertEquals(LayoutConfig.DEFAULT, config.layout());
		assertEquals(damaged, Files.readString(path));
	}

	@Test
	void outOfRangeValuesAreClampedOnRead() throws IOException {
		Path path = temporaryDirectory.resolve("chat_canvas.json");
		Files.writeString(path, """
				{
				  "chatXRatio": -4,
				  "chatYRatio": 9,
				  "chatWidthRatio": 0.4,
				  "chatHeightRatio": 0.3
				}
				""");
		ChatCanvasConfig config = new ChatCanvasConfig(path);
		config.load();
		assertEquals(0.0, config.layout().chatXRatio());
		assertEquals(0.7, config.layout().chatYRatio(), 0.00001);
		assertEquals(ChatTextConfig.DEFAULT, config.text());
	}

	@Test
	void invalidTextFieldsFallBackOrClampWithoutLosingLegacyLayout() throws IOException {
		Path path = temporaryDirectory.resolve("chat_canvas.json");
		Files.writeString(path, """
				{
				  "chatXRatio": 0.12,
				  "chatYRatio": 0.21,
				  "chatWidthRatio": 0.44,
				  "chatHeightRatio": 0.32,
				  "text": {
				    "fontScale": "NaN",
				    "lineSpacing": -5,
				    "textOpacity": 8,
				    "alignment": "diagonal",
				    "shadow": false
				  }
				}
				""");

		ChatCanvasConfig config = new ChatCanvasConfig(path);
		config.load();
		assertEquals(0.12, config.layout().chatXRatio(), 0.00001);
		assertEquals(0.44, config.layout().chatWidthRatio(), 0.00001);
		assertEquals(1.0, config.text().fontScale(), 0.00001);
		assertEquals(0.5, config.text().lineSpacing(), 0.00001);
		assertEquals(1.0, config.text().textOpacity(), 0.00001);
		assertEquals(ChatTextAlignment.LEFT, config.text().alignment());
		assertFalse(config.text().shadow());
		assertEquals(0.0, config.text().characterSpacing(), 0.00001);
	}

	@Test
	void missingTextMembersUseDefaultsIndividually() throws IOException {
		Path path = temporaryDirectory.resolve("chat_canvas.json");
		Files.writeString(path, """
				{
				  "chatXRatio": 0.1,
				  "chatYRatio": 0.2,
				  "chatWidthRatio": 0.4,
				  "chatHeightRatio": 0.3,
				  "text": {
				    "fontScale": 1.5
				  }
				}
				""");

		ChatCanvasConfig config = new ChatCanvasConfig(path);
		config.load();
		assertEquals(1.5, config.text().fontScale(), 0.00001);
		assertEquals(ChatTextConfig.DEFAULT.lineSpacing(), config.text().lineSpacing(), 0.00001);
		assertEquals(ChatTextConfig.DEFAULT.textOpacity(), config.text().textOpacity(), 0.00001);
		assertEquals(ChatTextAlignment.LEFT, config.text().alignment());
		assertTrue(config.text().shadow());
		assertEquals(0.0, config.text().characterSpacing(), 0.00001);
		assertEquals(ChatBackgroundConfig.DEFAULT, config.background());
	}

	@Test
	void missingOrInvalidBackgroundMembersUseDefaultsWithoutLosingExistingSettings() throws IOException {
		Path path = temporaryDirectory.resolve("chat_canvas.json");
		Files.writeString(path, """
				{
				  "chatXRatio": 0.15,
				  "chatYRatio": 0.2,
				  "chatWidthRatio": 0.4,
				  "chatHeightRatio": 0.3,
				  "text": {
				    "fontScale": 1.25
				  },
				  "background": {
				    "messageMode": "not-a-mode",
				    "messageColor": 1193046,
				    "messageOpacity": 4,
				    "horizontalPadding": 20,
				    "inputBorderEnabled": true
				  },
				  "recentColors": [1193046, -1, 1193046, 16777215]
				}
				""");

		ChatCanvasConfig config = new ChatCanvasConfig(path);
		config.load();
		assertEquals(0.15, config.layout().chatXRatio(), 0.00001);
		assertEquals(1.25, config.text().fontScale(), 0.00001);
		assertEquals(MessageBackgroundMode.FOLLOW_TEXT, config.background().messageMode());
		assertEquals(0x123456, config.background().messageColor());
		assertEquals(1.0, config.background().messageOpacity());
		assertEquals(12, config.background().horizontalPadding());
		assertEquals(ChatBackgroundConfig.DEFAULT.verticalPadding(),
				config.background().verticalPadding());
		assertTrue(config.background().inputBorderEnabled());
		assertEquals(PlayerColorConfig.DEFAULT, config.playerColors());
		assertEquals(MentionConfig.DEFAULT, config.mention());
		assertEquals(java.util.List.of(0x123456, 0xFFFFFF), config.recentColors());
	}

	@Test
	void commandSystemRoundTripsWithoutChangingLegacyPlayerLayout() {
		Path path = temporaryDirectory.resolve("chat_canvas.json");
		ChatCanvasConfig config = new ChatCanvasConfig(path);
		CommandSystemConfig command = new CommandSystemConfig(
				false, new LayoutConfig(.55, .08, .32, .25),
				new ChatTextConfig(1.4, 1.2, .8, ChatTextAlignment.LEFT, false, .5),
				ChatBackgroundConfig.DEFAULT, 0xAABBCC, 777, 24, 4, 31);
		ChatCanvasSettings defaults = ChatCanvasSettings.DEFAULT;
		assertTrue(config.save(new ChatCanvasSettings(
				defaults.layout(), defaults.text(), defaults.background(),
				defaults.playerColors(), defaults.mention(), defaults.commandClipboard(),
				defaults.recentColors(), defaults.editorUiStyle(),
				true, true, command)));
		ChatCanvasConfig reloaded = new ChatCanvasConfig(path);
		reloaded.load();
		assertEquals(LayoutConfig.DEFAULT, reloaded.layout());
		assertEquals(command.sanitized(), reloaded.commandSystem());
	}

	@Test
	void commandToolPrivacySettingsRoundTripAndOldConfigGetsSafeDefaults() throws IOException {
		Path path = temporaryDirectory.resolve("chat_canvas.json");
		CommandClipboardConfig commandTools = CommandClipboardConfig.DEFAULT
				.withRecordRecentCommands(false)
				.withMaxRecentCommands(40)
				.withClearRecentOnDisconnect(true)
				.withExcludedCommandNames(java.util.Set.of("secret", "/customauth"));
		ChatCanvasSettings defaults = ChatCanvasSettings.DEFAULT;
		ChatCanvasConfig config = new ChatCanvasConfig(path);
		assertTrue(config.save(new ChatCanvasSettings(
				defaults.layout(), defaults.text(), defaults.background(),
				defaults.playerColors(), defaults.mention(), commandTools,
				defaults.recentColors(), defaults.editorUiStyle(),
				defaults.enabled(), defaults.playerChatEnabled(),
				defaults.commandSystem())));

		ChatCanvasConfig reloaded = new ChatCanvasConfig(path);
		reloaded.load();
		assertEquals(commandTools, reloaded.commandClipboard());

		Files.writeString(path, """
				{"commandClipboard":{"enabled":true,"maxCommands":50}}
				""");
		ChatCanvasConfig legacy = new ChatCanvasConfig(path);
		legacy.load();
		assertTrue(legacy.commandClipboard().recordRecentCommands());
		assertEquals(CommandClipboardConfig.DEFAULT_EXCLUDED_COMMAND_NAMES,
				legacy.commandClipboard().excludedCommandNames());
	}

	@Test
	void playerLayoutRoundTripsAndLegacyOrInvalidValuesUseSafeDefaults()
			throws IOException {
		Path path = temporaryDirectory.resolve("chat_canvas.json");
		ChatCanvasSettings defaults = ChatCanvasSettings.DEFAULT;
		ChatCanvasConfig config = new ChatCanvasConfig(path);
		assertTrue(config.save(new ChatCanvasSettings(
				defaults.layout(), defaults.text(), defaults.background(),
				defaults.playerColors(), defaults.mention(), defaults.commandClipboard(),
				defaults.recentColors(), defaults.editorUiStyle(),
				defaults.enabled(), defaults.playerChatEnabled(),
				PlayerChatLayoutMode.SPLIT_ALIGNMENT, .62,
				defaults.commandSystem())));
		ChatCanvasConfig reloaded = new ChatCanvasConfig(path);
		reloaded.load();
		assertEquals(PlayerChatLayoutMode.SPLIT_ALIGNMENT,
				reloaded.playerChatLayoutMode());
		assertEquals(.62, reloaded.splitMessageMaxWidthRatio(), .00001);

		Files.writeString(path, """
				{
				  "playerChatLayoutMode": "unknown",
				  "splitMessageMaxWidthRatio": 9
				}
				""");
		ChatCanvasConfig invalid = new ChatCanvasConfig(path);
		invalid.load();
		assertEquals(PlayerChatLayoutMode.CLASSIC, invalid.playerChatLayoutMode());
		assertEquals(1.0, invalid.splitMessageMaxWidthRatio(), .00001);

		Files.writeString(path, "{}");
		ChatCanvasConfig legacy = new ChatCanvasConfig(path);
		legacy.load();
		assertEquals(PlayerChatLayoutMode.CLASSIC, legacy.playerChatLayoutMode());
		assertEquals(.75, legacy.splitMessageMaxWidthRatio(), .00001);
	}
}
