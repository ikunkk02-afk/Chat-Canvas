package io.github.ikunkk02.chatcanvas.chat.identity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class PlayerRosterTracker {
	private static final UUID SHOUYUN_PREVIEW_UUID = previewUuid("Shouyun");
	private static volatile List<PlayerChatIdentity> online = List.of();
	private static volatile long revision;

	private PlayerRosterTracker() {
	}

	public static void refresh(ClientPacketListener handler) {
		if (handler == null) {
			clear();
			return;
		}
		List<PlayerChatIdentity> updated = handler.getListedOnlinePlayers().stream()
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
		return online.isEmpty() ? List.of(
				preview("Steve"),
				preview("Alex"),
				new PlayerChatIdentity(SHOUYUN_PREVIEW_UUID,
						Component.translatable("chat_canvas.preview.shouyun_name").getString(), true))
				: online;
	}

	public static boolean usingPreviewPlayers() {
		return online.isEmpty();
	}

	public static long revision() {
		return revision;
	}

	private static PlayerChatIdentity fromProfile(GameProfile profile) {
		return new PlayerChatIdentity(profile.id(), profile.name(), true);
	}

	private static PlayerChatIdentity preview(String name) {
		return new PlayerChatIdentity(previewUuid(name), name, true);
	}

	private static UUID previewUuid(String name) {
		return UUID.nameUUIDFromBytes(
				("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
	}
}
