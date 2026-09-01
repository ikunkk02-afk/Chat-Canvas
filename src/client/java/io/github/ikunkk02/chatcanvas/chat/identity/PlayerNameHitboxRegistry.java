package io.github.ikunkk02.chatcanvas.chat.identity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

	public static synchronized Optional<PlayerNameHitbox> findAt(double mouseX, double mouseY) {
		return VISIBLE.stream()
				.filter(hitbox -> mouseX >= hitbox.left() && mouseX <= hitbox.right()
						&& mouseY >= hitbox.top() && mouseY <= hitbox.bottom())
				.min(Comparator
						.comparingDouble(PlayerNameHitboxRegistry::area)
						.thenComparingDouble(hitbox -> centerDistanceSquared(hitbox, mouseX, mouseY))
						.thenComparingInt(hitbox -> -VISIBLE.indexOf(hitbox)));
	}

	public static synchronized void clear() {
		VISIBLE.clear();
	}

	private static double area(PlayerNameHitbox hitbox) {
		return Math.max(0.0, hitbox.right() - hitbox.left())
				* Math.max(0.0, hitbox.bottom() - hitbox.top());
	}

	private static double centerDistanceSquared(PlayerNameHitbox hitbox, double x, double y) {
		double dx = (hitbox.left() + hitbox.right()) * 0.5 - x;
		double dy = (hitbox.top() + hitbox.bottom()) * 0.5 - y;
		return dx * dx + dy * dy;
	}
}
