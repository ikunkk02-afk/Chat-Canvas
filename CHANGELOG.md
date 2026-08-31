# Changelog

All notable changes to Chat Canvas will be documented in this file.

## [1.3.0] - 2026-08-31

### Added

- **Reworked offline voice input**: multiple ASR backends powered by sherpa-onnx 1.13.4 — streaming Zipformer Chinese, SenseVoice INT8 multilingual (Mandarin, Cantonese, English, Japanese, Korean) and Whisper Tiny INT8 multilingual — alongside the existing Vosk small Chinese model.
- **Silero VAD endpoint detection**: automatic speech/silence endpoints with configurable listen timeout, endpoint silence and tail padding.
- **Model manager**: model selection, download progress, SHA-256 verification, cancel, release, hot-swap switching between installed models, and one-click access to the model folder.
- **Android / iOS compatibility layer**: platform detection (FCL / Pojav-class launchers), ARM64/ARM32 native runtimes, staged loading into app-private cache, and capability checks that safely disable voice input when a launcher cannot provide a microphone or native runtime.
- **Expanded emoji picker**: 130+ emoji in 10 categories with a virtualized grid, tooltips, keyboard navigation, multi-language names, and font-based filtering of unsupported glyphs.
- **Traditional Chinese (zh_tw)** localization.
- **Improved localization**: refreshed English and Simplified Chinese strings across the editor, voice, emoji and chat log pages.

### Changed

- **Responsive settings UI**: editor panels, tabs and chat overlays were restyled and stabilized across GUI scales and aspect ratios.
- **Server interactive-message support**: vanilla `ClickEvent` / `HoverEvent` interactions are preserved through Chat Canvas text wrapping (run/suggest command, open URL, copy to clipboard, hover tooltips).
- Removed input mode labels and unified player/command chat input handling.

### Fixed

- Voice model runtime and control stability (load failures, model switching, keyboard shortcut edge cases).

### Compatibility

- Chat Heads, More Chat History, ChatAnimation, Smooth Scrolling.

### Privacy

- Speech recognition runs entirely locally with an installed offline model.
- Audio is never uploaded or persisted; networking is used only for player-requested downloads of models and the sherpa-onnx native runtime.

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

- **Voice key release Mixin crash**: Removed invalid `@Inject(method = "keyReleased")` from `ChatScreenMixin` — `ChatScreen` does not declare this method in Minecraft 1.21.5. Replaced with `KeyboardMixin` listening on GLFW `Keyboard.onKey` for a true GLFW_RELEASE event.
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
- Tested in a large Fabric 1.21.5 modpack environment.
