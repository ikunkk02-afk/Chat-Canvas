package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoiceKeyEdgeTest {
	@Test
	void onePhysicalPressTriggersExactlyOnceDespiteRepeat() {
		VoiceKeyEdge edge = new VoiceKeyEdge();
		assertEquals(VoiceKeyEdge.Decision.TRIGGER, edge.press(true));
		for (int i = 0; i < 10; i++) {
			assertEquals(VoiceKeyEdge.Decision.CONSUME, edge.repeat(true));
			assertEquals(VoiceKeyEdge.Decision.CONSUME, edge.press(true));
		}
		edge.release();
		assertEquals(VoiceKeyEdge.Decision.TRIGGER, edge.press(true));
	}

	@Test
	void ineligibleGuiDoesNotLatchOrConsumeKey() {
		VoiceKeyEdge edge = new VoiceKeyEdge();
		assertEquals(VoiceKeyEdge.Decision.PASS, edge.press(false));
		assertFalse(edge.isDown());
		assertEquals(VoiceKeyEdge.Decision.PASS, edge.repeat(false));
	}
}
