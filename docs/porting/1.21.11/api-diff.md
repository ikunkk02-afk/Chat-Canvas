# API Diff: Minecraft 1.21.9 → 1.21.11 (Fabric / Yarn)

## Source
- Baseline branch: `mc/1.21.9` (tag: `v1.2.0-mc1.21.9`)
- Target version: `1.21.11`
- Mapping: Yarn

## ChatScreen

| Item | 1.21.9 | 1.21.11 | Impact |
|------|--------|---------|--------|
| `mouseClicked` | Uses `DrawnTextConsumer.ClickHandler` + `handleClickEvent(Style, boolean)` | Uses `ChatHud.mouseClicked(x, y)` + `handleTextClick(Style)` | No direct Mixin impact (ChatScreenMixin handles mouse at HEAD) |
| `render` | `ChatHud.render(context, textRenderer, ticks, mouseX, mouseY, true, shouldInsert())` | `ChatHud.render(context, ticks, mouseX, mouseY, true)` + `MessageIndicator` handling | ChatHud.render 7-param signature unchanged, but render logic refactored internally |
| `resize` | `resize(MinecraftClient, int, int)` | `resize(MinecraftClient, int, int)` | **Unchanged** |

## ChatHud — Major Refactoring

The ChatHud class underwent significant internal refactoring:

| Item | 1.21.9 | 1.21.11 | Impact |
|------|--------|---------|--------|
| `toChatLineX` | Private method | **REMOVED** | ChatHudMixin `@Shadow` removed |
| `toChatLineY` | Private method | **REMOVED** | ChatHudMixin `@Shadow` removed |
| `getMessageLineIndex` | Private method | **REMOVED** | ChatHudMixin `@Shadow` removed |
| `getTextStyleAt` | New method | New method | **Unchanged** (public API) |
| `mouseClicked(double, double)` | — | **NEW** | New public method |
| Internal rendering | Direct `DrawContext` calls | Uses `ChatHud.Backend` interface (Hud, Interactable, Forwarder) | No direct impact since Mixin overrides render at HEAD |

## Screen

| Item | 1.21.9 | 1.21.11 | Impact |
|------|--------|---------|--------|
| `resize` | `resize(int, int)` | `resize(int, int)` | **Unchanged** (ChatScreen overrides to 3-arg) |

## TextFieldWidget

| Item | 1.21.9 | 1.21.11 | Impact |
|------|--------|---------|--------|
| `drawSelection` | `drawSelection(int, int, int, int)` | `drawSelection(int, int, int, int, boolean)` | Added `boolean invert` parameter |

## TextHandler

| Item | 1.21.9 | 1.21.11 | Impact |
|------|--------|---------|--------|
| `getStyleAt(OrderedText, int)` | Method existed | **REMOVED** | Replaced with character-walk helper `chat_canvas$styleAtPixel` |

## PositionedSoundInstance

| Item | 1.21.9 | 1.21.11 | Impact |
|------|--------|---------|--------|
| `master(SoundEvent, float, float)` | Method existed | **REMOVED**, replaced by `ui()` | Changed to `PositionedSoundInstance.ui()` |

## Unchanged (Verified)

- `Keyboard.onKey(long, int, KeyInput)` — Method signature unchanged
- `ChatScreen.init()`, `keyPressed(KeyInput)`, `insertText()`, `removed()` — No changes
- `ChatInputSuggestor` — API unchanged
- `ClientPlayNetworkHandler` — Packet handling unchanged
- `DrawContext.drawText*`, `fill`, matrix methods — No changes from 1.21.9

## owo-lib

| Item | 1.21.9 | 1.21.11 | Impact |
|------|--------|---------|--------|
| Version used | 0.12.24+1.21.9 | 0.12.24+1.21.9 (cross-version compat) | No owo-lib change needed for this port |

## Summary

- 3 `@Shadow` methods removed from ChatHudMixin
- 1 method signature change (drawSelection +1 param)
- 1 method replaced (master → ui)
- 1 method removal (TextHandler.getStyleAt → custom helper)
- 1 resize signature mismatch (ChatCanvasEditorScreen)
- **Total: 5 source-level fixes, 0 Mixin target changes**
