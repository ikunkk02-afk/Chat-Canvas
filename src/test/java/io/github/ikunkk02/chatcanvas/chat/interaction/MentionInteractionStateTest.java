package io.github.ikunkk02.chatcanvas.chat.interaction;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitbox;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MentionInteractionStateTest {
	private static final Object SCREEN = new Object();
	private static final UUID STEVE = UUID.fromString("12345678-1234-5678-9abc-123456789abc");

	@Test
	void requiresTwoNearbyClicksOnSameUuidAndMessage() {
		MentionInteractionState state = new MentionInteractionState();
		assertFalse(state.click(hitbox(STEVE, "Steve", 2), 1_000, 11, 11, 350, SCREEN));
		assertTrue(state.click(hitbox(STEVE, "DifferentDisplay", 2),
				1_200, 13, 12, 350, SCREEN));
		assertFalse(state.armed());
	}

	@Test
	void fallsBackToNormalizedNameOnlyWhenUuidIsUnavailable() {
		MentionInteractionState state = new MentionInteractionState();
		assertFalse(state.click(hitbox(null, "Steve", 1), 1_000, 10, 10, 350, SCREEN));
		assertTrue(state.click(hitbox(null, "sTeVe", 1), 1_100, 10, 10, 350, SCREEN));
	}

	@Test
	void timeoutDistanceMessageAndResetPreventFalseDoubleClicks() {
		MentionInteractionState state = new MentionInteractionState();
		state.click(hitbox(STEVE, "Steve", 1), 1_000, 10, 10, 350, SCREEN);
		assertFalse(state.click(hitbox(STEVE, "Steve", 1), 1_500, 10, 10, 350, SCREEN));
		assertFalse(state.click(hitbox(STEVE, "Steve", 1), 1_600, 30, 30, 350, SCREEN));
		assertFalse(state.click(hitbox(STEVE, "Steve", 2), 1_700, 30, 30, 350, SCREEN));
		state.reset();
		assertFalse(state.click(hitbox(STEVE, "Steve", 2), 1_800, 30, 30, 350, SCREEN));
	}

	@Test
	void tripleClickCanOnlyCompleteOnePair() {
		MentionInteractionState state = new MentionInteractionState();
		assertFalse(state.click(hitbox(STEVE, "Steve", 1), 100, 10, 10, 350, SCREEN));
		assertTrue(state.click(hitbox(STEVE, "Steve", 1), 200, 10, 10, 350, SCREEN));
		assertFalse(state.click(hitbox(STEVE, "Steve", 1), 300, 10, 10, 350, SCREEN));
	}

	private static PlayerNameHitbox hitbox(UUID uuid, String name, int messageIndex) {
		return new PlayerNameHitbox(uuid, name, messageIndex, 5, 5, 25, 15);
	}
}
