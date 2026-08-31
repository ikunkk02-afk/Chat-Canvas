# Test Report: Chat Canvas 1.3.0 for Minecraft 1.21.7

## Environment

| Item | Value |
|------|-------|
| Date | 2026-08-31 |
| Minecraft | 1.21.7 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.129.0+1.21.7 |
| owo-lib | 0.12.21+1.21.6 |
| Yarn mappings | 1.21.7+build.8 |
| Java | 22 runtime; Java 21 bytecode target |
| OS | Windows |

## Build Results

| Step | Status | Notes |
|------|--------|-------|
| `gradlew.bat compileJava compileClientJava` | PASS | Target API adaptations compile |
| `gradlew.bat test` | PASS | 263 tests, 0 failures, 0 errors, 1 skipped |
| `gradlew.bat build` | PASS | Remapped mod JAR produced |
| `gradlew.bat runClient` | PASS | Reached a 1.21.7 single-player world; stopped manually after startup verification |

## Mixin Runtime Verification

All 16 entries in `chat_canvas.client.mixins.json` loaded during `runClient`; no
Chat Canvas Mixin apply error was emitted. The vanilla `ChatHud.render` lambda
refactor is handled by the active `DualChatHudRenderer`; the four old render
wrappers were removed because their 1.21.1 injection points no longer exist in
1.21.7.

## Automated Functional Coverage

- Interactive component/style preservation, including run/suggest command values: PASS
- Emoji registry and resource localization: PASS
- UI layout metrics and responsive sizing: PASS
- Voice state machine, VAD endpoint logic, model registry, runtime fallback and text transaction: PASS
- Chat history, command storage and configuration serialization: PASS
- OpenAL/JavaSound capability paths and Android staging safety paths: PASS

## Runtime / Manual Coverage

- Fabric startup, Chat Canvas initialization, resource reload, OpenAL initialization and single-player world entry: PASS
- Main menu, chat screen, command suggestions, ClickEvent/HoverEvent mouse interaction, Emoji picker, settings controls, GUI Scale 2/3/4/Auto: NOT manually exercised in this run
- Voice V key flow and final ASR result with a real microphone: NOT tested; hardware interaction was not performed
- Android/iOS physical launcher/device tests: NOT tested; code paths are compiled and covered by capability/fallback tests
- Chat Heads, More Chat History, ChatAnimation and Smooth Scrolling coexistence: NOT tested in this isolated development profile

## Observed External Warnings

The development client reported authentication/public-key network timeouts and
optional class-probe warnings from the dev environment. No Chat Canvas resource,
Native Runtime, renderer, or Mixin application error was reported.

## Known Limitations

1. The 1.21.7 `ChatHud.render` lambda refactor prevents the old fallback render wrappers from being used; when the custom renderer is active, `DualChatHudRenderer` supplies the complete rendering path.
2. Offline speech models and platform-native sherpa-onnx libraries remain runtime downloads and are not bundled in the mod JAR.
3. Real microphone recognition and Android/iOS launcher behavior still require device-level verification.
