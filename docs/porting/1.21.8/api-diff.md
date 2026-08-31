# API Diff: Minecraft 1.21.6 → 1.21.8 (Fabric / Yarn)

## Source
- Baseline: Minecraft 1.21.6 (Chat Canvas mc/1.21.6 branch)
- Target: Minecraft 1.21.8
- Mapping: Yarn
- Tool: minecraft-dev-cli (MCP decompiler)

## High-Level Summary

Between 1.21.6 and 1.21.8, the chat/Screen/DrawContext API is **largely unchanged**.
Only 3 classes were added and 1 removed at the structural level:

| Type | Change |
|------|--------|
| Added classes | `class_11541`, `KeyedItemRenderState`, `TintedParticleEffect` |
| Removed classes | `EntityEffectParticleEffect` |

However, the 1.21.7 intermediate release introduced changes that affect this port:

The functional source baseline for this release is Chat Canvas 1.3.0 on Minecraft
1.21.1. In addition to the cumulative 1.21.6 → 1.21.8 changes documented below,
the 1.21.8 mappings expose `ClickEvent` as an interface with concrete nested
record implementations (`RunCommand`, `SuggestCommand`, `OpenUrl`, and
`CopyToClipboard`). The production interaction-preservation code uses the
target's `Style`/`ClickEvent` API, and the source-derived tests were adapted to
construct and inspect those concrete records instead of the 1.21.1 constructor
shape.

## Key API Changes (1.21.6 → 1.21.8 cumulative)

### 1. TextFieldWidget.drawSelectionHighlight → DrawContext.drawSelection (1.21.7)

| Version | Status |
|---------|--------|
| 1.21.6 | `TextFieldWidget.drawSelectionHighlight(DrawContext, int, int, int, int)` exists as private method |
| 1.21.7+ | Method **removed** from TextFieldWidget |
| 1.21.7+ | Replaced by `DrawContext.drawSelection(int x1, int y1, int x2, int y2)` |

**Fix**: Remove `@Shadow drawSelectionHighlight`, use `context.drawSelection()` instead.

### 2. DrawContext.drawText Alpha Channel Check (1.21.7+)

In 1.21.7+, `DrawContext.drawText()` checks `ColorHelper.getAlpha(color) != 0` before rendering.
Colors without explicit alpha (e.g., `0xE0E0E0`) are silently dropped.

**Checked and safe**: All Chat Canvas drawText calls use full ARGB colors (0xFF prefix).

### 3. MatrixStack → Matrix3x2fStack (1.21.6)

Already handled in mc/1.21.6 branch:
- `push()` → `pushMatrix()`
- `pop()` → `popMatrix()`
- `translate(x, y, 0)` → `translate(x, y)`
- `scale(x, y, 1)` → `scale(x, y)`

### 4. RenderLayer → RenderPipelines (1.21.6)

Already handled in mc/1.21.6 branch:
- Import changed to `net.minecraft.client.gl.RenderPipelines`
- `RenderLayer.getGuiOverlay()` → `RenderPipelines.GUI`

### 5. DrawContext drawText Return Type (1.21.6+)

Already handled in mc/1.21.6 branch:
- `drawTextWithShadow(...)` returns `void`
- `drawText(...)` returns `void`
- `drawWrappedTextWithShadow(...)` returns `void`

### 6. ChatHud.render Lambda Refactor (1.21.6+)

Already handled in mc/1.21.6 branch:
- `@WrapOperation` on `fill` and `drawTextWithShadow` in `ChatHud.render` are commented out
- `@Inject` at HEAD/RETURN still functional

## Verified Stable APIs (1.21.8)

### ChatScreen
- `init()` — unchanged ✅
- `setInitialFocus()` — unchanged ✅
- `resize(MinecraftClient, int, int)` — unchanged ✅
- `removed()` — unchanged ✅
- `keyPressed(int, int, int)` — unchanged ✅
- `mouseClicked(double, double, int)` — unchanged ✅
- `mouseScrolled(double, double, double, double)` — unchanged ✅
- `insertText(String, boolean)` — unchanged ✅
- `render(DrawContext, int, int, float)` — unchanged ✅
- `chatField` (protected) — exists ✅
- `chatInputSuggestor` (package-private) — exists ✅

