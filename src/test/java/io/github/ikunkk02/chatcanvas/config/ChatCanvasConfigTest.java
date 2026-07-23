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

		LayoutConfig custom = new LayoutConfig(0.12, 0.31, 0.42, 0.22);
		assertTrue(config.save(custom));
		ChatCanvasConfig reloaded = new ChatCanvasConfig(path);
		reloaded.load();
		assertEquals(custom, reloaded.layout());
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
	}
}
