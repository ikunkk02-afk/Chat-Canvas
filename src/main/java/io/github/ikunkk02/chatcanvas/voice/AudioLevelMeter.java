package io.github.ikunkk02.chatcanvas.voice;

public final class AudioLevelMeter {
	private double smoothed;
	private long effectiveMillis;

	public double acceptPcm16Le(byte[] bytes, int length, double threshold, long chunkMillis) {
		long sum = 0L;
		int samples = Math.max(0, Math.min(length, bytes.length)) / 2;
		for (int index = 0; index < samples * 2; index += 2) {
			int sample = (short) ((bytes[index] & 0xff) | (bytes[index + 1] << 8));
			sum += (long) sample * sample;
		}
		double rms = samples == 0 ? 0.0
				: Math.sqrt((double) sum / samples) / 32768.0;
		smoothed += (rms - smoothed) * 0.25;
		if (rms >= threshold) effectiveMillis += Math.max(0L, chunkMillis);
		return smoothed;
	}

	public double smoothed() {
		return smoothed;
	}

	public boolean hasClearSpeech() {
		return effectiveMillis >= 200L;
	}
}
