# Chat Canvas v1.1.1 — Hotfix

## 修复内容 | Fix

### 🐛 Minecraft 原版风格主题灰色矩形修复 | Vanilla Theme Gray Rectangle Fix

修复 SelectionIndicator 在 Minecraft 原版风格主题下，将宽度/高度误传为 `context.fill()` 的 right/bottom 坐标，导致设置面板位于右侧时出现覆盖整个预览区的巨大灰色矩形。

Fixed an incorrect coordinate bug where `SelectionIndicatorComponent.draw()` passed `segmentWidth - 2` and `height() - 2` directly to `context.fill()`, which expects absolute (left, top, right, bottom) coordinates — not (width, height). This caused a large opaque gray rectangle across the editor preview when the settings panel was on the right side.

### 细节 | Details

- 修复分类标签选中指示器坐标 → Fixed category tab selection indicator coordinates
- 修复文字对齐选中指示器坐标 → Fixed text alignment selection indicator coordinates
- 修复背景模式选中指示器坐标 → Fixed background mode selection indicator coordinates
- 新增 `ModernUiTheme.transparentButton()` 用于不需要实体背景的点击区域 → Added transparentButton() for hit-target buttons

---

**完整更新日志 / Full Changelog**: [CHANGELOG.md](CHANGELOG.md)
