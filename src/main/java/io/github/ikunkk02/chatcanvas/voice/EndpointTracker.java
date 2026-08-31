package io.github.ikunkk02.chatcanvas.voice;

/** Pure timing policy driven by neural VAD frame decisions. */
public final class EndpointTracker {
	private final long minimumSpeechMillis;
	private final long endpointSilenceMillis;
	private long voicedMillis;
	private long silentMillis;
	private boolean speechDetected;

	public EndpointTracker(long minimumSpeechMillis, long endpointSilenceMillis) {
		this.minimumSpeechMillis = Math.max(0L, minimumSpeechMillis);
		this.endpointSilenceMillis = Math.max(1L, endpointSilenceMillis);
	}

	public VadDecision accept(boolean voiced, long frameMillis) {
		long duration = Math.max(0L, frameMillis);
		boolean started = false;
		if (!speechDetected) {
			voicedMillis = voiced ? voicedMillis + duration : 0L;
			if (voicedMillis >= minimumSpeechMillis) {
				speechDetected = true;
				started = true;
			}
			return new VadDecision(started, speechDetected && voiced, false, false);
		}
		if (voiced) {
			silentMillis = 0L;
			return new VadDecision(false, true, false, false);
		}
		silentMillis += duration;
		boolean endpoint = silentMillis >= endpointSilenceMillis;
		return new VadDecision(false, false, !endpoint, endpoint);
	}

	public boolean speechDetected() { return speechDetected; }

	public void reset() {
		voicedMillis = 0L;
		silentMillis = 0L;
		speechDetected = false;
	}
}
