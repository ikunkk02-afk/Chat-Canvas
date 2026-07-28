# API Diff: Minecraft 1.21.1 → 1.21.2

Baseline Minecraft version: 1.21.1
Target Minecraft version: 1.21.2

## Summary

The 1.21.1 → 1.21.2 Fabric upgrade is a **low-impact point release** for Chat Canvas.
All Mixin target classes retain identical method signatures and field layouts.
No Mixin changes are required.

## Classes Analyzed

| Class | 1.21.1 Yarn | 1.21.2 Yarn | Changes |
|-------|------------|------------|---------|
| `ChatScreen` | Same | Same | None — identical source |
| `Keyboard` | Same | Same | None — identical source |
| `ChatHud` | Same | Same | `refresh()` and `getLineHeight()` changed visibility to `private` (Mixin-safe) |
| `ClientPlayNetworkHandler` | `onPlayerList(PlayerListS2CPacket)` / `onPlayerRemove(PlayerRemoveS2CPacket)` | Same | None — identical signatures |
| `TextFieldWidget` | `extends ClickableWidget implements Drawable` | `extends ClickableWidget` | Removed `implements Drawable` (all methods retained in ClickableWidget) |
| `ChatInputSuggestor` | Same | Same | None — identical source |
| `TextRenderer$Drawer` | Same | Same | None — identical source |
| `AbstractParentElement` / `ParentElement` | Same | Same | None |
| `Screen` | `addSelectableChild` | Same | None |

## Method Audit

### ChatScreen
- `init()` — identical
- `keyPressed(int, int, int)` — identical
- `mouseScrolled(double, double, double, double)` — identical
- `mouseClicked(double, double, int)` — identical
- `render(DrawContext, int, int, float)` — identical
- `removed()` — identical
- `resize(MinecraftClient, int, int)` — identical
- `insertText(String, boolean)` — identical
- `sendMessage(String, boolean)` — identical
- `normalize(String)` — identical

### Keyboard
- `onKey(long, int, int, int, int)` — identical

### ChatHud
- `render(DrawContext, int, int, int, boolean)` — identical
- `addMessage(Text, MessageSignatureData, MessageIndicator)` — identical
- `getTextStyleAt(double, double)` — identical
- `refresh()` — now `private` (was package-private; Mixin can still inject)
- `getLineHeight()` — now `private` (Mixin-safe via @ModifyReturnValue)

### ClientPlayNetworkHandler
- `onPlayerList(PlayerListS2CPacket)` — identical
- `onPlayerRemove(PlayerRemoveS2CPacket)` — identical

### TextFieldWidget
- `renderWidget(DrawContext, int, int, float)` — identical
- `onClick(double, double)` — identical
- `keyPressed(int, int, int)` — identical
- `charTyped(char, int)` — identical
- `write(String)` — identical
- `setText(String)` — identical
- `updateFirstCharacterIndex(int)` — identical
- `getCharacterX(int)` — identical
- `drawSelectionHighlight(DrawContext, int, int, int, int)` — identical

## Dependency Versions

| Dependency | 1.21.1 Baseline | 1.21.2 Target | Source |
|-----------|----------------|--------------|--------|
| Minecraft | 1.21.1 | 1.21.2 | — |
| Fabric Loader | 0.19.3 | 0.16.7 | fabricmc.net/2024/10/14/1212.html |
| Fabric API | 0.116.14+1.21.1 | 0.105.4+1.21.2 | Modrinth |
| Yarn mappings | 1.21.1+build.3 | 1.21.2+build.1 | Fabric develop site |
| Fabric Loom (remap) | 1.17-SNAPSHOT | 1.14.10 | Maven Central |
| owo-lib | 0.12.15.4+1.21 | 0.12.18+1.21.2 | Modrinth |
| Java | 21 | 21 | — |

## No Mixin Changes Required

All 15 Mixin classes remain source-compatible after the 1.21.1 → 1.21.2 upgrade:
- ChatScreenMixin
- ChatHudMixin
- ChatHudAccessor
- KeyboardMixin
- ClientPlayNetworkHandlerMixin
- ChatInputSuggestorMixin
- ChatInputSuggestorAccessor
- SuggestionWindowAccessor
- TextFieldWidgetMixin
- TextFieldWidgetAccessor
- TextRendererDrawerMixin
- TextRendererAccessor
- AbstractParentElementMixin
- ChatScreenAccessor
- ScreenAccessor

## Notable API Observations

1. **TextFieldWidget no longer `implements Drawable`**: In 1.21.2, the explicit `implements Drawable` was removed from the class declaration since `ClickableWidget` already implements it. No impact — all methods remain.

2. **ChatHud.refresh() visibility**: Changed to `private` from package-private. Mixin `@Inject` can still target private methods in the same `@Mixin` class.

3. **No chat message flow changes**: Player chat, system messages, command results, join/leave, death messages, and signed chat all use the same packet types and handler methods as 1.21.1.

4. **No rendering API changes**: `DrawContext`, `TextRenderer`, matrix stack, scissor, and fill operations are identical.

5. **No Fabric API event changes**: Client-side lifecycle callbacks, keybinding registration, and resource reload listeners remain unchanged.
