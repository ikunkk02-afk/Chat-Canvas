# API Diff: Minecraft 1.21.1 → 1.21.5

Generated for Chat Canvas porting.

## Source and Target

| Item | Source | Target |
|------|--------|--------|
| Minecraft | 1.21.1 | 1.21.5 |
| Fabric Loader | 0.19.3 | 0.19.3 |
| Fabric API | 0.116.14+1.21.1 | 0.128.2+1.21.5 |
| Yarn mappings | 1.21.1+build.3 | 1.21.5+build.1 |
| owo-lib | 0.12.15.4+1.21 | 0.12.21+1.21.5 |
| ModMenu | 11.0.4 | 13.0.2 |

## Method Signature Changes

### DrawContext

| 1.21.1 | 1.21.5 | Notes |
|--------|--------|-------|
| `drawTextWrapped(TextRenderer, Text, int, int, int, int)` | `drawWrappedText(TextRenderer, StringVisitable, int, int, int, int, boolean)` | Renamed, added boolean shadow |
| — | `drawWrappedTextWithShadow(TextRenderer, StringVisitable, int, int, int, int)` | New convenience method |

**Action**: Changed `drawTextWrapped` → `drawWrappedTextWithShadow` in `CommandToolPanel.java`.

### ClickEvent

| 1.21.1 | 1.21.5 | Notes |
|--------|--------|-------|
| `new ClickEvent(Action, String)` | `new ClickEvent.RunCommand(String)` / `SuggestCommand(String)` / `CopyToClipboard(String)` | `ClickEvent` is an interface in 1.21.5; value accessors are event-specific (`command()` or `value()`) |

**Action**: Adapted the interaction-preservation tests to the concrete 1.21.5 event records. Production code continues to retain the original `Style` and `ClickEvent` objects.

### ChatHud

| 1.21.1 | 1.21.5 | Notes |
|--------|--------|-------|
| `getMessageLineIndex(double, double)` | Both `getMessageLineIndex` and `getMessageIndex` exist | 1.21.5 retains both; `getMessageIndex` delegates to `getMessageLineIndex` |

**Action**: No change needed. Original `getMessageLineIndex` @Shadow works.

### ChatScreen

All signatures preserved — no changes needed.

### Keyboard

`onKey(long, int, int, int, int)` — no change.

### ClientPlayNetworkHandler

`onPlayerList` / `onPlayerRemove` — no change.

## 1.21.5 Known Changes (Not Affecting Chat Canvas)

- Pick Block events moved from client to server side (not used by Chat Canvas)
- `TradeOfferHelper` signature change (not used)
- Resource Loader API changes (not used)
- Rendering pipeline refactors (compatible via existing mixins)
