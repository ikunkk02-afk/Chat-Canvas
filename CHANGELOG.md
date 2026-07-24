# Changelog

All notable changes to Chat Canvas will be documented in this file.

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
