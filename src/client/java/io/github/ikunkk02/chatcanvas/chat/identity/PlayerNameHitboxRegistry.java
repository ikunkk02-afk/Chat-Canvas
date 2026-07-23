package io.github.ikunkk02.chatcanvas.chat.identity;

import java.util.ArrayList;
import java.util.List;

public final class PlayerNameHitboxRegistry {
	private static final List<PlayerNameHitbox> VISIBLE = new ArrayList<>();

	private PlayerNameHitboxRegistry() {
	}

	public static synchronized void beginFrame() {
		VISIBLE.clear();
	}

	public static synchronized void add(PlayerNameHitbox hitbox) {
		VISIBLE.add(hitbox);
	}

	public static synchronized List<PlayerNameHitbox> visibleHitboxes() {
		return List.copyOf(VISIBLE);
	}

	public static synchronized void clear() {
		VISIBLE.clear();
	}
}
