# Changelog

All notable changes to Chat Canvas will be documented in this file.

## [1.2.0-mc1.21.11] - 2026-07-29

### Ported

- Ported Chat Canvas 1.2.0 to Minecraft 1.21.11 (Fabric).
- Source baseline: `mc/1.21.9` branch.

### Changed

- Updated `ChatHudMixin.getTextStyleAt` injection: removed `@Shadow` for `toChatLineX`, `toChatLineY`, `getMessageLineIndex` (removed in MC 1.21.11 ChatHud refactoring).
- Replaced coordinate conversion with `ChatLayoutRuntime.currentTransform()` and manual line index calculation.
- Added `chat_canvas$styleAtPixel` helper to replace removed `TextHandler.getStyleAt(OrderedText, int)`.
- Fixed `DrawContext.drawSelection` call: added missing `boolean invert` parameter (new in 1.21.11).
- Fixed `PositionedSoundInstance.master()` → `ui()` (renamed in 1.21.11).
- Fixed `ChatCanvasEditorScreen.resize` signature: `resize(int, int)` instead of `resize(MinecraftClient, int, int)`.
- **Migrated owo-lib from 0.12.x → 0.13.0**: `BaseComponent` → `BaseUIComponent`, `OwoUIDrawContext` → `OwoUIGraphics`, `Containers` → `UIContainers`, `Components` → `UIComponents`, `Component` → `UIComponent`, `ParentComponent` → `ParentUIComponent` (16 files).

### Dependencies

| Dependency | Version |
|-----------|---------|
| Fabric Loader | 0.19.3 |
| Fabric API | 0.141.6+1.21.11 |
| Yarn mappings | 1.21.11+build.6 |
| owo-lib | 0.13.0+1.21.11 |
| Mod Menu | 17.0.0 |
| Java | 21 |

## [1.2.0-mc1.21.9] - 2026-07-29

### Ported

- Ported Chat Canvas 1.2.0 to Minecraft 1.21.9 (Fabric).
- Source baseline: `mc/1.21.8` branch.

### Changed

- Updated all Mixin targets for MC 1.21.9 input API refactoring: `KeyInput`, `CharInput`, and `Click` wrapper types replace primitive parameters.
- `Keyboard.onKey` signature changed from `(long, int, int, int, int)` → `(long, int, KeyInput)`, visibility `public` → `private`.
- `Screen`/`Element`/`ParentElement` input methods refactored: `keyPressed(KeyInput)`, `charTyped(CharInput)`, `mouseClicked(Click, boolean)`, `mouseReleased(Click)`, `mouseDragged(Click, double, double)`.
- `Screen.hasShiftDown()`/`hasControlDown()` → `MinecraftClient.getInstance().isShiftPressed()`/`isCtrlPressed()`.
- `Screen.isSelectAll()`/`isPaste()`/`isCopy()` → `KeyInput.isSelectAll()`/`isPaste()`/`isCopy()`.
- `Style.DEFAULT_FONT_ID` → `MinecraftClient.DEFAULT_FONT_ID`.
- `GameProfile.getName()`/`getId()` → `name()`/`id()` (record accessors in authlib).
- `DrawContext.drawBorder()` replaced with fill-based helper (removed from API).
- Updated dependencies: Fabric API 0.134.1+1.21.9, owo-lib 0.12.24+1.21.9, ModMenu 16.0.1, Fabric Loom 1.17.17.

## [1.2.0-mc1.21.8] - 2026-07-29

### Ported

- Ported Chat Canvas 1.2.0 to Minecraft 1.21.8 (Fabric).
- Source baseline: `mc/1.21.6` branch.

### Changed

- Replaced `TextFieldWidget.drawSelectionHighlight` shadow with `DrawContext.drawSelection()` (removed in MC 1.21.7).
- Updated dependencies: Fabric API 0.136.1+1.21.8, owo-lib 0.12.23+1.21.8, ModMenu 15.0.2, Fabric Loom 1.17.17.

## [1.2.0] - 2026-07-27

### Added

- **Local chat log saving**: automatically save player chat messages to `.minecraft/chatcanvas/chat-logs/` in UTF-8 JSONL format, isolated per world/server.
- Chat log settings page in editor (enable/disable, toggle self/others, command system channel, retention days, max file size, open directory).
- Asynchronous single-threaded log writer with bounded queue, daily file rotation, and size-based rotation.
- Chat log retention manager: automatically deletes log files older than configured days.
- Sensitive command exclusion for chat logs (reuses existing `SensitiveCommandDetector`).
- Session context switching (JOIN/DISCONNECT) for correct per-world/server log directories.
- Stable directory hashing (SHA-256) with sanitised display names to prevent path traversal.

### Changed

- Updated `README.md` and `README_EN.md` for all v1.2.0 features: dual channels, emoji, voice input, command tools, chat log, split layout, updated category count and data file paths.
- Bumped version to 1.2.0.

### Fixed

