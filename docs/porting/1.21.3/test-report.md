# Test Report: Chat Canvas 1.3.0 for Minecraft 1.21.3

## Environment

| Item | Value |
|------|-------|
| Minecraft | 1.21.3 |
| Chat Canvas | 1.3.0 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.106.1+1.21.3 |
| Fabric Loom | 1.17.20 |
| owo-lib | 0.12.18+1.21.2 |
| Java | 22 (target release 21) |
| OS | Windows |

## Build

| Step | Result |
|------|--------|
| `gradlew.bat build` | PASS |
| Java/client compilation | PASS |
| Unit tests | 263 tests, 0 failures, 0 errors, 1 skipped |
| JAR | `chat-canvas-1.3.0.jar` (28,217,728 bytes) |
| Sources JAR | `chat-canvas-1.3.0-sources.jar` (1,862,782 bytes) |

The final JAR contains `sherpa-onnx-jvm-1.13.4.jar` and `vosk-0.3.45.jar` as
nested jars. Speech models and platform-native runtime libraries remain runtime
downloads; no large model is bundled.

## Runtime

`gradlew.bat runClient` completed successfully. The client loaded Minecraft
1.21.3, initialized Chat Canvas 1.3.0, reloaded Chat Canvas resources, detected
Windows x86-64 voice capability, initialized OpenAL, loaded an integrated world,
and logged a normal chat message. No `MixinApplyError`, `InvalidInjectionException`,
`NoSuchMethodError`, or Chat Canvas resource/native initialization error occurred.

The log also contains non-blocking environment warnings: optional absent classes
from other integrations, a missing vanilla spawner sound, and a Mojang session
server connection reset while looking up profile properties.

## Automated coverage

- Chat component interaction preservation test: PASS
- Emoji registry, recent entries, and font evaluation paths: PASS
- Chat history, input modes, command tools, layout, UI metrics: PASS
- Voice key edge handling, VAD/endpoint detection, session state machine,
  text transaction, model registry, SHA-256 artifact validation, and fallback
  backends: PASS
- Runtime model registry: Vosk Chinese, streaming Zipformer Chinese,
  SenseVoice INT8, Whisper Tiny INT8 (4 models): PASS

## Manual / hardware coverage

Not manually exercised in this headless agent session:

- ClickEvent RUN_COMMAND / SUGGEST_COMMAND / OPEN_URL / COPY_TO_CLIPBOARD and
  HoverEvent interaction in a live server message
- Command suggestion dropdown and tab completion
- Opening every Settings control, scroll/resize behavior, and GUI Scale 2/3/4/Auto
- Live Emoji picker selection and tooltip interaction
- Physical microphone capture, VAD endpoint, final ASR insertion, model download,
  and native runtime loading
- Android/FCL/Pojav or iOS hardware/runtime paths; code paths are present and
  unit-tested where applicable but are not real-device tested here
