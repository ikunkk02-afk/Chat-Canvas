package io.github.ikunkk02.chatcanvas.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.ikunkk02.chatcanvas.ChatCanvasForge;
import io.github.ikunkk02.chatcanvas.editor.EditorUiStyle;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ChatCanvasConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ChatCanvasConfig instance;

	private final Path path;
	private ChatCanvasSettings settings = ChatCanvasSettings.DEFAULT;

	public ChatCanvasConfig(Path path) {
		this.path = path;
	}

	public static synchronized ChatCanvasConfig initialize() {
		if (instance == null) {
			instance = new ChatCanvasConfig(java.nio.file.Path.of("config").resolve("chat_canvas.json"));
			instance.load();
			// Rewrite successfully loaded legacy configurations through the current
			// serializer so newly introduced fields become visible on disk as well as
			// receiving their in-memory defaults.
			instance.save(instance.settings);
		}
		return instance;
	}

	public static ChatCanvasConfig instance() {
		if (instance == null) {
			return initialize();
		}
		return instance;
	}

	public synchronized LayoutConfig layout() {
		return settings.layout();
	}

	public synchronized ChatTextConfig text() {
		return settings.text();
	}

	public synchronized ChatBackgroundConfig background() {
		return settings.background();
	}

	public synchronized PlayerColorConfig playerColors() {
		return settings.playerColors();
	}

	public synchronized MentionConfig mention() {
		return settings.mention();
	}

	public synchronized CommandClipboardConfig commandClipboard() {
		return settings.commandClipboard();
	}

	public synchronized List<Integer> recentColors() {
		return settings.recentColors();
	}

	public synchronized EditorUiStyle editorUiStyle() {
		return settings.editorUiStyle();
	}

	public synchronized boolean enabled() {
		return settings.enabled();
	}

	public synchronized boolean playerChatEnabled() {
		return settings.playerChatEnabled();
	}

	public synchronized PlayerChatLayoutMode playerChatLayoutMode() {
		return settings.playerChatLayoutMode();
	}

	public synchronized double splitMessageMaxWidthRatio() {
		return settings.splitMessageMaxWidthRatio();
	}

	public synchronized CommandSystemConfig commandSystem() {
		return settings.commandSystem();
	}

	public synchronized ChatCanvasSettings settings() {
		return settings;
	}

	public synchronized void load() {
		if (Files.notExists(path)) {
			settings = ChatCanvasSettings.DEFAULT;
			if (!save(settings)) {
				ChatCanvasForge.LOGGER.warn("Could not create default Chat Canvas config at {}", path);
			}
			return;
		}

		try (Reader reader = Files.newBufferedReader(path)) {
			JsonElement root = JsonParser.parseReader(reader);
			if (!root.isJsonObject()) {
				throw new IllegalArgumentException("Chat Canvas config root must be an object");
			}
			settings = parseSettings(root.getAsJsonObject()).sanitized();
		} catch (IOException | RuntimeException exception) {
			ChatCanvasForge.LOGGER.warn("Failed to read Chat Canvas config at {}; using defaults", path, exception);
			settings = ChatCanvasSettings.DEFAULT;
		}
	}

	public synchronized boolean save(LayoutConfig value) {
		return save(new ChatCanvasSettings(
				value, settings.text(), settings.background(), settings.playerColors(),
				settings.mention(), settings.commandClipboard(), settings.recentColors(),
				settings.editorUiStyle(), settings.enabled(), settings.playerChatEnabled(),
				settings.playerChatLayoutMode(), settings.splitMessageMaxWidthRatio(),
				settings.commandSystem()));
	}

	public synchronized boolean save(ChatCanvasSettings value) {
		ChatCanvasSettings sanitized = value.sanitized();
		Path parent = path.getParent();
		Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
		try {
			if (parent != null) {
				Files.createDirectories(parent);
			}
			try (Writer writer = Files.newBufferedWriter(temporary)) {
				GSON.toJson(toJson(sanitized), writer);
			}
			try {
				Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException ignored) {
				Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
			}
			settings = sanitized;
			return true;
		} catch (IOException exception) {
			ChatCanvasForge.LOGGER.error("Failed to save Chat Canvas config to {}", path, exception);
			try {
				Files.deleteIfExists(temporary);
			} catch (IOException cleanupException) {
				ChatCanvasForge.LOGGER.debug("Failed to remove temporary config {}", temporary, cleanupException);
			}
			return false;
		}
	}

	public Path path() {
		return path;
	}

	private static ChatCanvasSettings parseSettings(JsonObject root) {
		LayoutConfig defaults = LayoutConfig.DEFAULT;
		JsonObject layout = objectOr(root, "layout", root);
		LayoutConfig parsedLayout = new LayoutConfig(
				doubleOr(layout, "chatXRatio", defaults.chatXRatio()),
				doubleOr(layout, "chatYRatio", defaults.chatYRatio()),
				doubleOr(layout, "chatWidthRatio", defaults.chatWidthRatio()),
				doubleOr(layout, "chatHeightRatio", defaults.chatHeightRatio())
		).sanitized();

		ChatTextConfig textDefaults = ChatTextConfig.DEFAULT;
		JsonObject text = objectOr(root, "text", null);
		ChatTextConfig parsedText = text == null
				? textDefaults
				: new ChatTextConfig(
						doubleOr(text, "fontScale", textDefaults.fontScale()),
						doubleOr(text, "lineSpacing", textDefaults.lineSpacing()),
						doubleOr(text, "textOpacity", textDefaults.textOpacity()),
						alignmentOr(text, "alignment", textDefaults.alignment()),
						booleanOr(text, "shadow", textDefaults.shadow()),
						doubleOr(text, "characterSpacing", textDefaults.characterSpacing())
				).sanitized();

		ChatBackgroundConfig backgroundDefaults = ChatBackgroundConfig.DEFAULT;
		JsonObject background = objectOr(root, "background", null);
		ChatBackgroundConfig parsedBackground = background == null
				? backgroundDefaults
				: new ChatBackgroundConfig(
						backgroundModeOr(background, "messageMode", backgroundDefaults.messageMode()),
						intOr(background, "messageColor", backgroundDefaults.messageColor()),
						doubleOr(background, "messageOpacity", backgroundDefaults.messageOpacity()),
						intOr(background, "horizontalPadding", backgroundDefaults.horizontalPadding()),
						intOr(background, "verticalPadding", backgroundDefaults.verticalPadding()),
						intOr(background, "inputColor", backgroundDefaults.inputColor()),
						doubleOr(background, "inputOpacity", backgroundDefaults.inputOpacity()),
						booleanOr(background, "inputBorderEnabled", backgroundDefaults.inputBorderEnabled()),
						intOr(background, "inputBorderColor", backgroundDefaults.inputBorderColor()),
						doubleOr(background, "inputBorderOpacity", backgroundDefaults.inputBorderOpacity())
				).sanitized();

		PlayerColorConfig playerDefaults = PlayerColorConfig.DEFAULT;
		JsonObject playerColors = objectOr(root, "playerColors", null);
		PlayerColorConfig parsedPlayerColors = playerColors == null
				? playerDefaults
				: new PlayerColorConfig(
						booleanOr(playerColors, "enabled", playerDefaults.enabled()),
						playerColorModeOr(playerColors, "mode", playerDefaults.mode()),
						colorListOr(playerColors, "palette", playerDefaults.palette()),
						colorMapOr(playerColors, "uuidOverrides"),
						colorMapOr(playerColors, "nameOverrides"),
						booleanOr(playerColors, "showNameHitboxes", false)
				).sanitized();
		MentionConfig mentionDefaults = MentionConfig.DEFAULT;
		JsonObject mention = objectOr(root, "mention", null);
		MentionConfig parsedMention = mention == null
				? mentionDefaults
				: new MentionConfig(
						booleanOr(mention, "doubleClickEnabled", mentionDefaults.doubleClickEnabled()),
						intOr(mention, "doubleClickIntervalMs", mentionDefaults.doubleClickIntervalMs()),
						booleanOr(mention, "highlightEnabled", mentionDefaults.highlightEnabled()),
						intOr(mention, "highlightColor", mentionDefaults.highlightColor()),
						booleanOr(mention, "highlightBold", mentionDefaults.highlightBold()),
						booleanOr(mention, "requireAtSymbol", mentionDefaults.requireAtSymbol()),
						booleanOr(mention, "soundEnabled", mentionDefaults.soundEnabled()),
						mentionSoundOr(mention, "sound", mentionDefaults.sound()),
						doubleOr(mention, "soundVolume", mentionDefaults.soundVolume()),
						doubleOr(mention, "soundPitch", mentionDefaults.soundPitch()),
						booleanOr(mention, "toastEnabled", mentionDefaults.toastEnabled()),
						booleanOr(mention, "toastWhenChatOpen", mentionDefaults.toastWhenChatOpen()),
						intOr(mention, "toastMessageLength", mentionDefaults.toastMessageLength()),
						booleanOr(mention, "flashEnabled", mentionDefaults.flashEnabled()),
						intOr(mention, "flashColor", mentionDefaults.flashColor()),
						doubleOr(mention, "flashOpacity", mentionDefaults.flashOpacity()),
						intOr(mention, "flashDurationMs", mentionDefaults.flashDurationMs()),
						booleanOr(mention, "ignoreOwnMessages", mentionDefaults.ignoreOwnMessages()),
						booleanOr(mention, "playerQuickActionsEnabled",
								mentionDefaults.playerQuickActionsEnabled()),
						stringOr(mention, "privateMessageTemplate",
								mentionDefaults.privateMessageTemplate())
				).sanitized();
		CommandClipboardConfig commandDefaults = CommandClipboardConfig.DEFAULT;
		JsonObject commandClipboard = objectOr(root, "commandClipboard", null);
		CommandClipboardConfig parsedCommandClipboard = commandClipboard == null
				? commandDefaults
				: new CommandClipboardConfig(
						booleanOr(commandClipboard, "enabled", commandDefaults.enabled()),
						booleanOr(commandClipboard, "showPanelButton",
								commandDefaults.showPanelButton()),
						commandInsertModeOr(commandClipboard, "insertMode",
								commandDefaults.insertMode()),
						booleanOr(commandClipboard, "allowDuplicates",
								commandDefaults.allowDuplicates()),
						booleanOr(commandClipboard, "sensitiveWarning",
								commandDefaults.sensitiveWarning()),
						intOr(commandClipboard, "maxCommands", commandDefaults.maxCommands()),
						stringSetOr(commandClipboard, "hiddenPresetIds"),
						booleanOr(commandClipboard, "recordRecentCommands",
								commandDefaults.recordRecentCommands()),
						intOr(commandClipboard, "maxRecentCommands",
								commandDefaults.maxRecentCommands()),
						booleanOr(commandClipboard, "clearRecentOnDisconnect",
								commandDefaults.clearRecentOnDisconnect()),
						commandClipboard.has("excludedCommandNames")
								? stringSetOr(commandClipboard, "excludedCommandNames")
								: commandDefaults.excludedCommandNames()
				).sanitized();
		return new ChatCanvasSettings(
				parsedLayout,
				parsedText,
				parsedBackground,
				parsedPlayerColors,
				parsedMention,
				parsedCommandClipboard,
				recentColorsOr(root, "recentColors"),
				editorUiStyleOr(root, "editorUiStyle", EditorUiStyle.CHAT_CANVAS),
				booleanOr(root, "enabled", true),
				booleanOr(root, "playerChatEnabled", true),
				playerChatLayoutModeOr(root, "playerChatLayoutMode",
						PlayerChatLayoutMode.CLASSIC),
				doubleOr(root, "splitMessageMaxWidthRatio",
						ChatCanvasSettings.DEFAULT_SPLIT_MESSAGE_MAX_WIDTH_RATIO),
				parseCommandSystem(objectOr(root, "commandSystem", null))
		);
	}

	private static CommandSystemConfig parseCommandSystem(JsonObject object) {
		CommandSystemConfig defaults = CommandSystemConfig.DEFAULT;
		if (object == null) return defaults;
		JsonObject layoutObject = objectOr(object, "layout", null);
		LayoutConfig layout = layoutObject == null ? defaults.layout() : new LayoutConfig(
				doubleOr(layoutObject, "chatXRatio", defaults.layout().chatXRatio()),
				doubleOr(layoutObject, "chatYRatio", defaults.layout().chatYRatio()),
				doubleOr(layoutObject, "chatWidthRatio", defaults.layout().chatWidthRatio()),
				doubleOr(layoutObject, "chatHeightRatio", defaults.layout().chatHeightRatio())
		).sanitized();
		JsonObject textObject = objectOr(object, "text", null);
		ChatTextConfig text = textObject == null ? defaults.text() : new ChatTextConfig(
				doubleOr(textObject, "fontScale", defaults.text().fontScale()),
				doubleOr(textObject, "lineSpacing", defaults.text().lineSpacing()),
				doubleOr(textObject, "textOpacity", defaults.text().textOpacity()),
				alignmentOr(textObject, "alignment", defaults.text().alignment()),
				booleanOr(textObject, "shadow", defaults.text().shadow()),
				doubleOr(textObject, "characterSpacing", defaults.text().characterSpacing())
		).sanitized();
		JsonObject backgroundObject = objectOr(object, "background", null);
		ChatBackgroundConfig background = backgroundObject == null ? defaults.background()
				: new ChatBackgroundConfig(
				backgroundModeOr(backgroundObject, "messageMode", defaults.background().messageMode()),
				intOr(backgroundObject, "messageColor", defaults.background().messageColor()),
				doubleOr(backgroundObject, "messageOpacity", defaults.background().messageOpacity()),
				intOr(backgroundObject, "horizontalPadding", defaults.background().horizontalPadding()),
				intOr(backgroundObject, "verticalPadding", defaults.background().verticalPadding()),
				defaults.background().inputColor(), defaults.background().inputOpacity(),
				defaults.background().inputBorderEnabled(), defaults.background().inputBorderColor(),
				defaults.background().inputBorderOpacity()).sanitized();
		return new CommandSystemConfig(
				booleanOr(object, "enabled", defaults.enabled()), layout, text, background,
				intOr(object, "textColor", defaults.textColor()),
				intOr(object, "maximumMessages", defaults.maximumMessages()),
				intOr(object, "fadeSeconds", defaults.fadeSeconds()),
				doubleOr(object, "messageSpacing", defaults.messageSpacing()),
				doubleOr(object, "scrollSpeed", defaults.scrollSpeed()),
				booleanOr(object, "outline", defaults.outline()),
				intOr(object, "outlineColor", defaults.outlineColor()),
				doubleOr(object, "outlineOpacity", defaults.outlineOpacity())).sanitized();
	}

	private static JsonObject toJson(ChatCanvasSettings value) {
		JsonObject root = new JsonObject();
		root.addProperty("enabled", value.enabled());
		root.addProperty("playerChatEnabled", value.playerChatEnabled());
		root.addProperty("playerChatLayoutMode", value.playerChatLayoutMode().name());
		root.addProperty("splitMessageMaxWidthRatio", value.splitMessageMaxWidthRatio());
		LayoutConfig layout = value.layout();
		root.addProperty("chatXRatio", layout.chatXRatio());
		root.addProperty("chatYRatio", layout.chatYRatio());
		root.addProperty("chatWidthRatio", layout.chatWidthRatio());
		root.addProperty("chatHeightRatio", layout.chatHeightRatio());

		ChatTextConfig text = value.text();
		JsonObject textObject = new JsonObject();
		textObject.addProperty("fontScale", text.fontScale());
		textObject.addProperty("lineSpacing", text.lineSpacing());
		textObject.addProperty("textOpacity", text.textOpacity());
		textObject.addProperty("alignment", text.alignment().name());
		textObject.addProperty("shadow", text.shadow());
		textObject.addProperty("characterSpacing", text.characterSpacing());
		root.add("text", textObject);

		ChatBackgroundConfig background = value.background();
		JsonObject backgroundObject = new JsonObject();
		backgroundObject.addProperty("messageMode", background.messageMode().name());
		backgroundObject.addProperty("messageColor", background.messageColor());
		backgroundObject.addProperty("messageOpacity", background.messageOpacity());
		backgroundObject.addProperty("horizontalPadding", background.horizontalPadding());
		backgroundObject.addProperty("verticalPadding", background.verticalPadding());
		backgroundObject.addProperty("inputColor", background.inputColor());
		backgroundObject.addProperty("inputOpacity", background.inputOpacity());
		backgroundObject.addProperty("inputBorderEnabled", background.inputBorderEnabled());
		backgroundObject.addProperty("inputBorderColor", background.inputBorderColor());
		backgroundObject.addProperty("inputBorderOpacity", background.inputBorderOpacity());
		root.add("background", backgroundObject);

		PlayerColorConfig playerColors = value.playerColors();
		JsonObject playerObject = new JsonObject();
		playerObject.addProperty("enabled", playerColors.enabled());
		playerObject.addProperty("mode", playerColors.mode().name());
		JsonArray palette = new JsonArray();
		for (int color : playerColors.palette()) {
			palette.add(color);
		}
		playerObject.add("palette", palette);
		playerObject.add("uuidOverrides", colorMapToJson(playerColors.uuidOverrides()));
		playerObject.add("nameOverrides", colorMapToJson(playerColors.nameOverrides()));
		playerObject.addProperty("showNameHitboxes", playerColors.showNameHitboxes());
		root.add("playerColors", playerObject);

		MentionConfig mention = value.mention();
		JsonObject mentionObject = new JsonObject();
		mentionObject.addProperty("doubleClickEnabled", mention.doubleClickEnabled());
		mentionObject.addProperty("doubleClickIntervalMs", mention.doubleClickIntervalMs());
		mentionObject.addProperty("highlightEnabled", mention.highlightEnabled());
		mentionObject.addProperty("highlightColor", mention.highlightColor());
		mentionObject.addProperty("highlightBold", mention.highlightBold());
		mentionObject.addProperty("requireAtSymbol", mention.requireAtSymbol());
		mentionObject.addProperty("soundEnabled", mention.soundEnabled());
		mentionObject.addProperty("sound", mention.sound().name());
		mentionObject.addProperty("soundVolume", mention.soundVolume());
		mentionObject.addProperty("soundPitch", mention.soundPitch());
		mentionObject.addProperty("toastEnabled", mention.toastEnabled());
		mentionObject.addProperty("toastWhenChatOpen", mention.toastWhenChatOpen());
		mentionObject.addProperty("toastMessageLength", mention.toastMessageLength());
		mentionObject.addProperty("flashEnabled", mention.flashEnabled());
		mentionObject.addProperty("flashColor", mention.flashColor());
		mentionObject.addProperty("flashOpacity", mention.flashOpacity());
		mentionObject.addProperty("flashDurationMs", mention.flashDurationMs());
		mentionObject.addProperty("ignoreOwnMessages", mention.ignoreOwnMessages());
		mentionObject.addProperty("playerQuickActionsEnabled", mention.playerQuickActionsEnabled());
		mentionObject.addProperty("privateMessageTemplate", mention.privateMessageTemplate());
		root.add("mention", mentionObject);

		CommandClipboardConfig command = value.commandClipboard();
		JsonObject commandObject = new JsonObject();
		commandObject.addProperty("enabled", command.enabled());
		commandObject.addProperty("showPanelButton", command.showPanelButton());
		commandObject.addProperty("insertMode", command.insertMode().name());
		commandObject.addProperty("allowDuplicates", command.allowDuplicates());
		commandObject.addProperty("sensitiveWarning", command.sensitiveWarning());
		commandObject.addProperty("maxCommands", command.maxCommands());
		JsonArray hiddenPresets = new JsonArray();
		command.hiddenPresetIds().forEach(hiddenPresets::add);
		commandObject.add("hiddenPresetIds", hiddenPresets);
		commandObject.addProperty("recordRecentCommands", command.recordRecentCommands());
		commandObject.addProperty("maxRecentCommands", command.maxRecentCommands());
		commandObject.addProperty("clearRecentOnDisconnect",
				command.clearRecentOnDisconnect());
		JsonArray excludedCommands = new JsonArray();
		command.excludedCommandNames().stream().sorted().forEach(excludedCommands::add);
		commandObject.add("excludedCommandNames", excludedCommands);
		root.add("commandClipboard", commandObject);

		JsonArray recentColors = new JsonArray();
		for (int color : value.recentColors()) {
			recentColors.add(color);
		}
		root.add("recentColors", recentColors);
		root.addProperty("editorUiStyle", value.editorUiStyle().name());
		root.add("commandSystem", commandSystemToJson(value.commandSystem()));
		return root;
	}

	private static JsonObject commandSystemToJson(CommandSystemConfig value) {
		JsonObject root = new JsonObject();
		root.addProperty("enabled", value.enabled());
		JsonObject layout = new JsonObject();
		layout.addProperty("chatXRatio", value.layout().chatXRatio());
		layout.addProperty("chatYRatio", value.layout().chatYRatio());
		layout.addProperty("chatWidthRatio", value.layout().chatWidthRatio());
		layout.addProperty("chatHeightRatio", value.layout().chatHeightRatio());
		root.add("layout", layout);
		JsonObject text = new JsonObject();
		text.addProperty("fontScale", value.text().fontScale());
		text.addProperty("lineSpacing", value.text().lineSpacing());
		text.addProperty("textOpacity", value.text().textOpacity());
		text.addProperty("alignment", value.text().alignment().name());
		text.addProperty("shadow", value.text().shadow());
		text.addProperty("characterSpacing", value.text().characterSpacing());
		root.add("text", text);
		JsonObject background = new JsonObject();
		background.addProperty("messageMode", value.background().messageMode().name());
		background.addProperty("messageColor", value.background().messageColor());
		background.addProperty("messageOpacity", value.background().messageOpacity());
		background.addProperty("horizontalPadding", value.background().horizontalPadding());
		background.addProperty("verticalPadding", value.background().verticalPadding());
		root.add("background", background);
		root.addProperty("textColor", value.textColor());
		root.addProperty("maximumMessages", value.maximumMessages());
		root.addProperty("fadeSeconds", value.fadeSeconds());
		root.addProperty("messageSpacing", value.messageSpacing());
		root.addProperty("scrollSpeed", value.scrollSpeed());
		root.addProperty("outline", value.outline());
		root.addProperty("outlineColor", value.outlineColor());
		root.addProperty("outlineOpacity", value.outlineOpacity());
		return root;
	}

	private static JsonObject objectOr(JsonObject root, String key, JsonObject fallback) {
		JsonElement element = root.get(key);
		return element != null && element.isJsonObject() ? element.getAsJsonObject() : fallback;
	}

	private static double doubleOr(JsonObject object, String key, double fallback) {
		JsonElement element = object.get(key);
		if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
			return fallback;
		}
		try {
			return element.getAsDouble();
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static boolean booleanOr(JsonObject object, String key, boolean fallback) {
		JsonElement element = object.get(key);
		if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
			return fallback;
		}
		try {
			return element.getAsBoolean();
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static int intOr(JsonObject object, String key, int fallback) {
		JsonElement element = object.get(key);
		if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
			return fallback;
		}
		try {
			return element.getAsInt();
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static String stringOr(JsonObject object, String key, String fallback) {
		JsonElement element = object.get(key);
		if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
			return fallback;
		}
		try {
			return element.getAsString();
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static MentionSound mentionSoundOr(JsonObject object, String key,
											 MentionSound fallback) {
		String value = stringOr(object, key, fallback.name());
		try {
			return MentionSound.valueOf(value.toUpperCase(Locale.ROOT));
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static CommandInsertMode commandInsertModeOr(
			JsonObject object, String key, CommandInsertMode fallback) {
		String value = stringOr(object, key, fallback.name());
		try {
			return CommandInsertMode.valueOf(value.toUpperCase(Locale.ROOT));
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static java.util.Set<String> stringSetOr(JsonObject object, String key) {
		JsonElement element = object.get(key);
		if (element == null || !element.isJsonArray()) return java.util.Set.of();
		java.util.LinkedHashSet<String> values = new java.util.LinkedHashSet<>();
		for (JsonElement entry : element.getAsJsonArray()) {
			try {
				String value = entry.getAsString();
				if (!value.isBlank()) values.add(value);
			} catch (RuntimeException ignored) {
			}
		}
		return values;
	}

	private static ChatTextAlignment alignmentOr(JsonObject object, String key,
												 ChatTextAlignment fallback) {
		JsonElement element = object.get(key);
		if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
			return fallback;
		}
		try {
			return ChatTextAlignment.valueOf(element.getAsString().toUpperCase(java.util.Locale.ROOT));
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static PlayerChatLayoutMode playerChatLayoutModeOr(
			JsonObject object, String key, PlayerChatLayoutMode fallback) {
		JsonElement element = object.get(key);
		if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
			return fallback;
		}
		try {
			return PlayerChatLayoutMode.valueOf(
					element.getAsString().toUpperCase(Locale.ROOT));
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static MessageBackgroundMode backgroundModeOr(JsonObject object, String key,
														  MessageBackgroundMode fallback) {
		JsonElement element = object.get(key);
		if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
			return fallback;
		}
		try {
			return MessageBackgroundMode.valueOf(element.getAsString().toUpperCase(Locale.ROOT));
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static PlayerColorMode playerColorModeOr(JsonObject object, String key,
													 PlayerColorMode fallback) {
		JsonElement element = object.get(key);
		if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
			return fallback;
		}
		try {
			return PlayerColorMode.valueOf(element.getAsString().toUpperCase(Locale.ROOT));
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static EditorUiStyle editorUiStyleOr(JsonObject object, String key,
												 EditorUiStyle fallback) {
		JsonElement element = object.get(key);
		if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
			return fallback;
		}
		try {
			return EditorUiStyle.valueOf(element.getAsString().toUpperCase(Locale.ROOT));
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static List<Integer> colorListOr(JsonObject object, String key, List<Integer> fallback) {
		JsonElement element = object.get(key);
		if (element == null || !element.isJsonArray()) return fallback;
		List<Integer> colors = new ArrayList<>();
		for (JsonElement candidate : element.getAsJsonArray()) {
			if (candidate == null || !candidate.isJsonPrimitive()) continue;
			try {
				int color = candidate.getAsInt();
				if (color >= 0 && color <= 0xFFFFFF) colors.add(color);
			} catch (RuntimeException ignored) {
				// Ignore malformed entries.
			}
		}
		return colors.isEmpty() ? fallback : colors;
	}

	private static Map<String, Integer> colorMapOr(JsonObject object, String key) {
		JsonElement element = object.get(key);
		if (element == null || !element.isJsonObject()) return Map.of();
		Map<String, Integer> result = new LinkedHashMap<>();
		for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
			if (entry.getValue() == null || !entry.getValue().isJsonPrimitive()) continue;
			try {
				int color = entry.getValue().getAsInt();
				if (color >= 0 && color <= 0xFFFFFF) result.put(entry.getKey(), color);
			} catch (RuntimeException ignored) {
				// Ignore malformed entries.
			}
		}
		return result;
	}

	private static JsonObject colorMapToJson(Map<String, Integer> values) {
		JsonObject result = new JsonObject();
		values.forEach(result::addProperty);
		return result;
	}

	private static List<Integer> recentColorsOr(JsonObject root, String key) {
		JsonElement element = root.get(key);
		if (element == null || !element.isJsonArray()) {
			return List.of();
		}
		List<Integer> colors = new ArrayList<>();
		for (JsonElement candidate : element.getAsJsonArray()) {
			if (candidate == null || !candidate.isJsonPrimitive()) {
				continue;
			}
			try {
				int color = candidate.getAsInt();
				if (color >= 0 && color <= 0xFFFFFF) {
					colors.add(color);
				}
			} catch (RuntimeException ignored) {
				// Ignore malformed palette entries without rejecting the config.
			}
		}
		return RecentColorStore.sanitizedCopy(colors);
	}
}
