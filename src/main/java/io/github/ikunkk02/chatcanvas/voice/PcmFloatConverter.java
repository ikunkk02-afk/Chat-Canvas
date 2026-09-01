package io.github.ikunkk02.chatcanvas.voice;

final class PcmFloatConverter {
	private PcmFloatConverter() { }

	static float[] convert(byte[] pcm, int length) {
		int samples = Math.max(0, Math.min(pcm.length, length)) / 2;
		float[] result = new float[samples];
		for (int i = 0; i < samples; i++) {
			short sample = (short) ((pcm[i * 2] & 0xff) | (pcm[i * 2 + 1] << 8));
			result[i] = sample / 32768.0f;
		}
		return result;
	}
}
