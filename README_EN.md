[简体中文](README.md) | [English](README_EN.md)

# Chat Canvas

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green)
![Mod Loader](https://img.shields.io/badge/Loader-Fabric-lightyellow)
![Environment](https://img.shields.io/badge/Environment-Client-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

**Chat Canvas** is a **client-side** chat HUD customization mod for Minecraft 1.21.1 Fabric. It provides a visual editor for moving, resizing and previewing the chat HUD in real time, together with text styling, background customization, per-player colors, mention notifications, player quick actions and a persistent command clipboard.

---

## Table of Contents

- [Highlights](#highlights)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Visual Editor](#visual-editor)
- [Text Settings](#text-settings)
- [Background Settings](#background-settings)
- [Player Colors](#player-colors)
- [Mentions and Notifications](#mentions-and-notifications)
- [Player Quick Actions](#player-quick-actions)
- [Command Clipboard](#command-clipboard)
- [Configuration and Data](#configuration-and-data)
- [Compatibility](#compatibility)
- [Known Limitations](#known-limitations)
- [FAQ](#faq)
- [Troubleshooting](#troubleshooting)
- [Development and Building](#development-and-building)
- [Project Structure](#project-structure)
- [Author and Video Channels](#author-and-video-channels)
- [License](#license)

---

## Highlights

### Visual Editor
- Real-time game viewport preview — what you see is what you get
- Drag the chat HUD to any position
- Eight-direction resizing
- Snap-to-edge and snap-to-center alignment guides
- Settings panel auto-avoids the chat preview (switches sides)
- Undo (Ctrl+Z) and Redo (Ctrl+Y)
- Save and Cancel — no manual config editing required

### Highly Customizable Text
- Independent controls for text size, line spacing, and character spacing
- Text opacity and shadow toggle
- Left, center, and right alignment
- Chinese, English, and emoji all render correctly
- Proper line wrapping and click detection; links and HoverEvents are preserved

### Background Customization
- Message background color and opacity
- Three display modes: follow text, full width, hidden
- Horizontal and vertical padding
- Input field background color, opacity, and border

### Per-Player Name Colors
- Stable automatic color based on player UUID
- Manual override for individual players
- Preserve vanilla server colors (no forced recoloring)
- Online player search with live preview
- Does not tint Chat Heads avatars

### Mentions and Notifications
- Double-click a player name in chat to insert `@PlayerName`
- Mention highlight with custom color and bold
- Mention sounds (multiple sound types, adjustable volume and pitch)
- Toast notifications with configurable preview length
- Full-screen flash alerts (adjustable color, opacity, and duration)
- Option to ignore your own mentions
- Each notification method can be toggled independently

### Player Quick Actions
- Right-click a player name in chat to open a quick-action menu
- Mention player, message player, copy player name
- Customizable private message template (`/msg {player}`, `/tell {player}`, `/w {player}`)
- All actions **only fill the input box — never auto-send**

### Command Clipboard
- Global client-side persistence, not bound to worlds or servers
- Save current chat input, custom name, and categories
- Favorites, search, edit, delete, and reorder
- Recent commands and usage counters
- Built-in command presets (gamemode, time, weather, keepInventory, etc.)
- Replace input or insert-at-cursor modes
- Shift-click to temporarily invert insert mode
- **Never auto-executes commands**
- Sensitive-command plaintext-storage warning
- Commands persist across world exits and game restarts

---

## Requirements

| Dependency | Type | Notes |
|------------|------|-------|
| Minecraft | Required | 1.21.1 |
| Java | Required | ≥ 21 |
| Fabric Loader | Required | ≥ 0.19.3 |
| Fabric API | Required | ≥ 0.116.14+1.21.1 |
| owo-lib | Required | ≥ 0.12.15.4+1.21 |
| Mod Menu | Optional | Open the editor from the mod menu |

---

## Installation

1. Install Minecraft 1.21.1
2. Install [Fabric Loader](https://fabricmc.net/use/) (≥ 0.19.3)
3. Install [Fabric API](https://modrinth.com/mod/fabric-api) (≥ 0.116.14)
4. Install [owo-lib](https://modrinth.com/mod/owo-lib) (≥ 0.12.15.4)
5. Place the Chat Canvas JAR into your `mods` folder
6. Launch the game

> Chat Canvas is a **client-side** mod. **No server installation** is required.

---

## Quick Start

1. Enter a world
2. Press the default keybind **K** to open the editor
3. Drag the preview chat HUD to your desired position
4. Drag the edges to resize
5. Switch between Layout, Text, Background, Player Colors, Mentions, and Command Input pages
6. Click **Save**
7. Press **T** to open real chat and verify
8. Double-click a player name to mention them
9. Right-click a player name for quick actions
10. Click the **Commands** button next to the chat input to open the command clipboard

---

## Visual Editor

The editor is split into two areas:
- **Left**: chat preview — drag the chat HUD directly, resize, WYSIWYG
- **Right**: settings panel with six category pages

### Numeric Scrubbing

- Hover over a numeric value, hold **left mouse** and drag horizontally
- **Scroll wheel** for fine adjustment (±1)
- Hold **Shift** for precise adjustment (±0.1)
- Hold **Ctrl** for fast adjustment (±10)
- **Right-click** to restore the default value

### Undo and Redo

- Toolbar buttons at the top
- Shortcuts: **Ctrl+Z** undo, **Ctrl+Y** redo

---

## Text Settings

| Option | Description | Range |
|--------|-------------|-------|
| Text Size | Chat text scaling | 0.5 – 2.0 |
| Line Spacing | Gap between lines | 0 – 20 px |
| Character Spacing | Extra gap between characters | 0 – 8 px |
| Text Opacity | Text opacity | 0 – 100% |
| Text Shadow | Shadow toggle | On / Off |
| Text Alignment | Left, center, right | — |

> When character spacing is set to 0, text rendering is identical to vanilla with no extra overhead.

---

## Background Settings

### Message Background

| Option | Description |
|--------|-------------|
| Display Mode | Follow text / Full width / Hidden |
| Background Color | Custom RGB color |
| Background Opacity | 0 – 100% |
| Horizontal Padding | Left/right padding around text |
| Vertical Padding | Top/bottom padding around text |

### Input Background

| Option | Description |
|--------|-------------|
| Input Color | Custom RGB color |
| Input Opacity | 0 – 100% |
| Input Border | Toggle and color |

---

## Player Colors

### Automatic Mode

Computes a stable HSV color from each player's UUID — the same player gets the same color across different servers.

### Vanilla Mode

Preserves server-assigned player name colors without applying new ones.

### Manual Override

The online player list supports:
- Searching by player name
- Clicking a color swatch to set a custom color
- Right-click to reset to automatic
- A customizable 24-color palette

---

## Mentions and Notifications

### Mention Insertion

- **Double-click** a player name in chat → inserts `@PlayerName` into the input box
- Only inserts — never auto-sends
- Configurable double-click interval

### Mention Highlighting

- Messages containing `@YourName` are highlighted
- Custom highlight color and bold toggle
- Option to only recognize mentions with an `@` symbol

### Notification Methods

Three independent notification channels:

1. **Sound**: multiple sound types (Experience Orb, Note Pling, Amethyst, Button Click), adjustable volume and pitch
2. **Toast**: message preview popup at the top of the screen
3. **Full-screen Flash**: brief color flash overlay

---

## Player Quick Actions

**Right-click** a player name in chat to open the quick-action menu:

| Action | Effect |
|--------|--------|
| Mention Player | Insert `@PlayerName` into the input |
| Message Player | Insert `/msg PlayerName ` using the template |
| Copy Player Name | Copy to clipboard |

The private message template supports the `{player}` placeholder. Default: `/msg {player} `.

> All actions **only fill the input box — never auto-send**. You must press Enter manually.

---

## Command Clipboard

Chat Canvas provides a **persistent command clipboard** that is independent of worlds and servers.

### Core Features

- Save the current chat input as a named command
- Custom names and categories
- Favorites, full-text search, edit, delete, and reorder
- Recent commands and usage counters
- Batch clear operations with confirmation

### Built-in Presets

| Preset | Command |
|--------|---------|
| Survival Mode | `/gamemode survival` |
| Creative Mode | `/gamemode creative` |
| Spectator Mode | `/gamemode spectator` |
| Set Day | `/time set day` |
| Set Night | `/time set night` |
| Clear Weather | `/weather clear` |
| Rain | `/weather rain` |
| Keep Inventory On | `/gamerule keepInventory true` |
| Keep Inventory Off | `/gamerule keepInventory false` |
| Peaceful | `/difficulty peaceful` |
| Easy | `/difficulty easy` |
| Normal | `/difficulty normal` |
| Hard | `/difficulty hard` |
| Set Spawn Point | `/spawnpoint` |
| Kill Self | `/kill` |

> Clicking a command **only fills the input box — it never auto-executes**. Press Enter to send as usual.

### Insert Modes

- **Replace Input**: clicking a command replaces the entire input box
- **Insert at Cursor**: clicking a command inserts at the current cursor position
- Hold **Shift** while clicking to temporarily invert the mode

---

## Configuration and Data

### File Locations

Main config:
```
.minecraft/config/chat_canvas.json
```

Command clipboard:
```
.minecraft/config/chat_canvas/command_clipboard.json
```

### Notes

- Both are **global client-side data**, not stored per-world
- Deleting the main config restores all default settings
- Deleting the command clipboard file removes all user-saved commands
- Back up before editing
- **Not recommended** to edit while the game is running
- If a config file is corrupted, the mod will try to back up the original and create a fresh default

> [!WARNING]
> The command clipboard stores commands locally as plain-text JSON. Do not save passwords, tokens, or private information on shared or untrusted computers, including commands such as `/login`, `/register`, and `/password`.

Chat Canvas does **not** upload commands, send them to the author, or auto-execute them. Commands are only processed by the game or server after you press Enter.

---

## Compatibility

Chat Canvas is designed to coexist with the following mods and has dedicated compatibility handling for them. Results may vary across different version combinations.

| Mod | Compatibility Notes |
|-----|---------------------|
| **Chat Heads** | Chat Canvas does not provide avatars. It preserves Chat Heads' head icons and works to keep them aligned with text, backgrounds, and hit areas |
| **More Chat History** | Chat Canvas does not modify chat history capacity — More Chat History continues to handle the history limit |
| **ChatAnimation** | Chat Canvas does not provide chat message animations — animations are handled by ChatAnimation |
| **Smooth Scrolling** | Chat Canvas does not reimplement chat scroll animations — scrolling is handled by Smooth Scrolling |

> These compatibility mods are **not hard dependencies**. Chat Canvas works correctly without any of them installed.

---

## Known Limitations

1. **Fabric 1.21.1 only** — Forge, NeoForge, and other versions are not supported
2. Client-side only — cannot run on a dedicated server
3. Server plugins that convert player chat to system messages may prevent reliable sender identification, affecting player colors and mention detection
4. Player quick actions may not apply when the sender cannot be reliably identified
5. Whether `/msg`, `/tell`, or `/w` commands work is determined by the server — Chat Canvas does not bypass permissions
6. Whether commands have permission to execute is determined by the server
7. Custom resource packs and fonts may alter text width and line wrapping
8. The command clipboard is stored as plain-text JSON
9. Chat Canvas does **not** provide player avatar functionality
10. Chat Canvas does **not** provide message entry or scroll animations

---

## FAQ

### Why is there no config button after installing?

Mod Menu is an optional dependency. You can install [Mod Menu](https://modrinth.com/mod/modmenu), or simply use the default keybind **K** to open the editor directly.

### Why doesn't clicking a command execute it immediately?

This is **by design** for safety. Chat Canvas only fills the chat input box — you must manually confirm and press **Enter**. The mod will never auto-execute commands.

### Why are there no player colors on some servers?

Server plugins may send chat as system messages, preventing the client from reliably obtaining the sender's UUID. This also affects mention detection.

### Why didn't I get a mention notification?

Check the following:
- Is the message using the full `@PlayerName` format?
- Is `requireAtSymbol` enabled (which only recognizes mentions with `@`)?
- Are the sound, toast, and flash toggles turned on?
- Is it a message sent by yourself (ignored by default)?
- Has the server modified the chat format?

### Why are my saved commands still showing up?

The command clipboard is **global persistent data**, not tied to a world or server. Use the command manager to clean up entries, or back up and delete the config file manually.

### Does Chat Canvas need to be installed on the server?

**No.** Chat Canvas is a client-side mod only.

### Does it work on Forge or NeoForge?

Not at this time. The current version only supports Fabric 1.21.1.

### Does it include Chat Heads functionality?

No. You can install [Chat Heads](https://modrinth.com/mod/chat-heads) separately.

### Does it include chat animations?

No. You can use [ChatAnimation](https://modrinth.com/mod/chat-animation) or [Smooth Scrolling](https://modrinth.com/mod/smooth-scrolling).

---

## Troubleshooting

### Basic Checks

If you encounter an issue, first verify:
1. Minecraft is **1.21.1**
2. Fabric Loader version matches
3. Fabric API version matches (≥ 0.116.14)
4. owo-lib version matches (≥ 0.12.15.4)
5. You haven't accidentally installed NeoForge or Forge versions of dependencies
6. You are not using an outdated Chat Canvas config
7. You don't have custom fonts or resource packs interfering
8. You don't have other mods modifying ChatHud or ChatScreen

### Isolation Test

1. Back up `config/chat_canvas.json` and `config/chat_canvas/`
2. Delete or rename the Chat Canvas config files
3. Test with only Fabric API, owo-lib, and Chat Canvas installed
4. Gradually re-add other mods to identify conflicts
5. Submit `latest.log`, full mod list, screenshots, and reproduction steps

---

## Development and Building

### Prerequisites
- Java 21
- Git

### Clone and Build

**Windows:**
```powershell
git clone https://github.com/ikunkk02-afk/Chat-Canvas.git
cd Chat-Canvas
.\gradlew.bat clean build
```

**Linux / macOS:**
```bash
git clone https://github.com/ikunkk02-afk/Chat-Canvas.git
cd Chat-Canvas
chmod +x gradlew
./gradlew clean build
```

### Run Client
```powershell
.\gradlew.bat runClient
```

### Build Output
```
build/libs/chat-canvas-1.0.0.jar
```

### Tech Stack
- Java 21
- [Fabric Loom](https://fabricmc.net/wiki/documentation:fabric_loom) 1.17
- [Yarn mappings](https://github.com/FabricMC/yarn) 1.21.1+build.3
- [Fabric API](https://github.com/FabricMC/fabric) 0.116.14
- [owo-lib](https://github.com/wisp-forest/owo-lib) 0.12.15.4

---

## Project Structure

```
src/
├── main/java/io/github/ikunkk02/chatcanvas/
│   ├── animation/          Animation engine (SpringValue, MotionPreset)
│   ├── chat/
│   │   ├── command/        Command clipboard storage and presets
│   │   ├── identity/       Player message identity resolution
│   │   ├── interaction/    Mention insertion interaction
│   │   ├── layout/         Chat HUD layout transforms and metrics
│   │   ├── mention/        Mention pattern matching
│   │   ├── notification/   Mention notification events
│   │   ├── style/          Text style overlay and indexing
│   │   └── text/           Character spacing glyph measurement
│   ├── config/             Configuration system
│   ├── editor/             Editor session, history, and snapshots
│   └── ui/                 Shared UI components
│
├── client/java/io/github/ikunkk02/chatcanvas/
│   ├── chat/
│   │   ├── command/        Command clipboard UI
│   │   ├── identity/       Player color runtime
│   │   ├── interaction/    Double-click and quick-action menu
│   │   ├── layout/         Layout runtime cache
│   │   ├── notification/   Notification controller
│   │   ├── render/         Chat rendering pipeline
│   │   ├── style/          Style overlay pipeline
│   │   └── text/           Character spacing renderer
│   ├── editor/             ChatCanvasEditorScreen
│   ├── integration/        ModMenu integration
│   ├── mixin/client/       Client mixins (12)
│   └── ui/                 AnimatedSettingsPanel, ClippedPageViewport, etc.
│
└── test/java/              26 unit tests
```

---

## Author and Video Channels

Chat Canvas is developed and maintained by **ShouYun / 寿云**.

- [Bilibili](https://space.bilibili.com/1832031043?spm_id_from=333.1007.0.0)
- [Douyin](https://www.douyin.com/user/MS4wLjABAAAAXPEr9Q0OnMztYvgDTXt6H3g9_626CRAmMbX1L64pBkxbvbHR2ACMWmL55mIL0-Gi)
- [Source Code](https://github.com/ikunkk02-afk/Chat-Canvas)
- [Issue Tracker](https://github.com/ikunkk02-afk/Chat-Canvas/issues)

---

## License

This project is licensed under the [MIT License](LICENSE).

You may use, modify, and distribute this project in accordance with the MIT License.