- **Voice key release Mixin crash**: Removed invalid `@Inject(method = "keyReleased")` from `ChatScreenMixin` — `ChatScreen` does not declare this method in Minecraft 1.21.1. Replaced with `KeyboardMixin` listening on GLFW `Keyboard.onKey` for a true GLFW_RELEASE event.
- **Microphone Lease concurrency** NPE (`this.opened is null`): Rewrote `MicrophoneManager.Lease.close()` using `AtomicReference<OpenedMicrophone>` with atomic `getAndSet(null)` to guarantee exactly-once close across concurrent capture, finish, cancel, and recognition threads.
- **Voice session cleanup**: Unified `VoiceInputSession` cleanup in a single `finally` block; made `finish()`/`cancel()` use CAS for safe one-shot signalling; made END marker enqueue idempotent via `endEnqueued` guard.
- **Error classification**: `IllegalStateException` from an already-closed lease no longer shows the misleading "microphone access" error.
- Voice input crash: replaced `ThreadPoolExecutor.AbortPolicy` with `DiscardOldestPolicy` to prevent `RejectedExecutionException` when microphone test or previous session occupied the capture thread.
- Keyboard voice recognition slowness: key repeat no longer overwrites `keyboardHolding` flag, so `finish()` is correctly called on key release instead of recording until timeout.
- Keyboard voice unresponsiveness when previous session was still in RECOGNIZING state: now cancels stale session before starting a new one on V press.

### Compatibility

- Chat Heads
- More Chat History
- ChatAnimation
- Smooth Scrolling

### Privacy

- Voice recognition runs entirely offline on the local machine.
- Audio is never uploaded or persisted to disk.
- Local chat log saving can be disabled per category in the editor.

## [1.1.1] - 2026-07-24

### Fixed

- Fixed incorrect right/bottom coordinates in the Minecraft-style selection indicator renderer causing a large opaque gray rectangle across the editor preview when the settings panel was positioned on the right side.
- Fixed Minecraft-style category, alignment and background-mode selection indicators drawing outside their parent controls.
- Added `ModernUiTheme.transparentButton()` for hit-target buttons that should never draw a solid background in either theme.

## [1.1.0] - 2026-07-24

### Added

- Selectable Chat Canvas and Minecraft-style editor visual themes.
- Runtime theme switching without losing unsaved settings, undo history or current category.
- Persistent editor theme preference across game restarts.
- Theme-aware controls: labels, panels, tabs, numeric scrubbers, text fields and footer.
- Dedicated theme selection button in the editor header.

### Changed

- Reworked the Minecraft-style interface from a separate screen into a **visual theme** of the main Chat Canvas editor. Both themes share the same layout, preview, controls, EditorSession and EditorHistory.
- Updated the editor header layout to accommodate the theme button (toolbar widened to 460 px).
- Completed Minecraft-style visuals for setting labels, numeric scrubbers, selection indicators and the footer.
- Rewrote Chinese (`README.md`) and English (`README_EN.md`) documentation for v1.1.0.

### Fixed

- Theme button text overlapping the Undo button.
- Incomplete Minecraft-style theme colours leaving purple labels and blue accents visible in the vanilla theme.
- Numeric scrubber controls retaining modern-theme visuals (blue progress bar, dark panel) in the Minecraft-style theme.
- Category tab selected state using modern-theme blue highlights in the vanilla theme.
- Command clipboard launcher button remaining visible and interactive while the clipboard panel was open.
- Invisible command launcher hitbox blocking panel input after the clipboard opened.
- Theme switching previously creating a separate full-screen editor layout rather than swapping visual skins.

## [1.0.0] - 2026-07-24

### Added

- Visual chat HUD editor with live game viewport preview.
- Drag-to-move and eight-direction resizing with snap-to-edge alignment guides.
- Settings panel with automatic left/right side avoidance.
- Six-category settings page system with horizontal tab switching.
- Text size, line spacing, character spacing, text opacity, and shadow toggle.
- Left, centre, and right text alignment.
- Message background with colours, opacity, and three display modes (follow text, full width, hidden).
- Horizontal and vertical padding for message backgrounds.
- Input field background colours, opacity, border colours, and border visibility.
- Player name colour engine: automatic UUID-based colour assignment with a customisable palette.
- Per-player manual colour override with online-player search.
- Double-click player name to insert `@mention`.
- Mention text highlighting with customisable colour, bold toggle, and `@`‑prefix requirement mode.
- Mention notification system: configurable sound alerts, Toast popups, and full‑screen coloured flash.
- Right-click player name quick-actions menu: mention, whisper, and copy name.
- Persistent command clipboard with save, search, category filter, favourites, reorder, edit, and delete.
- Built-in command presets.
- `Ctrl+Z` / `Ctrl+Y` undo and redo support.
- Compatibility with Chat Heads, More Chat History, ChatAnimation, and Smooth Scrolling.
- Mod Menu integration.
- Tested in a large Fabric 1.21.1 modpack environment.
