package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Pcm16MonoResamplerTest {
	@Test
	void convertsFortyEightKhzStereoToSixteenKhzMono() {
		byte[] input = stereoFrames(4_800, (short) 10_000, (short) -2_000);
		byte[] output = new Pcm16MonoResampler(48_000.0f, 2)
				.convert(input, input.length, false);
		assertTrue(Math.abs(output.length - 3_200) <= 2);
		assertEquals(4_000, sample(output, 0), 1);
	}

	@Test
	void keepsStateAcrossFortyFourKhzChunks() {
		Pcm16MonoResampler resampler = new Pcm16MonoResampler(44_100.0f, 1);
		byte[] first = monoFrames(2_205, (short) 1_500);
		byte[] second = monoFrames(2_205, (short) 1_500);
		int total = resampler.convert(first, first.length, false).length
				+ resampler.convert(second, second.length, false).length;
		assertTrue(Math.abs(total - 3_200) <= 2);
	}

	private static byte[] stereoFrames(int frames, short left, short right) {
		byte[] bytes = new byte[frames * 4];
		for (int i = 0; i < frames; i++) {
			write(bytes, i * 4, left);
			write(bytes, i * 4 + 2, right);
		}
		return bytes;
	}

	private static byte[] monoFrames(int frames, short value) {
		byte[] bytes = new byte[frames * 2];
		for (int i = 0; i < frames; i++) write(bytes, i * 2, value);
		return bytes;
	}

	private static void write(byte[] bytes, int offset, short value) {
		bytes[offset] = (byte) value;
		bytes[offset + 1] = (byte) (value >>> 8);
	}

	private static int sample(byte[] bytes, int offset) {
		return (short) ((bytes[offset] & 0xff) | (bytes[offset + 1] << 8));
	}
}
