# Test Report: Chat Canvas 1.3.0 for Minecraft 1.21.10

## Test Environment

| Item | Value |
|------|-------|
| Minecraft | 1.21.10 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.138.4+1.21.10 |
| Yarn mappings | 1.21.10+build.3 |
| Fabric Loom | 1.17.17 |
| owo-lib | 0.12.24+1.21.9 |
| ModMenu | 16.0.1 |
| Java | 22.0.2 development runtime; source target 21 |
| OS | Windows |

## Automated Checks

| Check | Result |
|-------|--------|
| `gradlew test` | PASS — 263 tests, 1 skipped |
| `gradlew compileJava compileClientJava` | PASS |
| `gradlew build` | PASS |
| Language key validation | PASS — `en_us`, `zh_cn`, `zh_tw`, 500 keys each |
| Final client remap | PASS |

## `runClient`

The development client started with Minecraft 1.21.10, Fabric Loader 0.19.3,
Chat Canvas 1.3.0, and the target dependencies. It completed Mixin
transformation, initialized Chat Canvas, reloaded resources, initialized
OpenAL, evaluated all 136 Emoji whitelist entries, and entered a generated
integrated world.

No Chat Canvas `MixinApplyError`, `InvalidInjectionException`,
`NoSuchMethodError`, or `NoSuchFieldError` remained in the final launch log.
The log does contain unrelated network/service warnings because the development
environment could not reach Mojang session/Realms services, plus optional-class
warnings from absent external mods.

## Final JAR

`build/libs/chat-canvas-1.3.0.jar` contains the target `fabric.mod.json`, all
three language files, the client Mixin configuration, the Emoji registry and
locale metadata, the third-party license file, and nested Sherpa/Vosk runtime
jars. The current 1.3.0 design has no bundled Emoji PNG/native `.dll`/`.so`/
`.dylib` assets: it renders Unicode Emoji and acquires ASR models and
platform-native runtime files at runtime. No large ASR model is bundled in the
mod JAR.

## Registered Voice Models

The migrated registry contains `sherpa-onnx-streaming-zipformer-zh-14m-2023-02-23`,
`sherpa-onnx-sense-voice-int8-2024-07-17` (default),
`sherpa-onnx-whisper-tiny-int8`, and the compatible `vosk-model-small-cn-0.22`.
Their URLs, revisions, SHA-256 values, language metadata, platform capability,
and hot-swap policy are kept in the target registry.

## Feature Coverage

The source-to-target implementation includes the 1.3.0 feature set: interactive
server chat components, click/hover actions and insertion preservation, removal
of persistent Chat/Command labels, the dark responsive UI, GUI-scale-aware
layout, Traditional Chinese localization, Emoji registry/picker/font support,
chat history, command input/suggestions, and the V-key voice state machine with
VAD, endpoint/finalization, model management, Sherpa/Vosk providers, and
Windows/Android/iOS capability paths.

## Not Hardware/Manual Verified

The following were not claimed as manual gameplay or device tests in this
environment: server ClickEvent/HoverEvent interaction, all GUI scales (2/3/4/
Auto), manual Emoji sending, command tab completion, full chat-history UX,
microphone capture/VAD/final ASR, model download, Android/iOS real-device
behavior, multiplayer servers, and compatibility mods. Android/iOS code paths
were ported and included in the successful build, but were not tested on those
devices.
