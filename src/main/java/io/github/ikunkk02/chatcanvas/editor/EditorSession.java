package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.config.ChatCanvasSettings;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import io.github.ikunkk02.chatcanvas.config.CommandClipboardConfig;
import io.github.ikunkk02.chatcanvas.config.LayoutConfig;
import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import io.github.ikunkk02.chatcanvas.config.RecentColorStore;
import io.github.ikunkk02.chatcanvas.config.PlayerColorConfig;
import io.github.ikunkk02.chatcanvas.config.CommandSystemConfig;

public final class EditorSession {
	private final EditorSnapshot original;
	private final EditorHistory history;
	private final RecentColorStore recentColors;
	private PixelLayout layout;
	private ChatTextConfig text;
	private ChatBackgroundConfig background;
	private PlayerColorConfig playerColors;
	private MentionConfig mention;
	private CommandClipboardConfig commandClipboard;
	private PixelLayout commandLayout;
	private CommandSystemConfig commandSystem;
	private EditorChannel selectedChannel = EditorChannel.PLAYER_CHAT;
	private final boolean enabled;
	private final boolean playerChatEnabled;
	private final EditorUiStyle editorUiStyle;
	private int screenWidth;
	private int screenHeight;

	public EditorSession(LayoutConfig original, int screenWidth, int screenHeight) {
		this(new ChatCanvasSettings(original, ChatTextConfig.DEFAULT), screenWidth, screenHeight);
	}

	public EditorSession(ChatCanvasSettings original, int screenWidth, int screenHeight) {
		ChatCanvasSettings safe = original.sanitized();
		this.original = new EditorSnapshot(
				safe.layout(), safe.text(), safe.background(), safe.playerColors(), safe.mention(),
				safe.commandClipboard(), safe.commandSystem());
		this.screenWidth = Math.max(1, screenWidth);
		this.screenHeight = Math.max(1, screenHeight);
		this.layout = this.original.layout().toPixels(this.screenWidth, this.screenHeight);
		this.text = this.original.text();
		this.background = this.original.background();
		this.playerColors = this.original.playerColors();
		this.mention = this.original.mention();
		this.commandClipboard = this.original.commandClipboard();
		this.commandSystem = this.original.commandSystem();
		this.commandLayout = commandSystem.layout().toPixels(this.screenWidth, this.screenHeight);
		this.enabled = safe.enabled();
		this.playerChatEnabled = safe.playerChatEnabled();
		this.editorUiStyle = safe.editorUiStyle();
		this.recentColors = new RecentColorStore(safe.recentColors());
		this.history = new EditorHistory(snapshot());
	}

	public PixelLayout layout() {
		return layout(selectedChannel);
	}

	public PixelLayout layout(EditorChannel channel) {
		return channel == EditorChannel.PLAYER_CHAT ? layout : commandLayout;
	}

	public ChatTextConfig text() {
		return text(selectedChannel);
	}

	public ChatTextConfig text(EditorChannel channel) {
		return channel == EditorChannel.PLAYER_CHAT ? text : commandSystem.text();
	}

	public ChatBackgroundConfig background() {
		return background(selectedChannel);
	}

	public ChatBackgroundConfig background(EditorChannel channel) {
		return channel == EditorChannel.PLAYER_CHAT ? background : commandSystem.background();
	}

	public PlayerColorConfig playerColors() {
		return playerColors;
	}

	public MentionConfig mention() {
		return mention;
	}

	public CommandClipboardConfig commandClipboard() {
		return commandClipboard;
	}

	public RecentColorStore recentColors() {
		return recentColors;
	}

	public EditorSnapshot original() {
		return original;
	}

	public EditorSnapshot snapshot() {
		CommandSystemConfig commandSnapshot = withCommandLayout(
				LayoutConfig.fromPixels(commandLayout, screenWidth, screenHeight));
		return new EditorSnapshot(
				LayoutConfig.fromPixels(layout, screenWidth, screenHeight),
				text,
				background,
				playerColors,
				mention,
				commandClipboard,
				commandSnapshot
		);
	}

	public ChatCanvasSettings settings() {
		EditorSnapshot snapshot = snapshot();
		return new ChatCanvasSettings(
				snapshot.layout(),
				snapshot.text(),
				snapshot.background(),
				snapshot.playerColors(),
				snapshot.mention(),
				snapshot.commandClipboard(),
				recentColors.colors(),
				editorUiStyle,
				enabled,
				playerChatEnabled,
				snapshot.commandSystem()
		);
	}

	public void setLayout(PixelLayout value) {
		setLayout(selectedChannel, value);
	}

	public void setLayout(EditorChannel channel, PixelLayout value) {
		if (channel == EditorChannel.PLAYER_CHAT) layout = value.constrained(screenWidth, screenHeight);
		else commandLayout = value.constrained(screenWidth, screenHeight);
	}

