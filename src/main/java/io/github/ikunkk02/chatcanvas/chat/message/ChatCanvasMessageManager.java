package io.github.ikunkk02.chatcanvas.chat.message;

public final class ChatCanvasMessageManager {
	public static final int DEFAULT_PLAYER_CAPACITY = 500;
	public static final int DEFAULT_COMMAND_CAPACITY = 300;
	private static final ChatCanvasMessageManager INSTANCE =
			new ChatCanvasMessageManager(DEFAULT_PLAYER_CAPACITY, DEFAULT_COMMAND_CAPACITY);

	private final ChatChannelHistory playerChat;
	private final ChatChannelHistory commandSystem;

	public ChatCanvasMessageManager(int playerCapacity, int commandCapacity) {
		playerChat = new ChatChannelHistory(playerCapacity);
		commandSystem = new ChatChannelHistory(commandCapacity);
	}

	public static ChatCanvasMessageManager instance() {
		return INSTANCE;
	}

	public boolean add(ChatCanvasMessage message) {
		return history(message.channel()).add(message);
	}

	public ChatChannelHistory history(ChatCanvasChannel channel) {
		return channel == ChatCanvasChannel.PLAYER_CHAT ? playerChat : commandSystem;
	}

	public ChatChannelHistory playerChat() {
		return playerChat;
	}

	public ChatChannelHistory commandSystem() {
		return commandSystem;
	}

	public void clearWorld() {
		playerChat.clear();
		commandSystem.clear();
	}

	public void invalidateLayouts() {
		playerChat.invalidateLayout();
		commandSystem.invalidateLayout();
	}

	public void invalidateLayout(ChatCanvasChannel channel) {
		history(channel).invalidateLayout();
	}
}
