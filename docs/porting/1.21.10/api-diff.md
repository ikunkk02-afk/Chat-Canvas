# API Diff: 1.21.1 / 1.3.0 → 1.21.10 / 1.3.0

## Summary

The target keeps the 1.21.10 template's Minecraft/Fabric coordinates while
bringing the 1.21.1 / Chat Canvas 1.3.0 functionality across. The target
already uses the newer input records, but several callback descriptors and
rendering internals still required direct verification and adaptation.

## Verified Target API

| Check | Result |
|-------|--------|
| Chat Canvas version | 1.3.0 |
| `ChatScreen.keyPressed` | Same signature: `boolean keyPressed(KeyInput input)` |
| `ChatScreen.mouseClicked` | Same signature: `boolean mouseClicked(Click click, boolean doubled)` |
| `ChatScreen.mouseScrolled` | Same signature: `boolean mouseScrolled(double, double, double, double)` |
| `ChatScreen.insertText` | Same signature: `void insertText(String, boolean)` |
| `ChatScreen.init` | Same signature: `void init()` |
| `ChatScreen.removed` | Same signature: `void removed()` |
| `ChatScreen.resize` | Same signature: `void resize(MinecraftClient, int, int)` |
| `ChatScreen.render` | Same signature: `void render(DrawContext, int, int, float)` |
| `Keyboard.onKey` | Same signature: `void onKey(long, int, KeyInput)` |
| `ChatInputSuggestor.keyPressed` | Same signature: `boolean keyPressed(KeyInput)` |
| `ChatInputSuggestor.mouseClicked` | Same signature: `boolean mouseClicked(Click)` |
| `ChatInputSuggestor.mouseScrolled` | Same signature: `boolean mouseScrolled(double)` |
| `KeyBinding.matchesKey` | Same signature: `boolean matchesKey(KeyInput)` |
| `DrawContext.fill` | 5-param fill still exists |
| `DrawContext.drawSelection` | Exists (from 1.21.7+) |
| `RenderPipelines.GUI` | Exists (from 1.21.6+) |
| `Matrix3x2fStack` | Exists (from 1.21.6+) |
| `drawText*` return `void` | Same as 1.21.9 (from 1.21.6+) |
| `drawWrappedTextWithShadow` | Same as 1.21.9 (from 1.21.6+) |

## Input Records Used by the Target

- `KeyInput(int key, int scancode, int modifiers)` - replaces bare `(keyCode, scanCode, modifiers)`
- `CharInput(int codepoint, int modifiers)` - replaces bare `(codePoint, modifiers)`  
- `Click(double x, double y, MouseInput buttonInfo)` - replaces bare `(mouseX, mouseY, button)`
- `MouseInput(int button, int modifiers)`

## Adaptations Required in the Target

- `ParentElement.mouseDragged`, `mouseReleased`, and `charTyped` use
  `Click`/`CharInput` records; the `AbstractParentElementMixin` descriptors and
  dispatch calls were updated accordingly.
- `GameProfile` exposes `name()` and `id()` in the target mappings.
- `KeyBinding.Category.MISC` is required by the target constructor.
- Owo callbacks use the target `Click`, `KeyInput`, and `CharInput` types; the
  editor, command tools, Emoji picker, and color picker were adapted without
  changing their behavior.
- `DrawContext`/owo do not expose the old `drawBorder` helper; the existing
  Chat Canvas border renderer is used instead.
- The target `TextRenderer` does not expose the source font-storage accessor or
  glyph method. Emoji font support therefore uses the target-safe text-width
  capability evaluation path.

## Dependency Changes

| Dependency | 1.21.9 | 1.21.10 |
|-----------|--------|---------|
| Minecraft | 1.21.9 | 1.21.10 |
| Yarn Mappings | 1.21.9+build.1 | 1.21.10+build.3 |
| Fabric API | 0.134.1+1.21.9 | 0.138.4+1.21.10 |
| owo-lib | 0.12.24+1.21.9 | 0.12.24+1.21.9 (same; 1.21.9+ compatible) |
| Fabric Loader | 0.19.3 | 0.19.3 (same) |
| Loom | 1.17.17 | 1.17.17 (same) |
| ModMenu | 16.0.1 | 16.0.1 (same) |
| Java | 21 | 21 (same) |
