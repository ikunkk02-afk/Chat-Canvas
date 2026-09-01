package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoiceSessionStateMachineTest {
	@Test
	void followsCompleteEndpointLifecycle() {
		VoiceSessionStateMachine machine = new VoiceSessionStateMachine();
		machine.transition(VoiceInputState.OPENING_CHAT);
		machine.transition(VoiceInputState.WAITING_FOR_SPEECH);
		machine.transition(VoiceInputState.SPEAKING);
		machine.transition(VoiceInputState.WAITING_FOR_ENDPOINT);
		machine.transition(VoiceInputState.FINALIZING);
		machine.transition(VoiceInputState.COMMITTING_RESULT);
		machine.transition(VoiceInputState.IDLE);
		assertEquals(VoiceInputState.IDLE, machine.state());
	}

	@Test
	void rejectsSkippingFinalize() {
		VoiceSessionStateMachine machine = new VoiceSessionStateMachine();
		machine.transition(VoiceInputState.OPENING_CHAT);
		machine.transition(VoiceInputState.WAITING_FOR_SPEECH);
		assertThrows(IllegalStateException.class,
				() -> machine.transition(VoiceInputState.COMMITTING_RESULT));
	}

	@Test
	void permitsSpeechToResumeDuringEndpointWait() {
		VoiceSessionStateMachine machine = new VoiceSessionStateMachine();
		machine.transition(VoiceInputState.OPENING_CHAT);
		machine.transition(VoiceInputState.WAITING_FOR_SPEECH);
		machine.transition(VoiceInputState.SPEAKING);
		machine.transition(VoiceInputState.WAITING_FOR_ENDPOINT);
		machine.transition(VoiceInputState.SPEAKING);
		assertEquals(VoiceInputState.SPEAKING, machine.state());
	}
}
