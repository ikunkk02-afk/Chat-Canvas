package io.github.ikunkk02.chatcanvas.chat.emoji;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageIngress;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

public final class EmojiRuntime {
	private static final Path DATA_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("chat_canvas").resolve("emoji.json");
	private static EmojiRecentManager recent;
	private static boolean pendingCorruptNotice;

	private EmojiRuntime() {
	}

	public static synchronized void initialize() {
		if (recent != null) return;
		recent = new EmojiRecentManager(new EmojiRecentStorage(DATA_PATH));
		EmojiRecentStorage.LoadResult result = recent.loadResult();
		if (result.status() == EmojiRecentStorage.LoadStatus.RECOVERED_CORRUPT) {
			pendingCorruptNotice = true;
			ChatCanvas.LOGGER.error(
					"Emoji recent-use data was corrupt and has been reset; backup={}",
					result.backupPath(), result.failure());
		} else if (result.status()
				== EmojiRecentStorage.LoadStatus.PARTIAL_RECOVERY) {
			ChatCanvas.LOGGER.warn(
					"Recovered valid entries from Emoji recent-use data at {}",
					DATA_PATH);
		}
	}

	public static EmojiRecentManager recent() {
		initialize();
		return recent;
	}

	public static void tick(Minecraft client) {
		if (!pendingCorruptNotice || client == null
				|| client.player == null || client.gui == null) return;
		pendingCorruptNotice = false;
		ChatCanvasMessageIngress.instance().reportError(
				Component.translatable("chat_canvas.emoji.recent_corrupt"), null);
	}

	public static void flush() {
		if (recent != null && !recent.flush()) {
			ChatCanvas.LOGGER.error("Failed to save Emoji recent-use data at {}", DATA_PATH);
		}
	}
}
