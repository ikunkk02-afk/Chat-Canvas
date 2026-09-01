package io.github.ikunkk02.chatcanvas.voice;

import java.nio.file.Path;

/** Launcher service for a signed iOS sherpa JNI runtime. */
public interface SherpaRuntimeBridge {
	String platformId();
	boolean isAvailable();
	String unavailableReason();
	Path nativeDirectory();
}
