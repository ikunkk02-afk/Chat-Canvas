package io.github.ikunkk02.chatcanvas.voice;

import java.util.List;

public record CaptureCapabilities(
		boolean available,
		String backendId,
		String displayName,
		int sampleRate,
		int channels,
		boolean permissionGranted,
		String unavailableReason,
		List<AudioCaptureDevice> devices
) {
	public CaptureCapabilities {
		backendId = backendId == null ? "" : backendId;
		displayName = displayName == null ? backendId : displayName;
		unavailableReason = unavailableReason == null ? "" : unavailableReason;
		devices = devices == null ? List.of() : List.copyOf(devices);
	}

	public static CaptureCapabilities unavailable(String id, String name, String reason) {
		return new CaptureCapabilities(false, id, name, 0, 0, false, reason, List.of());
	}
}