### ChatHud
- `render(DrawContext, int, int, int, boolean)` — unchanged ✅
- `addMessage(Text, MessageSignatureData, MessageIndicator)` — unchanged ✅
- `addMessage(ChatHudLine)` — unchanged ✅
- `refresh()` — unchanged ✅
- `clear(boolean)` — unchanged ✅
- `getWidth()` — unchanged ✅
- `getHeight()` — unchanged ✅
- `getLineHeight()` — unchanged ✅
- `getChatScale()` — unchanged ✅
- `getVisibleLineCount()` — unchanged ✅
- `toChatLineX(double)` — unchanged ✅
- `toChatLineY(double)` — unchanged ✅
- `getMessageLineIndex(double, double)` — exists ✅ (also `getMessageIndex`)
- `getIndicatorX(ChatHudLine.Visible)` — unchanged ✅
- `getTextStyleAt(double, double)` — unchanged ✅
- `mouseClicked(double, double)` — unchanged ✅
- `addToMessageHistory(String)` — unchanged ✅
- `getMessageHistory()` — unchanged ✅
- `scroll(int)` — unchanged ✅
- `resetScroll()` — unchanged ✅

### Keyboard
- `onKey(long, int, int, int, int)` — unchanged ✅

### DrawContext
- `drawTextWithShadow(TextRenderer, OrderedText, int, int, int)` → void ✅
- `drawText(TextRenderer, OrderedText, int, int, int, boolean)` → void ✅
- `drawText(TextRenderer, Text, int, int, int, boolean)` → void ✅
- `drawWrappedTextWithShadow(TextRenderer, StringVisitable, int, int, int, int)` → void ✅
- `drawSelection(int, int, int, int)` → void ✅ (NEW in 1.21.7)
- `drawBorder(int, int, int, int, int)` → void ✅
- `fill(int, int, int, int, int)` → void ✅
- `fill(RenderPipeline, int, int, int, int, int)` → void ✅
- `enableScissor(int, int, int, int)` → void ✅
- `disableScissor()` → void ✅
- `getMatrices()` → Matrix3x2fStack ✅

### TextFieldWidget
- `renderWidget(DrawContext, int, int, float)` — unchanged ✅
- `keyPressed(int, int, int)` — unchanged ✅
- `charTyped(char, int)` — unchanged ✅
- `onClick(double, double)` — unchanged ✅
- `write(String)` — unchanged ✅
- `setText(String)` — unchanged ✅
- All shadowed fields — present ✅

### ClientPlayNetworkHandler
- `onPlayerList(PlayerListS2CPacket)` — unchanged ✅
- `onPlayerRemove(PlayerRemoveS2CPacket)` — unchanged ✅

### TextRenderer.Drawer
- `accept(int, Style, int)` → boolean — unchanged ✅
- `x` (float) — exists ✅

### MinecraftClient
- `advanceValidatingTextRenderer` — exists ✅

### Screen
- `addDrawableChild(Element)` — exists ✅
- `addSelectableChild(Element & Selectable)` — exists ✅

## Dependency Changes

| Dependency | 1.21.6 | 1.21.8 |
|------------|--------|--------|
| Fabric Loader | 0.19.3 | 0.19.3 |
| Yarn mappings | 1.21.6+build.1 | 1.21.8+build.1 |
| Fabric API | 0.128.2+1.21.6 | 0.136.1+1.21.8 |
| owo-lib | 0.12.21+1.21.6 | 0.12.23+1.21.8 |
| ModMenu | 13.0.2 | 15.0.2 |
| Fabric Loom | 1.17-SNAPSHOT | 1.17.17 |
| Java | 21 | 21 |
