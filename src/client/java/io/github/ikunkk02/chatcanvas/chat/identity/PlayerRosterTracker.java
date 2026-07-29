package io.github.ikunkk02.chatcanvas.chat.identity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class PlayerRosterTracker {
	private static final List<PlayerChatIdentity> OFFLINE_PREVIEW = List.of(
			preview("Steve"), preview("Alex"), preview("寿云"));
	private static volatile List<PlayerChatIdentity> online = List.of();
	private static volatile long revision;

	private PlayerRosterTracker() {
	}

	public static void refresh(ClientGamePacketListener handler) {
		if (handler == null) {
			clear();
			return;
		}
		List<PlayerChatIdentity> updated = handler.getListedPlayers().stream()
				.map(PlayerInfo::getProfile)
				.map(PlayerRosterTracker::fromProfile)
				.sorted(Comparator.comparing(PlayerChatIdentity::playerName,
						String.CASE_INSENSITIVE_ORDER))
				.toList();
		if (!updated.equals(online)) {
			online = updated;
			revision++;
		}
	}

	public static void refreshFromClient() {
		refresh(Minecraft.getInstance().getConnection());
	}

	public static void clear() {
		if (!online.isEmpty()) {
			online = List.of();
			revision++;
		}
	}

	public static Collection<PlayerChatIdentity> onlinePlayers() {
		return online;
	}

	public static List<PlayerChatIdentity> editorPlayers() {
		return online.isEmpty() ? OFFLINE_PREVIEW : online;
	}

	public static boolean usingPreviewPlayers() {
		return online.isEmpty();
	}

	public static long revision() {
		return revision;
	}

	private static PlayerChatIdentity fromProfile(GameProfile profile) {
		return new PlayerChatIdentity(profile.uuid(), profile.name(), true);
	}

	private static PlayerChatIdentity preview(String name) {
		UUID uuid = UUID.nameUUIDFromBytes(
				("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
		return new PlayerChatIdentity(uuid, name, true);
	}
}
