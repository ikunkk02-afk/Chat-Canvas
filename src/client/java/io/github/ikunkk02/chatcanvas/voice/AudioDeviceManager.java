package io.github.ikunkk02.chatcanvas.voice;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AudioDeviceManager {
	public static final AudioFormat TARGET_FORMAT = new AudioFormat(
			AudioFormat.Encoding.PCM_SIGNED, 16_000.0f, 16, 1, 2, 16_000.0f, false);

	public List<AudioDevice> devices() {
		Map<String, Integer> occurrences = new HashMap<>();
		List<AudioDevice> devices = new ArrayList<>();
		for (Mixer.Info info : AudioSystem.getMixerInfo()) {
			Mixer mixer = AudioSystem.getMixer(info);
			if (mixer.getTargetLineInfo().length == 0) continue;
			String base = info.getName() + "\u001f" + info.getVendor() + "\u001f"
					+ info.getDescription() + "\u001f" + info.getVersion();
			int occurrence = occurrences.merge(base, 1, Integer::sum) - 1;
			devices.add(new AudioDevice(base + "\u001f" + occurrence,
					info.getName(), info, mixer.isLineSupported(
							new DataLine.Info(TargetDataLine.class, TARGET_FORMAT))));
		}
		return List.copyOf(devices);
	}

	public OpenedMicrophone open(String requestedId) throws Exception {
		AudioDevice selected = devices().stream()
				.filter(device -> device.id().equals(requestedId)).findFirst().orElse(null);
		if (selected != null) {
			OpenedMicrophone opened = tryOpen(selected.info(), TARGET_FORMAT);
			if (opened != null) return opened;
			opened = tryFallback(selected.info());
			if (opened != null) return opened;
		}
		TargetDataLine line = AudioSystem.getTargetDataLine(TARGET_FORMAT);
		line.open(TARGET_FORMAT);
		return new OpenedMicrophone(line, TARGET_FORMAT, "default");
	}

	private static OpenedMicrophone tryOpen(Mixer.Info info, AudioFormat format) {
		try {
			Mixer mixer = AudioSystem.getMixer(info);
			DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, format);
			if (!mixer.isLineSupported(lineInfo)) return null;
			TargetDataLine line = (TargetDataLine) mixer.getLine(lineInfo);
			line.open(format);
			return new OpenedMicrophone(line, format, info.getName());
		} catch (Exception ignored) {
			return null;
		}
	}

	private static OpenedMicrophone tryFallback(Mixer.Info info) {
		for (float rate : new float[]{48_000.0f, 44_100.0f}) {
			for (int channels : new int[]{1, 2}) {
				AudioFormat format = new AudioFormat(
						AudioFormat.Encoding.PCM_SIGNED, rate, 16, channels,
						channels * 2, rate, false);
				OpenedMicrophone opened = tryOpen(info, format);
				if (opened != null) return opened;
			}
		}
		return null;
	}

	public record AudioDevice(String id, String displayName, Mixer.Info info,
							  boolean exactFormat) { }
	public record OpenedMicrophone(TargetDataLine line, AudioFormat format,
								  String displayName) { }
}
