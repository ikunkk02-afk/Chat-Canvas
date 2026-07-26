package io.github.ikunkk02.chatcanvas.chat.message;

import io.github.ikunkk02.chatcanvas.chat.identity.ChatMessageMetadata;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import io.github.ikunkk02.chatcanvas.chat.identity.PluginChatFallbackResolver;
import io.github.ikunkk02.chatcanvas.config.PlayerColorConfig;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.util.Locale;

public final class DefaultMessageClassifier implements MessageClassifier {
	@Override
	public ClassifiedMessage classify(Text message, MessageContext context) {
		if (message == null) throw new IllegalArgumentException("message");
		MessageContext safe = context == null
				? MessageContext.direct(java.util.List.of(), null, "")
				: context;
		if (safe.ingress() == MessageIngress.CHAT) {
			return player(message, safe.sender(), safe);
		}
		if (safe.ingress() == MessageIngress.COMMAND_INPUT) {
			return system(message, ChatCanvasMessageSource.COMMAND_INPUT);
		}
		if (safe.ingress() == MessageIngress.CHAT_CANVAS_ERROR) {
			return system(message, ChatCanvasMessageSource.CHAT_CANVAS_ERROR);
		}
		if (safe.overlay()) {
			return system(message, ChatCanvasMessageSource.UNKNOWN);
		}

		ClassifiedMessage translatedPlayerChat = translatedPlayerChat(message, safe);
		if (translatedPlayerChat != null) return translatedPlayerChat;

		ChatCanvasMessageSource vanilla = vanillaSource(message);
		if (vanilla != null) return system(message, vanilla);

		var plugin = PluginChatFallbackResolver.resolve(message, safe.onlinePlayers());
		if (plugin.isPresent()) return player(message, plugin.get().sender(), safe);
		return system(message, safe.ingress() == MessageIngress.GAME
				? ChatCanvasMessageSource.SYSTEM
				: ChatCanvasMessageSource.UNKNOWN);
	}

	private static ClassifiedMessage player(
			Text message, PlayerChatIdentity sender, MessageContext context) {
		boolean self = isSelf(sender, context);
		return new ClassifiedMessage(
				ChatCanvasChannel.PLAYER_CHAT,
				self ? ChatCanvasMessageSource.SELF_PLAYER : ChatCanvasMessageSource.PLAYER,
				sender == null ? null : sender.uuid(),
				sender == null ? null : Text.literal(sender.playerName()),
				message,
				self
		);
	}

	private static ClassifiedMessage system(Text message, ChatCanvasMessageSource source) {
		return new ClassifiedMessage(
				ChatCanvasChannel.COMMAND_SYSTEM, source,
				null, null, message, false);
	}

	private static boolean isSelf(PlayerChatIdentity sender, MessageContext context) {
		if (sender == null) return false;
		if (sender.uuid() != null && context.localPlayerUuid() != null) {
			if (!sender.uuid().equals(context.localPlayerUuid())) return false;
			if (sender.reliable()) return true;
			return uniquelyMatchesLocalPlayer(sender, context);
		}
		if (!sender.reliable()) return false;
		return uniquelyMatchesLocalPlayer(sender, context);
	}

	private static boolean uniquelyMatchesLocalPlayer(
			PlayerChatIdentity sender, MessageContext context) {
		String senderName = PlayerColorConfig.normalizeName(sender.playerName());
		String localName = PlayerColorConfig.normalizeName(context.localPlayerName());
		if (senderName.isEmpty() || !senderName.equals(localName)) return false;
		long matches = context.onlinePlayers().stream()
				.filter(player -> PlayerColorConfig.normalizeName(player.playerName())
						.equals(localName))
				.count();
		return matches == 1;
	}

	private static ChatCanvasMessageSource vanillaSource(Text message) {
		if (!(message.getContent() instanceof TranslatableTextContent translated)) return null;
		String key = translated.getKey().toLowerCase(Locale.ROOT);
		if (key.startsWith("death.")) return ChatCanvasMessageSource.DEATH_MESSAGE;
		if (key.equals("multiplayer.player.joined")
				|| key.equals("multiplayer.player.joined.renamed")) {
			return ChatCanvasMessageSource.PLAYER_JOIN;
		}
		if (key.equals("multiplayer.player.left")) return ChatCanvasMessageSource.PLAYER_LEAVE;
		if (isCommandError(key)) return ChatCanvasMessageSource.COMMAND_ERROR;
		if (key.startsWith("commands.") || key.startsWith("command.")) {
			return ChatCanvasMessageSource.COMMAND_RESULT;
		}
		if (key.startsWith("chat.") || key.startsWith("multiplayer.")) {
			return ChatCanvasMessageSource.SYSTEM;
		}
		return ChatCanvasMessageSource.SERVER_NOTICE;
	}

	private static ClassifiedMessage translatedPlayerChat(Text message, MessageContext context) {
		if (!(message.getContent() instanceof TranslatableTextContent translated)
				|| !translated.getKey().equals("chat.type.text")) {
			return null;
		}
		Object[] args = translated.getArgs();
		if (args.length == 0) return null;
		String candidate = args[0] instanceof Text text
				? text.getString() : String.valueOf(args[0]);
		PlayerChatIdentity matched = null;
		for (PlayerChatIdentity player : context.onlinePlayers()) {
			if (!PlayerColorConfig.normalizeName(player.playerName()).equals(
					PlayerColorConfig.normalizeName(candidate))) continue;
			if (matched != null) return null;
			matched = player;
		}
		return matched == null ? null : player(message, matched, context);
	}

	private static boolean isCommandError(String key) {
		return key.contains("unknown")
				|| key.contains("invalid")
				|| key.contains("failed")
				|| key.contains("exception")
				|| key.startsWith("argument.")
				|| key.startsWith("brigadier.");
	}
}
