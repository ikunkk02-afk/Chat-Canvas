# Chat Canvas v1.1.1 — Hotfix

**Fixes a large gray rectangle appearing in the Minecraft-style theme on modded clients.**

---

## 🐛 What was fixed

When using the **Minecraft-style theme** with the settings panel positioned on the **right side** of the screen, the category tab / text alignment / background mode selection indicators would draw a **massive opaque gray rectangle** across the entire editor preview area.

**Root cause:** `SelectionIndicatorComponent.draw()` was passing `segmentWidth - 2` and `height() - 2` directly to Minecraft's DrawContext.fill() as if they were absolute right/bottom coordinates — but they were width/height values. When the component sat on the right half of the screen, the small "50px width" value was treated as a left-most screen coordinate, creating a monster rectangle.

**Fix:** The vanilla selection indicator now computes proper absolute right/bottom coordinates and clamps them to the parent component bounds.

---

## ✨ What's new in 1.1.x (since 1.0.0)

### 🎨 Two Selectable Editor Visual Themes

Chat Canvas now provides **two visual themes** for the settings editor — not two different editors. Both themes share the exact same layout, features, configuration, and editing session. Switching themes changes only the visual appearance.

- **Chat Canvas Theme** — Semi-transparent modern panels, rounded corners, blue/purple accent colors, custom numeric scrubbers.
- **Minecraft-style Theme** — Vanilla-style buttons, borders, sliders and text fields. Same layout, same features.

Switch at any time via the "UI Theme" button in the editor header. Your preference is saved automatically.

### 🛠️ Command Clipboard UX Fix

The command clipboard launcher button now hides automatically while the panel is open and reappears when closed.

---

## 📋 Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader | 0.19.3+ |
| Fabric API | 0.116.14+1.21.1 |
| owo-lib | 0.12.15.4+1.21 |
| Mod Menu *(optional)* | 11.0.4+ |

---

## 🔗 Links

- **GitHub:** https://github.com/ikunkk02-afk/Chat-Canvas
- **Issues:** https://github.com/ikunkk02-afk/Chat-Canvas/issues
- **Author (Bilibili):** https://space.bilibili.com/1832031043

---

*Full changelog: [CHANGELOG.md](https://github.com/ikunkk02-afk/Chat-Canvas/blob/main/CHANGELOG.md)*
