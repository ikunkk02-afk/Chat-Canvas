# Changelog

All notable changes to Chat Canvas will be documented in this file.

## [1.0.0] - 2026-07-24

### Added

- Visual chat HUD editor with live game viewport preview
- Drag-to-move and eight-direction resizing with snap-to-edge alignment guides
- Settings panel with automatic left/right side avoidance
- Six-category settings page system with horizontal tab switching
- **Selectable editor UI: Chat Canvas (modern owo-ui) or Minecraft-style (vanilla widgets)**
- **Runtime UI style switching without losing unsaved changes, undo history, or scroll positions**
- **Vanilla-style editor with ButtonWidget, SliderWidget, TextFieldWidget, and all 6 category pages**
- **VanillaColorPickerScreen with R/G/B sliders and hex input**
- Text size, line spacing, character spacing, text opacity, and shadow toggle
- Left, center, and right text alignment
- Message background with colors, opacity, three display modes (follow text, full width, hidden), horizontal/vertical padding
- Input field background with colors, opacity, and border
- Per-player UUID-based stable automatic name colors with manual override
- Player color palette customization (24 colors)
- Online player search in color settings
- Mention insertion by double-clicking player names in chat
- Mention highlight with custom color and bold toggle
- Mention sound notifications with multiple sound types, adjustable volume and pitch
- Mention toast notifications with configurable preview length
- Mention full-screen flash alerts with adjustable color, opacity, and duration
- Option to ignore self-sent mentions and require `@` symbol
- Player quick-action menu (right-click player name): mention, private message, copy name
- Customizable private message command template
- Persistent command clipboard with save, name, categorize, favorite, search, edit, delete, reorder, and batch clear
- Command usage counters and recent commands tracking
- 15 built-in command presets (gamemode, time, weather, keepInventory, difficulty, spawnpoint, kill)
- Replace-input and insert-at-cursor modes with Shift-click inversion
- Sensitive-command plaintext-storage warning dialog
- Mod Menu integration
- Chinese (zh_cn) and English (en_us) localisation

### Fixed

- Chat history overlapping with input field at extreme layouts
- Right-aligned text overflowing chat boundary
- Line-spacing causing black background artefacts
- Inactive settings pages visually leaking into the chat preview area across modded UI environments
- Settings page input dispatch broken after clipping fix
- Editor title overflowing the toolbar left edge
- Removed obsolete "Future categories" placeholder section

### Compatibility

- Chat Heads — head icons preserved and aligned with custom text, backgrounds, and layout
- More Chat History — history capacity left to More Chat History
- ChatAnimation — message entry animations left to ChatAnimation
- Smooth Scrolling — scroll animations left to Smooth Scrolling
- Tested in large Fabric 1.21.1 modpacks
