package io.github.ikunkk02.chatcanvas.voice;

import java.io.ByteArrayOutputStream;

public final class Pcm16MonoResampler {
	private final double inputRate;
	private final int channels;
	private double nextInputFrame;
	private long inputFrames;
	private short previous;
	private boolean hasPrevious;

	public Pcm16MonoResampler(float inputRate, int channels) {
		if (!Float.isFinite(inputRate) || inputRate <= 0) {
			throw new IllegalArgumentException("inputRate");
		}
		if (channels < 1 || channels > 2) throw new IllegalArgumentException("channels");
		this.inputRate = inputRate;
		this.channels = channels;
	}

	public byte[] convert(byte[] input, int length, boolean bigEndian) {
		int frameBytes = channels * 2;
		int frames = Math.max(0, Math.min(length, input.length)) / frameBytes;
		if (frames == 0) return new byte[0];
		short[] mono = new short[frames];
		for (int frame = 0; frame < frames; frame++) {
			int total = 0;
			for (int channel = 0; channel < channels; channel++) {
				int offset = frame * frameBytes + channel * 2;
				int value = bigEndian
						? (short) ((input[offset] << 8) | (input[offset + 1] & 0xff))
						: (short) ((input[offset] & 0xff) | (input[offset + 1] << 8));
				total += value;
			}
			mono[frame] = (short) (total / channels);
		}
		if (inputRate == 16_000.0) return encode(mono);

		ByteArrayOutputStream output = new ByteArrayOutputStream(
				(int) Math.ceil(frames * 32_000.0 / inputRate));
		double step = inputRate / 16_000.0;
		long firstFrame = inputFrames;
		long lastFrame = inputFrames + frames - 1L;
		if (!hasPrevious) {
			previous = mono[0];
			hasPrevious = true;
			nextInputFrame = firstFrame;
		}
		while (nextInputFrame <= lastFrame) {
			long lower = (long) Math.floor(nextInputFrame);
			double fraction = nextInputFrame - lower;
			short a = sample(mono, firstFrame, lower);
			short b = sample(mono, firstFrame, Math.min(lastFrame, lower + 1L));
			int interpolated = (int) Math.round(a + (b - a) * fraction);
			output.write(interpolated & 0xff);
			output.write((interpolated >>> 8) & 0xff);
			nextInputFrame += step;
		}
		previous = mono[frames - 1];
		inputFrames += frames;
		return output.toByteArray();
	}

	private short sample(short[] mono, long firstFrame, long absoluteFrame) {
		if (absoluteFrame < firstFrame) return previous;
		return mono[(int) Math.min(mono.length - 1, absoluteFrame - firstFrame)];
	}

	private static byte[] encode(short[] samples) {
		byte[] output = new byte[samples.length * 2];
		for (int i = 0; i < samples.length; i++) {
			output[i * 2] = (byte) samples[i];
			output[i * 2 + 1] = (byte) (samples[i] >>> 8);
		}
		return output;
	}
}