	public void apply(LayoutConfig value) {
		if (selectedChannel == EditorChannel.PLAYER_CHAT) {
			layout = value.toPixels(screenWidth, screenHeight);
		} else {
			commandLayout = value.toPixels(screenWidth, screenHeight);
		}
	}

	public void setText(ChatTextConfig value) {
		if (selectedChannel == EditorChannel.PLAYER_CHAT) text = value.sanitized();
		else commandSystem = new CommandSystemConfig(
				commandSystem.enabled(), commandSystem.layout(), value.sanitized(),
				commandSystem.background(), commandSystem.textColor(),
				commandSystem.maximumMessages(), commandSystem.fadeSeconds(),
				commandSystem.messageSpacing(), commandSystem.scrollSpeed(),
				commandSystem.outline(), commandSystem.outlineColor(),
				commandSystem.outlineOpacity()).sanitized();
	}

	public void setBackground(ChatBackgroundConfig value) {
		if (selectedChannel == EditorChannel.PLAYER_CHAT) background = value.sanitized();
		else commandSystem = new CommandSystemConfig(
				commandSystem.enabled(), commandSystem.layout(), commandSystem.text(),
				value.sanitized(), commandSystem.textColor(), commandSystem.maximumMessages(),
				commandSystem.fadeSeconds(), commandSystem.messageSpacing(),
				commandSystem.scrollSpeed(), commandSystem.outline(),
				commandSystem.outlineColor(), commandSystem.outlineOpacity()).sanitized();
	}

	public void setPlayerColors(PlayerColorConfig value) {
		playerColors = value.sanitized();
	}

	public void setMention(MentionConfig value) {
		mention = value.sanitized();
	}

	public void setCommandClipboard(CommandClipboardConfig value) {
		commandClipboard = value.sanitized();
	}

	public void apply(EditorSnapshot value) {
		layout = value.layout().toPixels(screenWidth, screenHeight);
		text = value.text();
		background = value.background();
		playerColors = value.playerColors();
		mention = value.mention();
		commandClipboard = value.commandClipboard();
		commandSystem = value.commandSystem();
		commandLayout = commandSystem.layout().toPixels(screenWidth, screenHeight);
	}

	public void resizeViewport(int width, int height) {
		LayoutConfig ratios = snapshot().layout();
		LayoutConfig commandRatios = LayoutConfig.fromPixels(commandLayout, screenWidth, screenHeight);
		screenWidth = Math.max(1, width);
		screenHeight = Math.max(1, height);
		layout = ratios.toPixels(screenWidth, screenHeight);
		commandLayout = commandRatios.toPixels(screenWidth, screenHeight);
	}

	public void restoreLayoutDefaults() {
		if (selectedChannel == EditorChannel.PLAYER_CHAT) apply(LayoutConfig.DEFAULT);
		else commandLayout = CommandSystemConfig.DEFAULT.layout().toPixels(screenWidth, screenHeight);
		commit();
	}

	public EditorChannel selectedChannel() {
		return selectedChannel;
	}

	public void select(EditorChannel channel) {
		selectedChannel = channel == null ? EditorChannel.PLAYER_CHAT : channel;
	}

	private CommandSystemConfig withCommandLayout(LayoutConfig value) {
		return new CommandSystemConfig(
				commandSystem.enabled(), value, commandSystem.text(), commandSystem.background(),
				commandSystem.textColor(), commandSystem.maximumMessages(),
				commandSystem.fadeSeconds(), commandSystem.messageSpacing(),
				commandSystem.scrollSpeed(), commandSystem.outline(),
				commandSystem.outlineColor(), commandSystem.outlineOpacity()).sanitized();
	}

	public void restoreTextDefaults() {
		setText(ChatTextConfig.DEFAULT);
		commit();
	}

	public void restoreBackgroundDefaults() {
		setBackground(ChatBackgroundConfig.DEFAULT);
		commit();
	}

	public void restorePlayerColorDefaults() {
		setPlayerColors(PlayerColorConfig.DEFAULT);
		commit();
	}

	public void restoreMentionDefaults() {
		setMention(MentionConfig.DEFAULT);
		commit();
	}

	public void restoreCommandClipboardDefaults() {
		setCommandClipboard(CommandClipboardConfig.DEFAULT);
		commit();
	}

	public void commit() {
		history.record(snapshot());
	}

	public boolean undo() {
		return history.undo().map(state -> {
			apply(state);
			return true;
		}).orElse(false);
	}

	public boolean redo() {
		return history.redo().map(state -> {
			apply(state);
			return true;
		}).orElse(false);
	}

	public boolean canUndo() {
		return history.canUndo();
	}

	public boolean canRedo() {
		return history.canRedo();
	}

	public EditorHistory history() {
		return history;
	}
}
