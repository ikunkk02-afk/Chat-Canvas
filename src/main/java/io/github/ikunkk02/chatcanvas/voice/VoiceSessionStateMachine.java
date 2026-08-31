package io.github.ikunkk02.chatcanvas.voice;

import java.util.EnumSet;
import java.util.Map;

public final class VoiceSessionStateMachine {
	private static final Map<VoiceInputState, EnumSet<VoiceInputState>> ALLOWED = Map.of(
			VoiceInputState.IDLE, EnumSet.of(VoiceInputState.OPENING_CHAT),
			VoiceInputState.OPENING_CHAT, EnumSet.of(VoiceInputState.WAITING_FOR_SPEECH, VoiceInputState.IDLE),
			VoiceInputState.WAITING_FOR_SPEECH, EnumSet.of(VoiceInputState.SPEAKING,
					VoiceInputState.FINALIZING, VoiceInputState.IDLE),
			VoiceInputState.SPEAKING, EnumSet.of(VoiceInputState.WAITING_FOR_ENDPOINT,
					VoiceInputState.FINALIZING, VoiceInputState.IDLE),
			VoiceInputState.WAITING_FOR_ENDPOINT, EnumSet.of(VoiceInputState.SPEAKING,
					VoiceInputState.FINALIZING, VoiceInputState.IDLE),
			VoiceInputState.FINALIZING, EnumSet.of(VoiceInputState.COMMITTING_RESULT, VoiceInputState.IDLE),
			VoiceInputState.COMMITTING_RESULT, EnumSet.of(VoiceInputState.IDLE)
	);
	private VoiceInputState state = VoiceInputState.IDLE;

	public synchronized VoiceInputState state() { return state; }

	public synchronized void transition(VoiceInputState next) {
		if (next == state) return;
		if (!ALLOWED.getOrDefault(state, EnumSet.noneOf(VoiceInputState.class)).contains(next)) {
			throw new IllegalStateException("Invalid voice transition " + state + " -> " + next);
		}
		state = next;
	}

	public synchronized void forceIdle() { state = VoiceInputState.IDLE; }
}
