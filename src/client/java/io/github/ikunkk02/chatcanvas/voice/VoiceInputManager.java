package io.github.ikunkk02.chatcanvas.voice;

import java.util.List;
import java.util.function.Consumer;

/** Stable UI facade; lifecycle and workers live in VoiceInputController. */
public final class VoiceInputManager {
	private final VoiceInputController controller = new VoiceInputController();
	private VoiceInputManager() { }
	public static VoiceInputManager instance() { return Holder.INSTANCE; }
	private static final class Holder { private static final VoiceInputManager INSTANCE = new VoiceInputManager(); }

	public void prepareQuickStart() { controller.prepareQuickStart(); }
	public boolean begin(Consumer<VoiceRecognitionResult> consumer) { return controller.begin(consumer); }
	public void finish() { controller.finish(); }
	public void cancel() { controller.cancel(); }
	public void installModel() { controller.installSelectedModel(); }
	public void installModel(String id, Consumer<VoiceRecognitionResult> consumer, boolean startAfter) {
		controller.installModel(id, consumer, startAfter);
	}
	public void selectModel(String id) { controller.selectModel(id); }
	public void warmSelectedModel() { controller.warmSelectedModel(); }
	public void toggleMicrophoneTest() { controller.toggleMicrophoneTest(); }
	public void stopMicrophoneTest() { controller.stopMicrophoneTest(); }
	public void cancelModelInstall() { controller.cancelModelInstall(); }
	public void releaseModel() { controller.releaseModel(); }
	public void openModelsDirectory() { controller.openModelsDirectory(); }
	public void refreshAvailability() { controller.refreshAvailability(); }
	public void updateSettings(VoiceSettings value) { controller.updateSettings(value); }
	public void shutdown() { controller.shutdown(); }

	public VoiceInputState state() { return controller.state(); }
	public VoiceSettings settings() { return controller.settings(); }
	public String partial() { return controller.partial(); }
	public double level() { return controller.level(); }
	public long progress() { return controller.progress(); }
	public long progressTotal() { return controller.progressTotal(); }
	public List<AudioCaptureDevice> devices() { return controller.devices(); }
	public List<VoiceModelDescriptor> models() { return controller.registeredModels(); }
	public VoiceModelDescriptor selectedModel() { return controller.selectedModel(); }
	public boolean isModelInstalled(String id) { return controller.isModelInstalled(id); }
	public VoiceModelCapability modelCapability(VoiceModelDescriptor model) { return controller.modelCapability(model); }
	public CaptureCapabilities captureCapabilities() { return controller.captureCapabilities(); }
	public int effectiveInferenceThreads() { return controller.effectiveInferenceThreads(); }
	public boolean isListening() { return controller.isListening(); }
	public boolean isBusy() { return controller.isBusy(); }
	public boolean isMicrophoneTesting() { return controller.isMicrophoneTesting(); }
	public double microphoneTestLevel() { return controller.microphoneTestLevel(); }
}
