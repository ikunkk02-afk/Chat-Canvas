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
		assertTrue(config.recentColors().isEmpty());

		LayoutConfig custom = new LayoutConfig(0.12, 0.31, 0.42, 0.22);
		ChatTextConfig text = new ChatTextConfig(
				1.25, 0.8, 0.55, ChatTextAlignment.RIGHT, false);
		ChatBackgroundConfig background = new ChatBackgroundConfig(
				MessageBackgroundMode.FULL_WIDTH,
				0x123456, 0.25, 7, 3,
				0x654321, 0.4, true, 0xABCDEF, 0.9);
		PlayerColorConfig playerColors = new PlayerColorConfig(
				false, PlayerColorMode.VANILLA, java.util.List.of(0x123456),
				java.util.Map.of("12345678-1234-5678-9abc-123456789abc", 0x654321),
				java.util.Map.of("Steve", 0xABCDEF), true);
		assertTrue(config.save(new ChatCanvasSettings(
				custom, text, background, playerColors,
				java.util.List.of(0x123456, 0x654321))));
		ChatCanvasConfig reloaded = new ChatCanvasConfig(path);
		reloaded.load();
		assertEquals(custom, reloaded.layout());
		assertEquals(text, reloaded.text());
		assertEquals(background, reloaded.background());
		assertEquals(playerColors, reloaded.playerColors());
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
		assertEquals(java.util.List.of(0x123456, 0xFFFFFF), config.recentColors());
	}
}
