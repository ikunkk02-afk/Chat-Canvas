# Test Report: Chat Canvas 1.3.0 for Minecraft 1.21.8 (Fabric)

## Environment

| Item | Value |
|------|-------|
| Minecraft | 1.21.8 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.136.1+1.21.8 |
| Yarn mappings | 1.21.8+build.1 |
| Fabric Loom | 1.17.17 |
| owo-lib | 0.12.23+1.21.8 |
| ModMenu | 15.0.2 |
| Java | 21 |
| Gradle | 9.5.1 |
| OS | Windows |

## Unit Tests

- **Result**: ✅ PASSED
- **Gradle task**: `gradlew.bat test` → BUILD SUCCESSFUL
- **Executed**: 262 tests, 0 failures, 1 skipped
- **Coverage areas**: chat component interaction, styled text wrapping, UI layout, emoji, voice state/VAD/model registry, native capability detection, and compatibility helpers

## Build

- **Result**: ✅ PASSED — `gradlew.bat build` → BUILD SUCCESSFUL
- **Output**: `build/libs/chat-canvas-1.3.0.jar` (28,215,649 bytes)
- **Sources jar**: `build/libs/chat-canvas-1.3.0-sources.jar` (1,862,821 bytes)
- **Compilation**: Java main and client source sets compile successfully
- **Target configuration**: Minecraft/Fabric/Loom versions remain the 1.21.8 template values

## JAR Contents

Verified in the generated 1.3.0 artifact:

| Expected | Status |
|----------|--------|
| `fabric.mod.json` | ✅ Present; mod version 1.3.0 and Minecraft dependency 1.21.8 |
| `chat_canvas.client.mixins.json` | ✅ Present |
| `META-INF/jars/sherpa-onnx-jvm-1.13.4.jar` | ✅ Bundled |
| `META-INF/jars/vosk-0.3.45.jar` | ✅ Bundled |
| `THIRD_PARTY_LICENSES.md` | ✅ Present |
| `en_us.json` / `zh_cn.json` / `zh_tw.json` | ✅ Present; 500 keys each |
| Emoji resources and metadata | ✅ Present |
| Offline speech models | ✅ Runtime-download only; not bundled in the mod JAR |

## runClient

- **Status**: ✅ Started successfully and reached a playable integrated-server world
- **Observed**: Fabric Loader, Chat Canvas 1.3.0, resource reload, OpenAL initialization, emoji font evaluation, chat rendering, and player join all completed
- **Chat Canvas Mixin/resource/native errors**: None observed
- **Non-blocking environment warnings**: Mojang authentication/public-key network timeouts and optional compatibility-class probes

## SHA-256

```
2c727f64ad9f6d7f4ff021561b83e3c0c672a78e3ef6f7b2006f9fd6ee996d02  build/libs/chat-canvas-1.3.0.jar
```

## Manual Feature Testing

- **Verified by automated tests/runtime**: chat rendering path, command/chat input code paths, emoji registry/resources, language loading, voice platform initialization, model registry, and Mixin application
- **Not manually verified in this run**: clickable/hoverable server message interaction, command suggestion UI, settings/sidebar controls, GUI scales 2/3/4/Auto, and an end-to-end microphone V → VAD endpoint → final ASR insertion flow
- **Android/iOS**: code paths and capability/fallback logic are included and build-checked; no Android or iOS device test was available

## Compatibility Mods

Chat Heads, More Chat History, ChatAnimation, and Smooth Scrolling were not separately tested against this build.

## Known Limitations

1. End-to-end voice input still requires a real microphone and an installed/downloaded model.
2. Android/iOS native and microphone behavior has not been verified on physical devices.
3. Compatibility mods were not separately loaded in the development client.
4. `ChatHud.render` draw-operation wrapping remains disabled for the 1.21.6+ lambda structure; the working HEAD injection delegates to `DualChatHudRenderer`.

## Mixin Audit

- **Total Mixins**: 15 (8 injectors + 7 accessors)
- **Target-specific adaptation**: `TextFieldWidgetMixin` uses `DrawContext.drawSelection()` because the 1.21.8 target no longer exposes the old private selection-highlight method
- **Runtime status**: All listed client mixins applied during `runClient`
