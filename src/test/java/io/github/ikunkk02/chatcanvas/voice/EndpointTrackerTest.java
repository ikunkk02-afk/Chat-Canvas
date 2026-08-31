package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EndpointTrackerTest {
	@Test
	void detectsSpeechThenEndpointAfterConfiguredSilence() {
		EndpointTracker tracker = new EndpointTracker(250, 800);
		for (int i = 0; i < 7; i++) assertFalse(tracker.accept(true, 32).speechStarted());
		VadDecision started = tracker.accept(true, 32);
		assertTrue(started.speechStarted());
		assertTrue(started.speechActive());

		for (int i = 0; i < 24; i++) {
			VadDecision waiting = tracker.accept(false, 32);
			assertTrue(waiting.waitingForEndpoint());
			assertFalse(waiting.endpoint());
		}
		VadDecision endpoint = tracker.accept(false, 32);
		assertTrue(endpoint.endpoint());
		assertFalse(endpoint.waitingForEndpoint());
	}

	@Test
	void speechResumingBeforeEndpointClearsSilenceTimer() {
		EndpointTracker tracker = new EndpointTracker(64, 800);
		assertTrue(tracker.accept(true, 64).speechStarted());
		assertTrue(tracker.accept(false, 700).waitingForEndpoint());
		assertTrue(tracker.accept(true, 32).speechActive());
		assertFalse(tracker.accept(false, 200).endpoint());
	}

	@Test
	void isolatedNoiseNeverStartsSpeech() {
		EndpointTracker tracker = new EndpointTracker(250, 800);
		for (int i = 0; i < 30; i++) {
			assertFalse(tracker.accept(i % 2 == 0, 32).speechStarted());
		}
		assertFalse(tracker.speechDetected());
	}
}
