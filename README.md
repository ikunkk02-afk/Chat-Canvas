[English](README.md) | [简体中文](README_zh_CN.md)

<p align="center">
  <img src="src/main/resources/assets/chat_canvas/icon.png" width="180" alt="Chat Canvas">
</p>

# Chat Canvas

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.7-green)
![Loader](https://img.shields.io/badge/Loader-Fabric-lightyellow)
![Side](https://img.shields.io/badge/Side-Client--only-blue)
![Java](https://img.shields.io/badge/Java-21%2B-orange)
![Version](https://img.shields.io/badge/Version-1.3.0-informational)
![License](https://img.shields.io/badge/License-MIT-brightgreen)

Chat Canvas is a client-side chat enhancement and redesign mod for **Minecraft 1.21.7 (Fabric)**. It focuses on chat reading, chat input and interface layout, and adds Emoji, offline voice input, local chat history and reliable support for interactive server messages.

Everything is configured in a visual editor directly in the game — no config-file editing required.

## What's New in 1.3.0

- Reworked offline voice input with multiple ASR models
- VAD automatic endpoint detection (speech starts and ends on its own)
- Model manager with SHA-256 verified downloads and hot-swap switching
- Android / iOS compatibility layer for the voice system
- Expanded Emoji picker (130+ emoji, 10 categories)
- Traditional Chinese (繁體中文) support
- Improved server interactive-message support (ClickEvent / HoverEvent)
- Restyled and responsive settings UI

## Features

### Chat Interface

- Visual chat editor with real-time in-game preview
- Drag to reposition and eight-direction resize of the chat HUD, with edge and centre snapping
- **Two selectable editor visual themes**: Chat Canvas (modern) and Minecraft-style — identical layout, features and configuration
- **Dual chat channels**: player chat and command & system messages, each with independent position, size, style and scrolling
- **Split alignment layout**: classic or left/right column layout, with own and other players' messages visually separated and an adjustable message width
- Player avatars via [Chat Heads](https://modrinth.com/mod/chat-heads) (not bundled)
- Text size, line spacing, character spacing, opacity, shadow and alignment
- Message background colour, opacity, display mode and padding; input box background and border
- Undo / redo while editing (Ctrl+Z / Ctrl+Y)

### Player Colours, Mentions and Quick Actions

- Stable automatic colours assigned by player UUID, with a custom palette and manual per-player overrides
- Option to keep the server's original name colours
- Double-click a player name to insert `@name` at the cursor; mention highlighting with custom colour and bold
- Mention notifications: sound, toast, or full-screen flash (own mentions ignored by default)
- Right-click a player name for quick actions: mention, private message template (`/msg`, `/tell`, `/w`), or copy name
- All quick actions only fill the input box — nothing is sent automatically

## Interactive Server Messages

Chat Canvas preserves vanilla component interactions in chat messages, including:

- `ClickEvent`: run command, suggest command, open URL, copy to clipboard
- `HoverEvent`: tooltips

This means server features such as `/tpa` accept/deny buttons, clickable links and copyable text keep working inside the redesigned chat. The exact behaviour depends on what the server sends using vanilla components.

## Voice Input

Press **V** to open chat and start voice input, and press **V** again to finish. Speech-to-text runs fully offline after a model is installed.

- Hold-free recording: start with V, stop with V, or let VAD end the sentence automatically
- Silero VAD with configurable listen timeout, endpoint silence and tail padding
- Partial results are shown live while speaking; the final result is inserted at the cursor
- Results are never sent automatically — review before pressing Enter
- Microphone device selection and level testing
- Optional automatic final punctuation and character limit handling
- Maximum recording time, noise threshold and ASR thread settings

## Offline Speech Models

Chat Canvas does not bundle speech models in the mod JAR. On first use, the model manager lets you pick one of the following models (each downloaded only after your confirmation):

| Model | Languages | Size | Profile |
|---|---|---|---|
| Streaming Zipformer Chinese | Mandarin | ~30 MB | Low resource, streaming, very fast — suitable for low-end devices and mobile |
| SenseVoice INT8 | Mandarin, Cantonese, English, Japanese, Korean | ~240 MB | Balanced; higher quality, fast response, offline decoding |
| Whisper Tiny INT8 | Multilingual | ~104 MB | Offline decoding, broad multilingual coverage |
| Vosk Small Chinese (legacy) | Mandarin | ~44 MB | Streaming; compatibility option, desktop x86-64 only |

Zipformer and Vosk stream partial results while you speak; SenseVoice and Whisper decode after VAD detects the end of a sentence.

## Model Manager

- Pick a model from the voice settings page; download progress and sizes are shown
- All downloads are verified against SHA-256 checksums
- Switching between installed models is hot-swapped — no game restart needed
- Cancel downloads, release a loaded model, or open the model folder from the UI

## Mobile Support

The voice system includes a compatibility layer for mobile Minecraft launchers:

- **Android** (FCL / Pojav-class Java Minecraft environments): ARM64 and ARM32 native runtimes are downloaded and staged into the app-private cache.
- **iOS arm64**: the sherpa-onnx native runtime must be supplied by the launcher through the runtime bridge.

Actual microphone access and native library loading depend on what the launcher provides (microphone permission, native runtime support). Chat Canvas detects capabilities at runtime and safely disables voice input when the environment cannot support it — the rest of the mod keeps working.

## Emoji Picker

- 130+ emoji in 10 categories (Smileys, People, Animals, Food, Activities, Travel, Objects, Symbols, Hearts, Recently Used)
- Search, recently used history (persisted), and multi-language emoji names
- Virtualized scrollable grid with tooltips and keyboard navigation
- Emoji that the active font cannot render are hidden automatically

## Chat History

- Player chat messages are saved locally to `.minecraft/chatcanvas/chat-logs/`, isolated per world/server
- UTF-8 JSON Lines format with daily and size-based rotation, written asynchronously
- Separately toggle saving of your own messages, other players' messages and the command/system channel
- Configurable retention days (0 = keep forever) and one-click access to the log folder

Chat logs are plain local files; there is no cloud sync and no in-game history viewer.

## Command Input

- Typing `/` switches the input to command mode with vanilla (Brigadier-based) command suggestions
- Command tools panel (Ctrl+F in command mode): recently executed commands, favourites with drag ordering, common presets, and the system clipboard
- Search, edit, delete and categorise saved commands
- Insert at cursor or replace the input; sensitive commands (e.g. `/login`, `/password`) are flagged and excluded from logging
- Commands are only ever inserted into the input box — press Enter yourself to run them

## Languages

- English (en_us)
- 简体中文 (zh_cn)
- 繁體中文（台灣） (zh_tw)

## Responsive UI

The editor adapts to Minecraft's logical GUI size, so it works across GUI scales, window sizes, common aspect ratios (16:9, 16:10, 4:3) and windowed/fullscreen modes. The settings panel also moves automatically to avoid overlapping the chat preview.

## Controls

| Key | Action |
| --- | --- |
| K | Open/close the Chat Canvas editor |
| V | Start / finish voice input |
| T | Open chat (vanilla) |
| / | Command input (vanilla) |
| Ctrl+E | Toggle the Emoji picker |
| Ctrl+F | Command tools / Emoji search |
| Ctrl+Z / Ctrl+Y | Undo / redo in the editor |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.7
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) and [owo-lib](https://modrinth.com/mod/owo-lib)
3. Download the Chat Canvas JAR from [Releases](https://github.com/ikunkk02-afk/Chat-Canvas/releases) and put it in `.minecraft/mods/`
4. Launch the game (Java 21 or higher)
5. Optional: install [Mod Menu](https://modrinth.com/mod/modmenu) for an in-game configuration entry point

## Requirements

| Dependency | Type | Version |
|---|---|---|
| Minecraft | Required | 1.21.7 |
| Java | Required | 21 or higher |
| Fabric Loader | Required | 0.19.3 or higher |
| Fabric API | Required | 0.129.0+1.21.7 or compatible |
| owo-lib | Required | 0.12.21+1.21.6 or compatible |
| Mod Menu | Optional | 11.0.4 or compatible |

## Voice Model Storage

Speech models are downloaded at runtime and are never included in the mod JAR.

- Models: `config/chatcanvas/voice-models/`
- sherpa-onnx native runtime and Silero VAD: `config/chatcanvas/voice-runtime/sherpa-onnx/<version>/`

Delete these folders to reclaim disk space; the model manager will offer to re-download when needed.

## Privacy

- With an installed offline model, **speech recognition runs locally**. The microphone and decoding are active only during a voice session; audio is never uploaded or saved.
- Networking is used only for player-requested downloads (models and the native runtime); these require network on first voice setup.
- Chat logs and saved commands are stored as local plain-text JSON. Do not save passwords or tokens (e.g. `/login`, `/register`, `/password`) on shared computers.

## Compatibility

Chat Canvas is **client-side only**. It does not need to be installed on the server and works with vanilla, Fabric, Paper or Spigot servers. It does not bypass server permissions — commands are executed by the server as usual.

Known-good coexistence with other chat mods:

- [Chat Heads](https://modrinth.com/mod/chat-heads) — avatars are rendered inside the Chat Canvas layout
- [More Chat History](https://modrinth.com/mod/morechathistory) — history capacity is left to More Chat History
- [ChatAnimation](https://modrinth.com/mod/chatanimation) — message entry animations are left to ChatAnimation
- [Smooth Scrolling](https://modrinth.com/mod/smooth-scroll) — scroll animation is left to Smooth Scrolling

## Known Limitations

- Fabric 1.21.7 only; no Forge or NeoForge build.
- On iOS, voice input requires the launcher to provide a signed sherpa-onnx runtime bridge; on Android it depends on the launcher exposing a usable microphone and allowing native library loading. If unavailable, voice input is disabled safely and the rest of the mod still works.
- The legacy Vosk model only works on desktop x86-64.
- If a server plugin rewrites player messages into system messages, the sender UUID may be lost; player colours and name quick actions then cannot be applied reliably.
- Emoji availability depends on the active font; unsupported glyphs are hidden automatically.
- Chat logs are plain local files — there is no built-in viewer and no synchronization between devices.

## Configuration

Press **K** in-game (or open Chat Canvas from Mod Menu) to access the editor with eight settings tabs:

- Layout — position, size, channel and split-layout options
- Text — size, spacing, opacity, shadow, alignment
- Background — message and input box backgrounds
- Player Colours — automatic colours, palette and overrides
- Mentions — double-click behaviour, highlighting and notifications
- Commands — command tools and saved commands
- Voice — voice input, model manager and microphone settings
- Chat Log — local chat history options

## Screenshots

Screenshots for 1.3.0 will be added with the release assets.

## Credits

- [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx) — offline ASR runtime and Silero VAD model
- [Vosk](https://alphacephei.com/vosk/) — legacy small Chinese speech model
- Speech models distributed via [Hugging Face](https://huggingface.co/csukuangfj) (downloaded at runtime)
- [Fabric API](https://github.com/FabricMC/fabric), [owo-lib](https://github.com/wisp-forest/owo-lib), [Mod Menu](https://github.com/TerraformersMC/ModMenu)

See [THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md) for dependency licenses.

## License

[MIT](LICENSE) — Copyright &copy; 2026 寿云
