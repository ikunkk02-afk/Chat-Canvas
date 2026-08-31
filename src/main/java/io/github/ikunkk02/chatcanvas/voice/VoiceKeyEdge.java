package io.github.ikunkk02.chatcanvas.voice;

public final class VoiceKeyEdge {
	private boolean down;

	public synchronized Decision press(boolean eligible) {
		if (!eligible) return Decision.PASS;
		if (down) return Decision.CONSUME;
		down = true;
		return Decision.TRIGGER;
	}

	public synchronized Decision repeat(boolean eligible) {
		return eligible && down ? Decision.CONSUME : Decision.PASS;
	}

	public synchronized void release() { down = false; }
	public synchronized boolean isDown() { return down; }

	public enum Decision { PASS, TRIGGER, CONSUME }
}
