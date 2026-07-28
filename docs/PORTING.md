# Porting Guide

How to port Chat Canvas to newer Minecraft versions.

## Current Baseline (1.2.0)

| Item | 1.21.1 | 1.21.2 |
|------|--------|--------|
| Minecraft | 1.21.1 | 1.21.2 |
| Fabric Loader | 0.19.3 | 0.16.7 |
| Fabric API | 0.116.14+1.21.1 | 0.105.4+1.21.2 |
| Yarn mappings | 1.21.1+build.3 | 1.21.2+build.1 |
| Fabric Loom | 1.17-SNAPSHOT | 1.14.10 |
| owo-lib | 0.12.15.4+1.21 | 0.12.18+1.21.2 |
| Java | 21 | 21 |
| Vosk Java | 0.3.45 | 0.3.45 |

## High-Risk Porting Areas

These code areas are sensitive to Minecraft version changes and must be
carefully reviewed for each target version:

- **ChatScreenMixin** — Core chat input handling. Relies on `ChatScreen` field
  layout, `keyPressed` method signature, and `render` call order.
- **ChatHudMixin** — Chat HUD rendering pipeline. Depends on `ChatHud` internal
  message storage and render method signatures.
- **KeyboardMixin** — GLFW keyboard event listener. Targets
  `Keyboard.onKey(long window, int key, int scancode, int action, int modifiers)`.
- **Message ingress** — `ClientPlayNetworkHandlerMixin` or equivalent. Handles
  chat message classification on the client side.
- **CommandSuggestor** — Vanilla command suggestion UI. Method signatures and
  behaviour may shift.
- **TextRenderer / DrawContext** — Text layout and rendering. Font metrics,
  `draw()` signatures, and matrix transforms are common breakage points.
- **Screen lifecycle** — `init`, `removed`, `resize` method signatures.
- **Resource reload** — Font and resource reload listeners.
- **Client connection events** — Fabric lifecycle callbacks may change API surface.
- **KeyBinding registration** — Fabric Key Binding API registration order and
  API shape.
- **owo-ui API** — owo-lib's config screen components may change across versions.

## Mixin Target Checklist

| Mixin | Target Class | Key Methods | Purpose |
|-------|-------------|-------------|---------|
| ChatScreenMixin | `ChatScreen` | `init`, `keyPressed`, `render`, `removed`, `resize`, `mouseClicked`, `mouseScrolled`, `insertText` | Independent player/command input fields |
| ChatHudMixin | `ChatHud` | `render`, `addMessage` | Dual-channel chat rendering |
| KeyboardMixin | `Keyboard` | `onKey` | Voice key release via GLFW events |
| ClientPlayNetworkHandlerMixin | `ClientPlayNetworkHandler` | `onGameMessage` | Message ingress and classification |
| ChatInputSuggestorMixin | `ChatInputSuggestor` | Various | Suggestion window integration |
| TextFieldWidgetMixin | `TextFieldWidget` | Various | Input field extensions |
| TextRendererDrawerMixin | `TextRenderer$Drawer` | Various | Font rendering hooks |
| AbstractParentElementMixin | `AbstractParentElement` | Focus management | Focus routing |

### Lessons from 1.21.1

- **Do not inject `keyReleased` into `ChatScreen`**. In Minecraft 1.21.1,
  `ChatScreen` inherits `keyReleased` from `Screen` but does not declare it.
  Mixin `@Inject` requires the target class to *declare* the method. Use
  `Keyboard.onKey` instead.
- **Mixin compilation success ≠ runtime success**. `./gradlew build` only
  verifies Java compilation — Mixin target resolution happens at game launch.
  Always run `./gradlew runClient` and verify the chat screen opens.
- **Lease/closing patterns must be thread-safe**. The voice pipeline involves
  render thread, capture thread, and recognition thread all potentially
  closing the same microphone resource.

## Version-Independent Modules

These modules should port with minimal or no changes:

- `chat/emoji/EmojiRegistry`, `EmojiEntry`, `EmojiCategory`
- `chat/text/UnicodeTextNavigator`, `SpacedAdvanceMath`
- `chat/history/ChatLogWriter`, `ChatLogJson`, `StoredChatMessage`
- `voice/VoiceInputState`, `VoiceSettings`, `VoiceTextSanitizer`
- `voice/VoskResultParser`, `VoskEncodingBootstrap`
- `voice/VoiceModelDownloadManager`, `VoskModelManager` (except encoding bootstrap)
- `chat/command/SensitiveCommandDetector`, `SensitiveCommandMasker`
- Config data models under `config/`

## Version-Dependent Modules

These require the most attention during porting:

- All `mixin/client/*` classes
- `chat/render/*` (ChatHud interaction)
- `chat/message/ChatCanvasMessageIngress` (network handler integration)
- `chat/layout/ChatLayoutRuntime` (screen dimension access)
- `chat/input/ChatCanvasInputScreenBridge` (ChatScreen integration)
- `chat/text/GlyphAdvanceCache`, `SpacedTextRenderer` (font metrics)
- `ChatCanvasClient` (Fabric lifecycle callbacks, KeyBinding registration)
- `voice/VoiceInputManager.onClient()` (MinecraftClient.execute)

## Recommended Porting Workflow

1. Create a target version branch: `git switch -c mc/1.21.x` from `mc/1.21.1`
2. Update `gradle.properties`: `minecraft_version`, `yarn_mappings`,
   `loader_version`, `fabric_api_version`, `owo_version`, `loom_version`
3. Run `./gradlew compileJava compileClientJava` and fix compilation errors
4. Run `./gradlew runClient` and verify the game reaches the main menu
5. Enter a world and test chat, commands, emoji, voice, and chat log
6. Verify compatibility mods (Chat Heads, More Chat History, etc.)
7. Run `./gradlew test` and `./gradlew build`
8. Test the final JAR in a clean instance

## Branch Strategy

- `main` — Current development (currently 1.21.1)
- `mc/1.21.1` — Maintenance branch for 1.21.1 fixes only
- `mc/1.21.2` — Maintenance branch for 1.21.2. Ported from v1.2.0 with no code changes needed (Mixin targets identical between 1.21.1 and 1.21.2).
- Future: `mc/1.21.4`, `mc/1.21.5`, etc.

> Each maintenance branch is created from its release tag and receives only
> targeted fixes for that Minecraft version.
