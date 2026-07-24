[简体中文](README.md) | [English](README_EN.md)

<p align="center">
  <img src="src/main/resources/assets/chat_canvas/icon.png" width="180" alt="Chat Canvas">
</p>

# Chat Canvas

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green)
![Loader](https://img.shields.io/badge/Loader-Fabric-lightyellow)
![Side](https://img.shields.io/badge/Side-Client--only-blue)
![Java](https://img.shields.io/badge/Java-21%2B-orange)
![Version](https://img.shields.io/badge/Version-1.1.0-informational)
![License](https://img.shields.io/badge/License-MIT-brightgreen)

Chat Canvas is a **client-side only** chat HUD customisation mod for **Minecraft 1.21.1 Fabric**.

Drag, resize and preview your chat overlay directly in-game while customising text styles, backgrounds, player name colours, @‑mention alerts and command input features — all in real time.

Chat Canvas ships with **two selectable editor visual themes** that share the same layout, features and configuration — only the UI controls change their appearance.

---

## Table of Contents

- [Highlights](#highlights)
- [Two Editor Visual Themes](#two-editor-visual-themes)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Visual Editor](#visual-editor)
- [Layout Settings](#layout-settings)
- [Text Settings](#text-settings)
- [Background Settings](#background-settings)
- [Player Name Colours](#player-name-colours)
- [Mentions &amp; Notifications](#mentions--notifications)
- [Player Quick Actions](#player-quick-actions)
- [Command Clipboard](#command-clipboard)
- [Configuration &amp; Data Files](#configuration--data-files)
- [Compatibility](#compatibility)
- [Security Notice](#security-notice)
- [Known Limitations](#known-limitations)
- [FAQ](#faq)
- [Troubleshooting](#troubleshooting)
- [Building](#building)
- [Issue Tracker](#issue-tracker)
- [Author &amp; Links](#author--links)
- [License](#license)

---

## Highlights

- ✅ Drag and resize the chat HUD directly in-game
- ✅ WYSIWYG chat preview
- ✅ Eight-direction resizing with edge &amp; centre snapping
- ✅ Settings panel auto‑avoids the chat preview
- ✅ **Two switchable editor visual themes** (Chat Canvas &amp; Minecraft‑style)
- ✅ Text size, character spacing, line spacing, opacity, shadow &amp; alignment
- ✅ Message background colour, opacity and display mode
- ✅ Automatic per‑player name colours + manual overrides
- ✅ Double‑click @‑mentions + sound / toast / full‑screen flash
- ✅ Right‑click player name quick menu
- ✅ Persistent command clipboard (search / category / favourites / presets)
- ✅ Compatible with Chat Heads, More Chat History, ChatAnimation &amp; Smooth Scrolling
- ✅ Ctrl+Z / Ctrl+Y undo &amp; redo
- ✅ Theme preference persisted across sessions

---

## Two Editor Visual Themes

Chat Canvas provides two **visual themes** for the editor — they are **not** two different editors. Both themes:

- Use the exact same editor layout
- Offer the exact same features
- Read and write the same configuration file
- Share the same editing session (EditorSession)
- Share the same undo / redo history
- Never lose unsaved changes when switching
- Only change the appearance of buttons, panels, sliders and text controls

### Chat Canvas Theme

- Semi‑transparent modern panels
- Rounded corners, shadows and custom controls
- Blue and purple accent colours
- Custom numeric scrubbers
- For players who prefer a modern UI

### Minecraft‑style Theme

- Minecraft‑style buttons, borders, sliders and text fields
- Square edges and vanilla grey visuals
- The exact same editor layout and features
- For players who prefer an appearance that matches the vanilla game

> **How to switch:** Click the "UI Theme" button in the editor header — your preference is saved automatically.

---

## Requirements

| Dependency | Type | Version |
|---|---|---|
| Minecraft | Required | 1.21.1 |
| Java | Required | 21 or higher |
| Fabric Loader | Required | 0.19.3 or higher |
| Fabric API | Required | 0.116.14+1.21.1 or compatible |
| owo‑lib | Required | 0.12.15.4+1.21 or compatible |
| Mod Menu | Optional | 11.0.4 or compatible |

> Chat Canvas is client‑side only. It does not need to be installed on the server and does not bypass server permissions.

---

## Installation

1. Download the latest Chat Canvas JAR from [Releases](https://github.com/ikunkk02-afk/Chat-Canvas/releases)
2. Place the JAR in your `.minecraft/mods/` folder
3. Launch Minecraft 1.21.1 Fabric

Chat Canvas registers a configuration entry in Mod Menu automatically.

---

## Quick Start

1. Press **K** (default keybind) to open the editor
2. Drag the chat overlay to your desired position
3. Drag the corners to resize the chat area
4. Use the six category tabs in the settings panel for detailed customisations
5. Click **Save** to apply

> While the editor is open you can see a live preview of your chat HUD on the left.

---

## Visual Editor

- **Live preview**: See exactly how the chat HUD will look
- **Drag position**: Click and drag the chat preview to move it
- **Eight‑direction resize**: Drag the edges and corners to adjust width and height
- **Snapping**: Snaps to screen edges, corners and centre
- **Settings panel**: A floating panel on the right that auto‑switches sides to avoid the preview
- **Category tabs**: Layout, Text, Background, Player Colours, Mentions, Command Input — 6 tabs with horizontal sliding
- **Undo / Redo**: Ctrl+Z / Ctrl+Y, or the toolbar buttons
- **Save / Cancel**: Fixed footer bar

---

## Layout Settings

- X position (pixels)
- Y position (pixels)
- Width (pixels)
- Height (pixels)
- Chat open / closed preview state toggle
- Values adapt to your resolution and GUI Scale

---

## Text Settings

- **Font scale**: 50% – 200%
- **Line spacing**: 0 – 20 px
- **Character spacing**: 0.0 – 8.0 px
- **Text opacity**: 0% – 100%
- **Text shadow**: on / off
- **Text alignment**: left / centre / right

All text settings support Chinese, English, Unicode and emoji.

### Numeric Scrubbing

- Hover the value area, hold left mouse button and **drag left / right**
- **Scroll wheel** for fine adjustment
- **Shift** for precise control
- **Ctrl** for rapid adjustment
- **Right‑click** to restore the default
- Only one undo history entry is created on release

---

## Background Settings

- **Display mode**: follow text / full width / hidden
- **Message background colour**: RGB colour picker
- **Message background opacity**: 0% – 100%
- **Horizontal padding**: 0 – 12 px
- **Vertical padding**: 0 – 6 px
- **Input field background colour**
- **Input field opacity**: 0% – 100%
- **Input field border colour** and opacity

---

## Player Name Colours

- Automatically assigns a **stable colour per player UUID**
- Player colour persists across name changes (same UUID)
- **Manual per‑player colour override**
- Custom colour palette editing
- Search online players
- Restore automatic colour
- Respects the server's original colour mode (does not force override)
- Does not tint Chat Heads avatars

---

## Mentions &amp; Notifications

### Mention Behaviour

- **Double‑click a player name** to insert `@playername` (inserts at cursor, does not send)
- Mention text highlighting with customisable colour
- Optional bold
- Configurable double‑click interval
- Optional requirement for an `@` prefix

### Mention Notifications

- **Sound alert**: multiple sound types, adjustable volume and pitch
- **Toast**: on‑screen popup with configurable message preview length
- **Full‑screen flash**: customisable colour, opacity and duration
- Ignore your own messages

---

## Player Quick Actions

**Right‑click** a player name in chat to:

- **Mention player**: insert `@playername`
- **Whisper player**: using a configurable template (`/msg {player}`, `/tell {player}`, `/w {player}`)
- **Copy player name**

> All quick actions only modify the chat input field — they never send messages automatically.

---

## Command Clipboard

A "Commands" button appears next to the chat input field. Click it to open the clipboard panel:

- The **launcher button hides automatically** while the panel is open and reappears when closed
- **My Commands**: your saved command list
- **Presets**: built‑in game commands (difficulty, time, weather, etc.)
- **Search** by title, command text or category
- **Category filter**: filter by custom categories
- **Favourites**: star your frequent commands
- **Recent**: sort by recently used
- **Save current input**: save the text currently in the chat field
- **Edit / Delete / Reorder**
- Insert at **cursor position** or **replace the entire input**
- Shift‑click to temporarily invert the insert mode
- Confirmation dialogs prevent accidental deletion or clearing
- Data persists across worlds, servers and restarts
- **Commands are never executed automatically**

---

## Configuration &amp; Data Files

Settings are stored locally on your client (not per‑world):

| File | Path |
|---|---|
| Main config | `.minecraft/config/chat_canvas.json` |
| Command clipboard | `.minecraft/config/chat_canvas/command_clipboard.json` |

- Switching worlds or servers does not lose your settings
- Delete the main config file to reset all settings
- Deleting the clipboard file removes all saved commands
- Back up before modifying
- Manual editing during play is not recommended
- Corrupted configs are backed up and replaced with defaults automatically

---

## Compatibility

### Chat Heads

Chat Canvas does not provide player avatars. With Chat Heads installed, avatars display normally and participate in text width, background and alignment calculations. Double‑click and right‑click only respond to the player name, not the avatar area.

### More Chat History

Chat Canvas does not alter the chat history size — More Chat History handles that.

### ChatAnimation

Chat Canvas does not provide message entry animations — ChatAnimation does.

### Smooth Scrolling

Chat Canvas does not implement scroll animation — Smooth Scrolling provides it.

> Behaviour may differ across mod combinations, resource packs, custom fonts, GUI Scales and large modpacks.

---

## Security Notice

> [!WARNING]
> The command clipboard stores commands as **local plain‑text JSON**. **Do not save passwords, tokens or private information** on shared or untrusted computers — for example, `/login`, `/register`, `/password`.

- Chat Canvas **never uploads** your commands
- Commands are never sent to the author or third parties
- Clicking a command **only fills the input field** — it never executes automatically
- Commands are only processed after you press Enter
- A plain‑text storage warning is shown when saving commands containing sensitive keywords
- You are responsible for what you choose to save

---

## Known Limitations

1. Currently supports Fabric 1.21.1 only
2. Client‑side only — no Forge or NeoForge version
3. Does not provide player avatars (use Chat Heads)
4. Does not provide message entry animations (use ChatAnimation)
5. Does not provide scroll animations (use Smooth Scrolling)
6. Some server plugins that convert player messages to system messages may strip sender UUIDs
7. Player colour and quick actions may not work when player identity cannot be determined
8. Custom fonts may alter text width, wrapping and click hit‑testing
9. Specialised chat‑format mods may require additional compatibility work
10. Whisper commands are server‑dependent — `/msg` may not be available
11. Command permissions are determined by the server — Chat Canvas cannot bypass them
12. The command clipboard uses local plain‑text JSON storage

---

## FAQ

### How do I open the editor?

Press **K** (default) or use the Mod Menu config button. The keybind can be changed in Minecraft's Controls menu.

### How do I switch between themes?

Click the "UI Theme" button in the editor header and select your preferred theme.

### Will switching themes lose my changes?

No. Switching only changes visual control styles. All unsaved edits, undo history and your current category tab are preserved.

### Why don't I see a Mod Menu config entry?

Make sure Mod Menu is installed. Chat Canvas registers its entry automatically.

### Why do some player names lack automatic colours?

If the server converts player messages to system messages, the sender UUID may be lost, making colour assignment impossible.

### Why doesn't double‑clicking a name insert a mention?

Ensure "Double‑click mention" is enabled in the Mentions settings, and that you double‑click within the configured time interval.

### Why does the command button disappear when the panel opens?

This is by design. The launcher button hides while the clipboard panel is open so it does not obstruct the panel. It reappears when you close the panel.

### Why doesn't clicking a command execute it immediately?

Chat Canvas only fills the input field. You must press Enter to send — this keeps you in control.

### Where are commands saved?

`.minecraft/config/chat_canvas/command_clipboard.json` (local plain‑text JSON).

### Does Chat Canvas need to be installed on the server?

No. It is client‑side only.

### Does it support Forge / NeoForge?

The current version supports Fabric 1.21.1 only.

---

## Troubleshooting

1. Verify Minecraft version is 1.21.1
2. Verify Fabric Loader is installed
3. Verify Fabric API and owo‑lib are installed
4. Back up `config/chat_canvas.json` and `config/chat_canvas/`
5. Temporarily rename config files to test for corruption
6. Test with only Chat Canvas and its required dependencies
7. Re‑enable other mods in batches to isolate conflicts
8. Check custom fonts and resource packs
9. Provide `latest.log`, mod list, screenshots and reproduction steps when reporting

---

## Building

```bash
git clone https://github.com/ikunkk02-afk/Chat-Canvas.git
cd Chat-Canvas

./gradlew.bat build
./gradlew.bat runClient
```

- Java 21+
- Gradle is bootstrapped via the included wrapper

---

## Issue Tracker

Please report issues at [GitHub Issues](https://github.com/ikunkk02-afk/Chat-Canvas/issues) with:

- Chat Canvas version
- Minecraft version
- Fabric Loader / Fabric API / owo‑lib versions
- Other chat‑related mods
- GUI Scale
- Resource packs &amp; fonts
- `latest.log`
- Screenshots or video
- Reliable reproduction steps

---

## Author &amp; Links

Chat Canvas is developed and maintained by **寿云 (Shou Yun)**.

- Bilibili: [https://space.bilibili.com/1832031043](https://space.bilibili.com/1832031043?spm_id_from=333.1007.0.0)
- Douyin: [https://www.douyin.com/user/MS4wLjABAAAAXPEr9Q0OnMztYvgDTXt6H3g9_626CRAmMbX1L64pBkxbvbHR2ACMWmL55mIL0-Gi](https://www.douyin.com/user/MS4wLjABAAAAXPEr9Q0OnMztYvgDTXt6H3g9_626CRAmMbX1L64pBkxbvbHR2ACMWmL55mIL0-Gi)
- GitHub: [https://github.com/ikunkk02-afk/Chat-Canvas](https://github.com/ikunkk02-afk/Chat-Canvas)

---

## License

This project is licensed under the [MIT License](LICENSE).

Copyright &copy; 2026 寿云
