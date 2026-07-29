# API Diff: Minecraft 1.21.1 → 1.21.4

Generated for Chat Canvas porting.

## Source and Target

| Item | Source | Target |
|------|--------|--------|
| Minecraft | 1.21.1 | 1.21.4 |
| Fabric Loader | 0.19.3 | 0.19.3 |
| Fabric API | 0.116.14+1.21.1 | 0.119.4+1.21.4 |
| Yarn mappings | 1.21.1+build.3 | 1.21.4+build.8 |
| owo-lib | 0.12.15.4+1.21 | 0.12.20+1.21.4 |
| ModMenu | 11.0.4 | 13.0.2 |
| Java | 21 | 21 |

## Method Signature Changes

### DrawContext

| 1.21.1 | 1.21.4 | Notes |
|--------|--------|-------|
| `drawTextWrapped(TextRenderer, Text, int, int, int, int)` | `drawWrappedText(TextRenderer, StringVisitable, int, int, int, int, boolean)` | Renamed, added `boolean shadow` |
| — | `drawWrappedTextWithShadow(TextRenderer, StringVisitable, int, int, int, int)` | New convenience method |

**Action**: Changed `drawTextWrapped` → `drawWrappedTextWithShadow` in `CommandToolPanel.java`.

### ChatHud

| 1.21.1 | 1.21.4 | Notes |
|--------|--------|-------|
| `getMessageLineIndex(double, double)` | `getMessageIndex(double, double)` | Renamed |

**Action**: Updated `ChatHudMixin.java` @Shadow method name and call site.

### ChatScreen

All method signatures preserved:
- `init()` ✓
- `setInitialFocus()` ✓
- `keyPressed(int, int, int)` ✓
- `insertText(String, boolean)` ✓
- `resize(MinecraftClient, int, int)` ✓
- `removed()` ✓
- `mouseClicked(double, double, int)` ✓ (now returns boolean, same as before)
- `mouseScrolled(double, double, double, double)` ✓ (now returns boolean, same as before)
- `render(DrawContext, int, int, int, float)` ✓

Fields:
- `chatField` (protected TextFieldWidget) ✓
- `chatInputSuggestor` (package-private ChatInputSuggestor) ✓

### Keyboard

| 1.21.1 | 1.21.4 | Notes |
|--------|--------|-------|
| `onKey(long, int, int, int, int)` | `onKey(long, int, int, int, int)` | No change ✓ |

### ClientPlayNetworkHandler

| 1.21.1 | 1.21.4 | Notes |
|--------|--------|-------|
| `onPlayerList(PlayerListS2CPacket)` | `onPlayerList(PlayerListS2CPacket)` | No change ✓ |
| `onPlayerRemove(PlayerRemoveS2CPacket)` | `onPlayerRemove(PlayerRemoveS2CPacket)` | No change ✓ |

### ChatInputSuggestor

All methods preserved: `refresh()`, `mouseClicked()`, `mouseScrolled()`, `keyPressed()`, `setWindowActive()`, `setCanLeave()`, `render()` ✓

### TextFieldWidget

All methods preserved: `setChangedListener()`, `write()`, `setText()`, `getText()`, `setMaxLength()`, `setDrawsBackground()`, `setFocusUnlocked()`, `setFocused()` ✓

## Unchanged Systems

- **TextRenderer / TextRenderer.Drawer**: No API changes affecting our usage
- **AbstractParentElement**: No changes
- **Screen**: No changes affecting accessors
- **Font / Glyph**: No breaking changes
- **Identifier**: `Identifier.of()` unchanged
- **Text API**: `Text.translatable()`, `Text.literal()` unchanged

## Fabric API Changes

Fabric API 0.119.4+1.21.4 is backward compatible with 0.116.14+1.21.1 for all APIs used:
- ClientTickEvents.END_CLIENT_TICK ✓
- ClientLifecycleEvents.CLIENT_STOPPING ✓
- KeyBindingHelper.registerKeyBinding ✓
- ResourceManagerHelper.registerReloadListener ✓

## owo-lib Changes

owo-lib 0.12.20+1.21.4 is compatible with 0.12.15.4 API. No breaking changes affecting Chat Canvas config UI.
