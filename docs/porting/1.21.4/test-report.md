# Test Report: Chat Canvas 1.3.0 for Minecraft 1.21.4

## Environment

| Item | Value |
|------|-------|
| Minecraft | 1.21.4 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.119.4+1.21.4 |
| Yarn mappings | 1.21.4+build.8 |
| owo-lib | 0.12.20+1.21.4 |
| Java compile target | 21 |
| Java runtime reported by Fabric | 22 |
| Gradle | 9.5.1 |
| Loom | 1.17.20 |
| OS | Windows |

## Automated Tests

Command: `gradlew.bat clean build --no-daemon`

- `BUILD SUCCESSFUL`
- 73 test suites, 263 tests
- 0 failures, 0 errors
- 1 skipped: `VoskAsrProviderIntegrationTest` requires a locally installed Vosk model

## Build

Command: `gradlew.bat clean build --no-daemon`

- `BUILD SUCCESSFUL`
- Output: `build/libs/chat-canvas-1.3.0.jar`
- SHA-256: `EEACEB7CD20FCA565DB27FE1FE22B08F57A2CF77D79BDDCA38A89A18F7F9DE0E`
- Includes nested `vosk-0.3.45.jar` and `sherpa-onnx-jvm-1.13.4.jar`
- Does not include ASR models, Silero VAD model, or platform native libraries

## Runtime

Command: `gradlew.bat runClient --no-daemon`

- `BUILD SUCCESSFUL`
- Minecraft 1.21.4 reached the main menu and entered an integrated singleplayer world
- Chat Canvas initialized successfully
- Resource reload completed
- OpenAL initialized
- Emoji font evaluation completed (`136/136` whitelist entries supported)
- World saved and client stopped normally
- No `MixinApplyError`, `InvalidInjectionException`, `NoSuchMethodError`, `NoSuchFieldError`, `AbstractMethodError`, or Chat Canvas resource error was logged

Known environment warnings:

- Mojang session profile lookup failed because the remote TLS handshake was terminated; this did not prevent local play
- Fabric logged optional class lookup warnings for unrelated absent classes; no Mixin application failure followed

## Jar Verification

- [x] `fabric.mod.json` present and expanded to Chat Canvas `1.3.0`
- [x] Minecraft dependency is `~1.21.4`
- [x] `chat_canvas.client.mixins.json` present
- [x] `en_us.json`, `zh_cn.json`, and `zh_tw.json` present
- [x] Emoji icon present
- [x] Vosk and sherpa-onnx nested JARs present
- [x] Third-party license file present
- [x] No model or native binary payload bundled
- [x] No test classes or local config/log files bundled

## Manual Verification Status

Runtime smoke evidence covers Chat Canvas initialization, resource loading, OpenAL initialization, Emoji font capability detection, world entry, a rendered local chat line, save, and clean shutdown.

The following were not manually clicked through in this automation run and remain pending interactive verification:

- ChatScreen editor interactions and command suggestions
- Server `ClickEvent` / `HoverEvent` actions, including `/tpa` accept/deny buttons
- Emoji picker grid, search, scroll, tooltip, and insertion
- Chat history scroll and persistence through re-entry
- Settings sidebar/header/footer, sliders, toggles, dropdowns, resize and scissor behavior
- Live language switching across English, Simplified Chinese, and Traditional Chinese
- GUI Scale 2, 3, 4, and Auto visual checks
- V-key voice start/stop, microphone capture, VAD endpoint, partial/final result insertion
- Actual ASR model downloads and model hot-reload UI

Voice initialization, model registry, VAD/state-machine, platform capability, Android/FCL fallback, iOS bridge fallback, and native runtime paths were covered by compilation/unit tests and the Windows capability log. No physical microphone, Android/FCL, or iOS device test was performed.

Compatibility mods and a real multiplayer server were not tested in this run.
