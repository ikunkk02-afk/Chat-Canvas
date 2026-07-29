package io.github.ikunkk02.chatcanvas.chat.command;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageIngress;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

public final class CommandToolRuntime {
	private static final Path DATA_DIRECTORY = FabricLoader.getInstance()
			.getConfigDir().resolve("chat_canvas");
	private static final Path DATA_PATH = DATA_DIRECTORY.resolve("commands.json");
	private static final Path LEGACY_PATH =
			DATA_DIRECTORY.resolve("command_clipboard.json");
	private static final CommandToolManager MANAGER = new CommandToolManager(
			new CommandToolStorage(DATA_PATH),
			new CommandClipboardStorage(LEGACY_PATH),
			LEGACY_PATH,
			() -> ChatCanvasConfig.instance().commandClipboard(),
			CommandToolRuntime::reportError);
	private static String serverIdentifier = "unknown";

	private CommandToolRuntime() {
	}

	public static CommandToolManager manager() {
		return MANAGER;
	}

	public static void beginSession(MinecraftClient client) {
		if (client == null) {
			serverIdentifier = "unknown";
		} else if (client.isInSingleplayer()) {
			serverIdentifier = "singleplayer";
		} else {
			String address = client.getCurrentServerEntry() == null
					? "unknown" : client.getCurrentServerEntry().address;
			serverIdentifier = "server-"
					+ UUID.nameUUIDFromBytes(address.getBytes(StandardCharsets.UTF_8));
		}
	}

	public static void recordExecuted(String command) {
		MANAGER.recordExecuted(command, serverIdentifier, System.currentTimeMillis());
	}

	public static void endSession() {
		if (ChatCanvasConfig.instance().commandClipboard()
				.clearRecentOnDisconnect()) {
			MANAGER.clearRecentForServer(
					serverIdentifier, System.currentTimeMillis());
		}
		serverIdentifier = "unknown";
		MANAGER.flush();
	}

	public static void reportToolError(String summary, Throwable throwable) {
		reportError(summary, throwable);
	}

	private static void reportError(String summary, Throwable throwable) {
		if (throwable == null) ChatCanvas.LOGGER.warn(summary);
		try {
			ChatCanvasMessageIngress.instance().reportError(
					Component.literal("[Chat Canvas] " + summary), throwable);
		} catch (RuntimeException nested) {
			ChatCanvas.LOGGER.error("Failed to report command tool error", nested);
		}
	}
}
