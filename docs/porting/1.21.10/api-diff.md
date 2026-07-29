# API Diff: 1.21.9 → 1.21.10

## Summary

The 1.21.9 → 1.21.10 port is a **minimal port**. Both versions share the same Fabric API generation and the input refactoring (KeyInput/Click/CharInput records) occurred in 1.21.9 or earlier. No classes were added or removed between these versions.

## Verified: No Breaking Changes

| Check | Result |
|-------|--------|
| Classes added/removed | 0 / 0 |
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

## Input Records (Introduced in 1.21.9)

- `KeyInput(int key, int scancode, int modifiers)` - replaces bare `(keyCode, scanCode, modifiers)`
- `CharInput(int codepoint, int modifiers)` - replaces bare `(codePoint, modifiers)`  
- `Click(double x, double y, MouseInput buttonInfo)` - replaces bare `(mouseX, mouseY, button)`
- `MouseInput(int button, int modifiers)`

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
