package io.github.ikunkk02.chatcanvas.voice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class VoiceModelRegistry {
	public static final String VOSK_CN = "vosk-model-small-cn-0.22";
	public static final String ZIPFORMER_ZH = "sherpa-onnx-streaming-zipformer-zh-14m-2023-02-23";
	public static final String SENSE_VOICE = "sherpa-onnx-sense-voice-int8-2024-07-17";
	public static final String WHISPER_TINY = "sherpa-onnx-whisper-tiny-int8";

	private static final String ZIP_REV = "204ad334e2e683fd295359930cc16fc0432a23ac";
	private static final String SENSE_REV = "2365baeacb507f821a0c8120fcee3d484dba7a07";
	private static final String WHISPER_REV = "65176e2deb88badc814a94058666cadccc29b61c";
	private static final Map<String, VoiceModelDescriptor> MODELS = create();

	private VoiceModelRegistry() { }

	public static List<VoiceModelDescriptor> all() { return List.copyOf(MODELS.values()); }

	public static VoiceModelDescriptor get(String id) { return MODELS.get(id); }

	public static VoiceModelDescriptor defaultModel() { return MODELS.get(SENSE_VOICE); }

	private static Map<String, VoiceModelDescriptor> create() {
		Map<String, VoiceModelDescriptor> models = new LinkedHashMap<>();
		models.put(ZIPFORMER_ZH, new VoiceModelDescriptor(
				ZIPFORMER_ZH, "chat_canvas.voice.model.zipformer", VoiceModelProvider.SHERPA_ONLINE,
				List.of("chat_canvas.voice.language.mandarin"), 30_975_688L, 30_975_688L,
				"chat_canvas.voice.profile.low", "chat_canvas.voice.profile.basic_good",
				"chat_canvas.voice.profile.extremely_fast", true, true, true, true, true, true,
				ReloadPolicy.HOT_SWAP, "chat_canvas.voice.model.zipformer.description", ZIPFORMER_ZH,
				List.of(
						hf("csukuangfj/sherpa-onnx-streaming-zipformer-zh-14M-2023-02-23", ZIP_REV,
								"encoder-epoch-99-avg-1.int8.onnx", 21_621_684L, "1c556ea57cec304e55ec4b72e52c1cc098bb01476ed7d90f3de939fe126487b1"),
						hf("csukuangfj/sherpa-onnx-streaming-zipformer-zh-14M-2023-02-23", ZIP_REV,
								"decoder-epoch-99-avg-1.onnx", 7_509_745L, "5ee0f03a2768ff1d5c83ef3a493243c7935d316cd41280037b14783a3467cc78"),
						hf("csukuangfj/sherpa-onnx-streaming-zipformer-zh-14M-2023-02-23", ZIP_REV,
								"joiner-epoch-99-avg-1.int8.onnx", 1_795_562L, "a7cf9d82757bdcf786059454495a9ca95e4bd7347f72473fc08d794475c36169"),
						hf("csukuangfj/sherpa-onnx-streaming-zipformer-zh-14M-2023-02-23", ZIP_REV,
								"tokens.txt", 48_697L, "8b294db9045d6e5f94647f4c1eec1af4da143a75053c399611444b378ff966ac")
				)));
		models.put(SENSE_VOICE, new VoiceModelDescriptor(
				SENSE_VOICE, "chat_canvas.voice.model.sense_voice", VoiceModelProvider.SHERPA_SENSE_VOICE,
				List.of("chat_canvas.voice.language.mandarin", "chat_canvas.voice.language.cantonese",
						"chat_canvas.voice.language.english", "chat_canvas.voice.language.japanese",
						"chat_canvas.voice.language.korean"),
				239_549_806L, 239_549_806L, "chat_canvas.voice.profile.medium",
				"chat_canvas.voice.profile.high", "chat_canvas.voice.profile.fast", false,
				true, true, true, true, true, ReloadPolicy.HOT_SWAP,
				"chat_canvas.voice.model.sense_voice.description", SENSE_VOICE,
				List.of(
						hf("csukuangfj/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-2024-07-17", SENSE_REV,
								"model.int8.onnx", 239_233_841L, "c71f0ce00bec95b07744e116345e33d8cbbe08cef896382cf907bf4b51a2cd51"),
						hf("csukuangfj/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-2024-07-17", SENSE_REV,
								"tokens.txt", 315_894L, "f449eb28dc567533d7fa59be34e2abca8784f771850c78a47fb731a31429a1dc"),
						hf("csukuangfj/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-2024-07-17", SENSE_REV,
								"LICENSE", 71L, "221c6df10b0931a5629adad671ea48fb7747e034c414b6d2bfa275bc3dd4ea17")
				)));
		models.put(WHISPER_TINY, new VoiceModelDescriptor(
				WHISPER_TINY, "chat_canvas.voice.model.whisper_tiny", VoiceModelProvider.SHERPA_WHISPER,
				List.of("chat_canvas.voice.language.multilingual"), 103_609_903L, 103_609_903L,
				"chat_canvas.voice.profile.medium_high", "chat_canvas.voice.profile.multilingual_excellent",
				"chat_canvas.voice.profile.medium", false, true, true, true, true, true,
				ReloadPolicy.HOT_SWAP, "chat_canvas.voice.model.whisper_tiny.description", WHISPER_TINY,
				List.of(
						hf("csukuangfj/sherpa-onnx-whisper-tiny", WHISPER_REV,
								"tiny-encoder.int8.onnx", 12_937_772L, "d24fb083ae3b1041fc24e97971d60e280c9342201fbb67b0ab428a8b4a51a434"),
						hf("csukuangfj/sherpa-onnx-whisper-tiny", WHISPER_REV,
								"tiny-decoder.int8.onnx", 89_855_401L, "d2fece8dd42771f1df975c6c0445770d0c292bf7547c2cae04a6c0cc57540925"),
						hf("csukuangfj/sherpa-onnx-whisper-tiny", WHISPER_REV,
								"tiny-tokens.txt", 816_730L, "b34b360dbb493e781e479794586d661700670d65564001f23024971d1f2fa126")
				)));
		models.put(VOSK_CN, new VoiceModelDescriptor(
				VOSK_CN, "chat_canvas.voice.model.vosk_cn", VoiceModelProvider.VOSK,
				List.of("chat_canvas.voice.language.mandarin"), 43_898_754L, 68_292_271L,
				"chat_canvas.voice.profile.low", "chat_canvas.voice.profile.compatibility",
				"chat_canvas.voice.profile.fast", true, false, false, true, true, true,
				ReloadPolicy.HOT_SWAP, "chat_canvas.voice.model.vosk_cn.description", VOSK_CN,
				List.of(new VoiceModelFile("vosk-model-small-cn-0.22.zip",
						"https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip",
						43_898_754L, "3AF8B0E7E0F835AE9D414CE5DF580237A3CFB08D586C9FBBB0F7FF29AD5B14BA"))));
		return Map.copyOf(models);
	}

	private static VoiceModelFile hf(String repository, String revision, String file,
								 long size, String sha256) {
		return new VoiceModelFile(file,
				"https://huggingface.co/" + repository + "/resolve/" + revision + "/" + file,
				size, sha256);
	}
}
