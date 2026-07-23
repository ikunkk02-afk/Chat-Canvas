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

		LayoutConfig custom = new LayoutConfig(0.12, 0.31, 0.42, 0.22);
		ChatTextConfig text = new ChatTextConfig(
				1.25, 0.8, 0.55, ChatTextAlignment.RIGHT, false);
		assertTrue(config.save(new ChatCanvasSettings(custom, text)));
		ChatCanvasConfig reloaded = new ChatCanvasConfig(path);
		reloaded.load();
		assertEquals(custom, reloaded.layout());
		assertEquals(text, reloaded.text());
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
	}
}
