package io.github.ikunkk02.chatcanvas.chat.identity;

import io.github.ikunkk02.chatcanvas.config.PlayerColorConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerColorMode;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

public final class PlayerNameColorProvider {
	private PlayerColorConfig config = PlayerColorConfig.DEFAULT;
	private final Map<IdentityKey, OptionalInt> cache = new HashMap<>();

	public void updateConfig(PlayerColorConfig value) {
		PlayerColorConfig safe = value == null ? PlayerColorConfig.DEFAULT : value.sanitized();
		if (safe.equals(config)) return;
		config = safe;
		cache.clear();
	}

	public OptionalInt colorFor(PlayerChatIdentity identity) {
		if (identity == null || !config.enabled() || config.mode() == PlayerColorMode.VANILLA) {
			return OptionalInt.empty();
		}
		IdentityKey key = IdentityKey.of(identity);
		return cache.computeIfAbsent(key, ignored -> resolve(identity));
	}

	public boolean isCustom(PlayerChatIdentity identity) {
		return identity != null && config.hasOverride(identity.uuid(), identity.playerName());
	}

	public int cacheSize() {
		return cache.size();
	}

	private OptionalInt resolve(PlayerChatIdentity identity) {
		if (identity.uuid() != null) {
			Integer override = config.uuidOverrides().get(identity.uuid().toString());
			if (override != null) return OptionalInt.of(override);
		}
		String normalizedName = PlayerColorConfig.normalizeName(identity.playerName());
		Integer nameOverride = config.nameOverrides().get(normalizedName);
		if (nameOverride != null) return OptionalInt.of(nameOverride);

		int hash;
		if (identity.uuid() != null) {
			hash = identity.uuid().hashCode();
		} else if (!normalizedName.isEmpty()) {
			hash = normalizedName.hashCode();
		} else {
			return OptionalInt.empty();
		}
		if (config.palette().isEmpty()) return OptionalInt.empty();
		return OptionalInt.of(config.palette().get(
				Math.floorMod(hash, config.palette().size())));
	}

	private record IdentityKey(java.util.UUID uuid, String normalizedName) {
		private static IdentityKey of(PlayerChatIdentity identity) {
			return new IdentityKey(identity.uuid(),
					PlayerColorConfig.normalizeName(identity.playerName()));
		}
	}
}
