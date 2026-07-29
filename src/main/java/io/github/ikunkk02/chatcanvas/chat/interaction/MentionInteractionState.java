package io.github.ikunkk02.chatcanvas.chat.interaction;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitbox;
import io.github.ikunkk02.chatcanvas.config.PlayerColorConfig;

import java.util.UUID;

public final class MentionInteractionState {
	public static final double MAX_CLICK_DISTANCE = 5.0;

	private UUID lastPlayerUuid;
	private String lastPlayerName = "";
	private int lastMessageIndex = -1;
	private long lastClickTimeMs;
	private double lastClickX;
	private double lastClickY;
	private Object screenToken;
	private boolean armed;

	public boolean click(
			PlayerNameHitbox hitbox,
			long nowMs,
			double mouseX,
			double mouseY,
			int intervalMs,
			Object currentScreenToken
	) {
		if (hitbox == null || currentScreenToken == null) {
			reset();
			return false;
		}
		boolean match = armed
				&& screenToken == currentScreenToken
				&& nowMs >= lastClickTimeMs
				&& nowMs - lastClickTimeMs <= intervalMs
				&& samePlayer(hitbox)
				&& sameRegion(hitbox)
				&& squaredDistance(mouseX, mouseY, lastClickX, lastClickY)
						<= MAX_CLICK_DISTANCE * MAX_CLICK_DISTANCE;
		if (match) {
			reset();
			return true;
		}
		arm(hitbox, nowMs, mouseX, mouseY, currentScreenToken);
		return false;
	}

	public void expire(long nowMs, int intervalMs) {
		if (armed && (nowMs < lastClickTimeMs || nowMs - lastClickTimeMs > intervalMs)) {
			reset();
		}
	}

	public void reset() {
		lastPlayerUuid = null;
		lastPlayerName = "";
		lastMessageIndex = -1;
		lastClickTimeMs = 0L;
		lastClickX = 0.0;
		lastClickY = 0.0;
		screenToken = null;
		armed = false;
	}

	public boolean armed() {
		return armed;
	}

	private void arm(PlayerNameHitbox hitbox, long nowMs, double mouseX, double mouseY,
					 Object currentScreenToken) {
		lastPlayerUuid = hitbox.playerUuid();
		lastPlayerName = PlayerColorConfig.normalizeName(hitbox.playerName());
		lastMessageIndex = hitbox.messageIndex();
		lastClickTimeMs = nowMs;
		lastClickX = mouseX;
		lastClickY = mouseY;
		screenToken = currentScreenToken;
		armed = true;
	}

	private boolean samePlayer(PlayerNameHitbox hitbox) {
		if (lastPlayerUuid != null && hitbox.playerUuid() != null) {
			return lastPlayerUuid.equals(hitbox.playerUuid());
		}
		return !lastPlayerName.isEmpty()
				&& lastPlayerName.equals(PlayerColorConfig.normalizeName(hitbox.playerName()));
	}

	private boolean sameRegion(PlayerNameHitbox hitbox) {
		return lastMessageIndex < 0 || hitbox.messageIndex() < 0
				|| lastMessageIndex == hitbox.messageIndex();
	}

	private static double squaredDistance(double x1, double y1, double x2, double y2) {
		double dx = x1 - x2;
		double dy = y1 - y2;
		return dx * dx + dy * dy;
	}
}
