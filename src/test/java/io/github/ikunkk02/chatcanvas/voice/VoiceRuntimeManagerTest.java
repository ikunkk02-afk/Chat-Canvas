package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VoiceRuntimeManagerTest {
	@Test
	void loadsAndroidNativeDependenciesInDtNeededOrder() {
		Path jni = Path.of("libsherpa-onnx-jni.so");
		Path cxx = Path.of("libsherpa-onnx-cxx-api.so");
		Path onnx = Path.of("libonnxruntime.so");
		Path c = Path.of("libsherpa-onnx-c-api.so");

		assertEquals(List.of(onnx, c, cxx),
				VoiceRuntimeManager.dependencyLoadOrder(List.of(jni, cxx, onnx, c)));
	}
}
