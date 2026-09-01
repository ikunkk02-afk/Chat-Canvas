package io.github.ikunkk02.chatcanvas.chat.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatLineHitboxTest {
	@Test
	void containsUsesTheSameScreenBoundsAsTheRenderedLine() {
		ChatLineHitbox hitbox = new ChatLineHitbox(120, 80, 96, 14, 1.0);

		assertTrue(hitbox.contains(120, 80));
		assertTrue(hitbox.contains(216, 94));
		assertFalse(hitbox.contains(119.99, 87));
		assertFalse(hitbox.contains(216.01, 87));
		assertFalse(hitbox.contains(150, 94.01));
	}

	@Test
	void textCoordinateTracksEverySupportedTextScale() {
		for (double scale : new double[]{0.75, 1.0, 1.25, 1.5, 2.0}) {
			ChatLineHitbox hitbox = new ChatLineHitbox(240, 120, 180, 18, scale);
			assertEquals(40.0, hitbox.textX(240 + 40 * scale), 0.00001);
		}
	}

	@Test
	void movingTheChatWindowDoesNotChangeTheLocalTextCoordinate() {
		ChatLineHitbox first = new ChatLineHitbox(20, 40, 120, 12, 1.25);
		ChatLineHitbox moved = new ChatLineHitbox(320, 240, 120, 12, 1.25);

		assertEquals(first.textX(70), moved.textX(370), 0.00001);
		assertTrue(first.contains(70, 45));
		assertTrue(moved.contains(370, 245));
	}

	@Test
	void invalidScaleFallsBackToOne() {
		ChatLineHitbox hitbox = new ChatLineHitbox(10, 10, 40, 10, 0.0);
		assertEquals(12.0, hitbox.textX(22), 0.00001);
	}
}
