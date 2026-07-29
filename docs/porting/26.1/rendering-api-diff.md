# MC 26.1 Rendering API Migration Guide

Created: 2026-07-29 | Source JARs: `minecraft-clientOnly-44b7319af9-26.1-sources.jar`, `minecraft-common-44b7319af9-26.1-sources.jar`

---

## Executive Summary

MC 26.1 introduces a **render-graph-based immediate-mode replacement**. The old `DrawContext` (which directly drew to the framebuffer) is replaced by `GuiGraphicsExtractor`, which **records render commands** into a render-state graph for batched GPU execution. This is the single biggest paradigm shift:

- `DrawContext` → `GuiGraphicsExtractor` (render-state recorder, not direct drawer)
- `TextRenderer` → `Font` (renamed; similar semantics)
- `Screen.render(DrawContext, int, int, float)` → `Screen.extractRenderState(GuiGraphicsExtractor, int, int, float)`
- `TextFieldWidget` → `EditBox` (renamed; API changes)
- Tooltips are now **deferred** via `setTooltipForNextFrame(...)` instead of drawn inline
- GUI matrices are **2D-only** (`Matrix3x2fStack`) — no more 3D `MatrixStack`

---

## Class Renames

| Old Yarn Name | MC 26.1 Mojang Name | Package |
|---|---|---|
| `DrawContext` | `GuiGraphicsExtractor` | `net.minecraft.client.gui` |
| `TextRenderer` | `Font` | `net.minecraft.client.gui` |
| `TextFieldWidget` | `EditBox` | `net.minecraft.client.gui.components` |
| `OrderedText` | `FormattedCharSequence` | `net.minecraft.util` |
| `Text` / `LiteralText` | `Component` | `net.minecraft.network.chat` |
| `MatrixStack` | `Matrix3x2fStack` | `org.joml` |

---

## Detailed Method Mapping

### 1. `context.drawTextWithShadow(TextRenderer, OrderedText, int, int, int)`

**MC 26.1 Replacement:**
```java
// GuiGraphicsExtractor
public void text(Font font, FormattedCharSequence str, int x, int y, int color)
public void text(Font font, FormattedCharSequence str, int x, int y, int color, boolean dropShadow)
```

**Location:** `net/minecraft/client/gui/GuiGraphicsExtractor.java` lines 266-273

**Notes:**
- `OrderedText` → `FormattedCharSequence` (now `net.minecraft.util.FormattedCharSequence`)
- Drop shadow is `true` by default in the 5-arg overload
- The first parameter is now `Font` (not `TextRenderer`)
- Shadow color is automatically computed (25% of text color via `ARGB.scaleRGB(textColor, 0.25F)`)

**Migration example:**
```java
// Old:
context.drawTextWithShadow(textRenderer, orderedText, x, y, color);

// New:
graphics.text(font, formattedCharSequence, x, y, color); // shadow on by default
graphics.text(font, formattedCharSequence, x, y, color, false); // no shadow
```

---

### 2. `context.drawCenteredTextWithShadow(TextRenderer, Text, int, int, int)`

**MC 26.1 Replacement:**
```java
// GuiGraphicsExtractor
public void centeredText(Font font, Component text, int x, int y, int color)
public void centeredText(Font font, FormattedCharSequence text, int x, int y, int color)
public void centeredText(Font font, String str, int x, int y, int color)
```

**Location:** `net/minecraft/client/gui/GuiGraphicsExtractor.java` lines 284-295

**Notes:**
- Three overloads: `Component`, `FormattedCharSequence`, or `String`
- Centering is done by subtracting half the text width from x before calling `text()`
- Drop shadow is always on (calls `text()` with default `dropShadow=true`)
- Old `Text` (Yarn) → `Component` (Mojang)

**Migration example:**
```java
// Old:
context.drawCenteredTextWithShadow(textRenderer, text, centerX, y, color);

// New:
graphics.centeredText(font, component, centerX, y, color);
```

---

### 3. `context.drawTextWrapped(TextRenderer, Text, int, int, int, int)`

**MC 26.1 Replacement:**
```java
// GuiGraphicsExtractor
public void textWithWordWrap(Font font, FormattedText string, int x, int y, int width, int col)
public void textWithWordWrap(Font font, FormattedText string, int x, int y, int width, int col, boolean dropShadow)
```

**Location:** `net/minecraft/client/gui/GuiGraphicsExtractor.java` lines 297-305

**Notes:**
- `FormattedText` NOT `Component` — use `FormattedText` directly or convert via `Component.getVisualOrderText()`
- Internally calls `font.split(string, width)` then renders each line with 9px line height
- Drop shadow defaults to `true`

**Migration example:**
```java
// Old:
context.drawTextWrapped(textRenderer, text, x, y, maxWidth, color);

// New:
graphics.textWithWordWrap(font, formattedText, x, y, maxWidth, color);
graphics.textWithWordWrap(font, formattedText, x, y, maxWidth, color, false);
```

---

### 4. `context.drawTooltip(TextRenderer, Text, int, int)`

**MC 26.1 Replacement — DEFERRED TOOLTIPS:**
```java
// GuiGraphicsExtractor — simplest form:
public void setTooltipForNextFrame(Component component, int x, int y)

// With Font + lines:
public void setTooltipForNextFrame(Font font, List<? extends FormattedCharSequence> lines, int xo, int yo)

// With Font + Component:
public void setTooltipForNextFrame(Font font, Component text, int xo, int yo)

// Full control:
public void setTooltipForNextFrame(Font font, List<FormattedCharSequence> tooltip, ClientTooltipPositioner positioner, int xo, int yo, boolean replaceExisting)
```

**Location:** `net/minecraft/client/gui/GuiGraphicsExtractor.java` lines 1074-1164

**CRITICAL CHANGE:** Tooltips are no longer drawn inline. Instead, call `setTooltipForNextFrame(...)` during `extractRenderState()`. The tooltip is rendered as a **deferred element** in `extractDeferredElements()`, which is called automatically by Screen's `extractRenderStateWithTooltipAndSubtitles()`.

**Migration example:**
```java
// Old (in render method):
context.drawTooltip(textRenderer, text, mouseX, mouseY);

// New (in extractRenderState — set it, don't draw it):
graphics.setTooltipForNextFrame(component, mouseX, mouseY);

// Or with font control:
graphics.setTooltipForNextFrame(font, component, mouseX, mouseY);

// Or with pre-split lines:
List<FormattedCharSequence> lines = font.split(formattedText, maxWidth);
graphics.setTooltipForNextFrame(font, lines, mouseX, mouseY);
```

---

### 5. `context.fill(x, y, right, bottom, color)`

**MC 26.1 Replacement:**
```java
// GuiGraphicsExtractor
public void fill(int x0, int y0, int x1, int y1, int col)
public void fill(RenderPipeline pipeline, int x0, int y0, int x1, int y1, int col)
```

**Location:** `net/minecraft/client/gui/GuiGraphicsExtractor.java` lines 195-213

**Notes:**
- Same parameter order: `x0, y0, x1, y1, color`
- Default pipeline is `RenderPipelines.GUI`
- Still renders as a colored rectangle in the render-state graph
- The internal `innerFill` normalizes coordinates so `x0 < x1` and `y0 < y1`

**Migration example:**
```java
// Old:
context.fill(x, y, right, bottom, color);

// New:
graphics.fill(x, y, right, bottom, color);
```

---

### 6. `context.drawBorder(x, y, w, h, color)`

**MC 26.1 Replacement:**
```java
// GuiGraphicsExtractor
public void outline(int x, int y, int width, int height, int color)
```

**Location:** `net/minecraft/client/gui/GuiGraphicsExtractor.java` lines 223-228

**Notes:**
- Renamed from `drawBorder` to `outline`
- Parameter semantics: `x, y` = top-left, `width` and `height` are the OUTER dimensions
- Internally draws 4 thin `fill()` calls (1px each) — top, bottom, left, right edges
- Note: Unlike old `drawBorder` which drew inside the given bounds, `outline` draws on the OUTSIDE — the filled area is the perimeter, not the interior

**Migration example:**
```java
// Old:
context.drawBorder(x, y, width, height, color);

// New:
graphics.outline(x, y, width, height, color);
```

---

### 7. `context.getMatrices()`

**MC 26.1 Replacement:**
```java
// GuiGraphicsExtractor
public Matrix3x2fStack pose()
```

**Location:** `net/minecraft/client/gui/GuiGraphicsExtractor.java` lines 150-152

**CRITICAL CHANGE:** The return type is `Matrix3x2fStack` (from JOML `org.joml`), not the old 3D `MatrixStack`. GUI rendering is now **purely 2D affine** — no 3D projection matrix, no model-view stack. This means:
- `.pushMatrix()` → `.pushMatrix()` (same)
- `.popMatrix()` → `.popMatrix()` (same)
- `.translate(x, y, z)` → `.translate(x, y)` — **no Z parameter**
- `.scale(x, y, z)` → `.scale(x, y)` — **no Z parameter**
- `.peek().getPositionMatrix()` → `.peek()` but returns `Matrix3x2f` (2D)
- Old `Matrix4f` operations are now `Matrix3x2f`

**Migration example:**
```java
// Old:
MatrixStack matrices = context.getMatrices();
matrices.push();
matrices.translate(x, y, 0);
// ... draw ...
matrices.pop();

// New:
Matrix3x2fStack pose = graphics.pose();
pose.pushMatrix();
pose.translate(x, y);
// ... draw ...
pose.popMatrix();
```

---

### 8. `Screen.hasShiftDown()`

**MC 26.1 Replacement — REMOVED AS STATIC METHOD:**
```java
// InputWithModifiers (interface implemented by KeyEvent)
default boolean hasShiftDown()
```

**Location:** `net/minecraft/client/input/InputWithModifiers.java`

**CRITICAL CHANGE:** `Screen.hasShiftDown()` and `Screen.hasControlDown()` are no longer static methods on `Screen`. They are now **instance methods on `KeyEvent`** (and `MouseButtonEvent`) via the `InputWithModifiers` interface.

**Migration example:**
```java
// Old:
if (Screen.hasShiftDown()) { ... }

// New — in keyPressed(KeyEvent event):
if (event.hasShiftDown()) { ... }

// New — in mouseClicked(MouseButtonEvent event):
if (event.hasShiftDown()) { ... }
```

---

### 9. `Screen.hasControlDown()`

**MC 26.1 Replacement — REMOVED AS STATIC METHOD:**
```java
// InputWithModifiers (interface implemented by KeyEvent)
default boolean hasControlDown()
default boolean hasControlDownWithQuirk()  // accounts for macOS Cmd vs Ctrl
```

**Location:** `net/minecraft/client/input/InputWithModifiers.java`

**Notes:**
- Use `hasControlDownWithQuirk()` for keyboard shortcuts (Select All, Copy, Paste, Cut) — it respects `EDIT_SHORTCUT_KEY_MODIFIER` which maps to Cmd on macOS
- Use `hasControlDown()` for the literal Ctrl key state

**Migration example:**
```java
// Old:
if (Screen.hasControlDown()) { ... }

// New — in keyPressed(KeyEvent event):
if (event.hasControlDown()) { ... }
// For edit shortcuts (respects macOS Cmd key):
if (event.hasControlDownWithQuirk()) { ... }
```

---

### 10. `Screen.render(DrawContext, int, int, float)`

**MC 26.1 Replacement:**
```java
// Screen (implements Renderable)
public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
```

**Location:** `net/minecraft/client/gui/screens/Screen.java` line 120

**CRITICAL CHANGE — Render-State Paradigm:** The `render` method is gone. MC 26.1 implements a **render graph** where:
1. `extractRenderState()` collects graphical commands (no actual GPU work)
2. The renderer batches and executes all collected state at the end of the frame

The Screen lifecycle entry point is now:
```java
// Called by Minecraft master render loop:
public final void extractRenderStateWithTooltipAndSubtitles(
    GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
```

This method:
1. Calls `nextStratum()` — starts a new render layer
2. Calls `extractBackground(graphics, mouseX, mouseY, a)` — background stratum
3. Calls `nextStratum()` — foreground layer
4. Calls `extractRenderState(graphics, mouseX, mouseY, a)` — your screen content
5. Calls `extractDeferredElements(mouseX, mouseY, a)` — tooltips, preedit overlays

**Migration example:**
```java
// Old:
@Override
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    context.fill(0, 0, this.width, this.height, 0x80000000);
    context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    super.render(context, mouseX, mouseY, delta);
}

// New:
@Override
public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    graphics.fill(0, 0, this.width, this.height, 0x80000000);
    graphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    super.extractRenderState(graphics, mouseX, mouseY, delta);
}
```

---

### 11. `TextFieldWidget.write(String)`

**MC 26.1 Replacement:**
```java
// EditBox
public void insertText(String input)
```

**Location:** `net/minecraft/client/gui/components/EditBox.java` line 131

**Notes:**
- Inserts text at the cursor position (replacing any selection)
- Sanitizes input via `StringUtil.filterText(input)`
- Handles surrogate pairs
- Respects `maxLength`
- `write(String)` → `insertText(String)` — naming change only, semantics are the same

**Migration example:**
```java
// Old:
textField.write("hello");

// New:
editBox.insertText("hello");
```

---

### 12. `TextFieldWidget.getText()`

**MC 26.1 Replacement:**
```java
// EditBox
public String getValue()
```

**Location:** `net/minecraft/client/gui/components/EditBox.java` line 109

**Migration example:**
```java
// Old:
String text = textField.getText();

// New:
String text = editBox.getValue();
```

---

### 13. `TextFieldWidget.setText(String)`

**MC 26.1 Replacement:**
```java
// EditBox
public void setValue(String value)
```

**Location:** `net/minecraft/client/gui/components/EditBox.java` line 97

**Notes:**
- Truncates to `maxLength` if the value exceeds it
- Moves cursor to end after setting
- Fires `responder` callback
- `setText(String)` → `setValue(String)` — naming change

**Migration example:**
```java
// Old:
textField.setText("default");

// New:
editBox.setValue("default");
```

---

### 14. `TextRenderer.getWidth(String)`

**MC 26.1 Replacement:**
```java
// Font
public int width(String str)
public int width(FormattedText text)
public int width(FormattedCharSequence text)
```

**Location:** `net/minecraft/client/gui/Font.java` lines 185-195

**Notes:**
- Three overloads for `String`, `FormattedText`, and `FormattedCharSequence`
- Uses `Mth.ceil(splitter.stringWidth(...))` internally for integer result

**Migration example:**
```java
// Old:
int w = textRenderer.getWidth("hello");

// New:
int w = font.width("hello");
```

---

### 15. `TextRenderer.fontHeight`

**MC 26.1 Replacement:**
```java
// Font
public final int lineHeight = 9;
```

**Location:** `net/minecraft/client/gui/Font.java` line 37

**Notes:**
- Changed from `fontHeight` to `lineHeight`
- Value is still `9` (hardcoded)
- `public final` field, not a method

**Migration example:**
```java
// Old:
int h = textRenderer.fontHeight;

// New:
int h = font.lineHeight;
```

---

### 16. `TextRenderer.getTextHandler()`

**MC 26.1 Replacement:**
```java
// Font
public StringSplitter getSplitter()
```

**Location:** `net/minecraft/client/gui/Font.java` lines 225-227

**Notes:**
- Returns `StringSplitter` instead of the old `TextHandler`
- `StringSplitter` provides: `stringWidth()`, `splitLines()`, `plainHeadByWidth()`, `plainTailByWidth()`, `headByWidth()`

**Migration example:**
```java
// Old:
TextHandler handler = textRenderer.getTextHandler();
float width = handler.getWidth(text);

// New:
StringSplitter splitter = font.getSplitter();
// Use Font.width() directly for most cases — getSplitter() is for advanced use
```

---

## Additional Important Changes

### Render Pipelines

`GuiGraphicsExtractor` methods often accept a `RenderPipeline` parameter:
```java
RenderPipelines.GUI              // Default GUI pipeline
RenderPipelines.GUI_TEXTURED     // For textured blits (sprites, images)
RenderPipelines.GUI_INVERT       // For inverting text color on highlight
RenderPipelines.GUI_TEXT_HIGHLIGHT // For text selection highlights
```

### Stratums (Render Layers)

The new render graph uses "stratums" (layers) for compositing:
```java
graphics.nextStratum();         // Start a new render layer
graphics.blurBeforeThisStratum(); // Apply background blur
graphics.enableScissor(x, y, x2, y2); // Clip rendering
graphics.disableScissor();      // Remove clip
```

### Scissor Stack

Scissor rectangles are now managed as a stack (supports nested clipping):
```java
public final GuiGraphicsExtractor.ScissorStack scissorStack;  // public field
graphics.enableScissor(x0, y0, x1, y1);
graphics.disableScissor();
boolean inBounds = graphics.containsPointInScissor(mx, my);
```

### Matrix Transform Differences

| Old (MatrixStack 3D) | New (Matrix3x2fStack 2D) |
|---|---|
| `matrices.push()` | `pose.pushMatrix()` |
| `matrices.pop()` | `pose.popMatrix()` |
| `matrices.translate(x, y, z)` | `pose.translate(x, y)` |
| `matrices.scale(x, y, z)` | `pose.scale(x, y)` |
| `matrices.peek().getPositionMatrix()` | `pose.peek()` (returns `Matrix3x2f`) |
| `Matrix4f` | `Matrix3x2f` |

### Blit / Sprite Rendering

The old `drawTexture()` method family is replaced by `blitSprite()`:
```java
// Sprite-based drawing
graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier, x, y, width, height);
graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier, x, y, width, height, color);

// Full blit with UV
graphics.blit(RenderPipeline, Identifier, x, y, u, v, width, height, texW, texH);
```

### Horizontal/Vertical Lines

```java
graphics.horizontalLine(x0, x1, y, color);
graphics.verticalLine(x, y0, y1, color);
```

### Item Rendering

```java
graphics.item(itemStack, x, y);
graphics.item(itemStack, x, y, seed);
graphics.fakeItem(itemStack, x, y);      // No cooldown/durability bar
graphics.itemDecorations(font, itemStack, x, y);
```

### Cursor Control

```java
graphics.requestCursor(CursorTypes.IBEAM);       // Text cursor
graphics.requestCursor(CursorTypes.POINTING_HAND); // Clickable cursor
graphics.requestCursor(CursorTypes.NOT_ALLOWED);  // Disabled cursor
```

### Text Color Constants (ARGB)

MC 26.1 uses `ARGB` utility class for color manipulation:
```java
ARGB.color(alpha, rgb);      // Pack ARGB
ARGB.opaque(rgb);            // Full opacity
ARGB.white(alpha);           // White with alpha
ARGB.black(alpha);           // Black with alpha
ARGB.alpha(color);           // Extract alpha
ARGB.alphaFloat(color);      // Alpha as float 0..1
ARGB.multiply(a, b);         // Multiply colors
ARGB.scaleRGB(color, factor); // Scale RGB channels
```

### Font Drawing

The Font class also provides lower-level drawing (for non-graph-extractor use):
```java
font.drawInBatch(String str, float x, float y, int color, boolean dropShadow,
    Matrix4fc pose, MultiBufferSource bufferSource, Font.DisplayMode displayMode,
    int backgroundColor, int packedLightCoords);

font.drawInBatch(FormattedCharSequence str, ...);  // same params
font.drawInBatch(Component str, ...);              // same params
font.drawInBatch8xOutline(FormattedCharSequence str, float x, float y,
    int color, int outlineColor, Matrix4fc pose,
    MultiBufferSource bufferSource, int packedLightCoords);
```

---

## Quick Reference Cheat Sheet

| # | Old Yarn Call | MC 26.1 Replacement |
|---|---|---|
| 1 | `context.drawTextWithShadow(TextRenderer, OrderedText, int, int, int)` | `graphics.text(Font, FormattedCharSequence, int, int, int)` |
| 2 | `context.drawCenteredTextWithShadow(TextRenderer, Text, int, int, int)` | `graphics.centeredText(Font, Component, int, int, int)` |
| 3 | `context.drawTextWrapped(TextRenderer, Text, int, int, int, int)` | `graphics.textWithWordWrap(Font, FormattedText, int, int, int, int)` |
| 4 | `context.drawTooltip(TextRenderer, Text, int, int)` | `graphics.setTooltipForNextFrame(Component, int, int)` ⚠️ deferred |
| 5 | `context.fill(x, y, right, bottom, color)` | `graphics.fill(int, int, int, int, int)` |
| 6 | `context.drawBorder(x, y, w, h, color)` | `graphics.outline(int, int, int, int, int)` |
| 7 | `context.getMatrices()` | `graphics.pose()` → `Matrix3x2fStack` ⚠️ 2D only |
| 8 | `Screen.hasShiftDown()` | `event.hasShiftDown()` on `KeyEvent` ⚠️ instance method |
| 9 | `Screen.hasControlDown()` | `event.hasControlDown()` / `event.hasControlDownWithQuirk()` ⚠️ instance method |
| 10 | `Screen.render(DrawContext, int, int, float)` | `Screen.extractRenderState(GuiGraphicsExtractor, int, int, float)` |
| 11 | `TextFieldWidget.write(String)` | `EditBox.insertText(String)` |
| 12 | `TextFieldWidget.getText()` | `EditBox.getValue()` |
| 13 | `TextFieldWidget.setText(String)` | `EditBox.setValue(String)` |
| 14 | `TextRenderer.getWidth(String)` | `Font.width(String)` |
| 15 | `TextRenderer.fontHeight` | `Font.lineHeight` (= 9) |
| 16 | `TextRenderer.getTextHandler()` | `Font.getSplitter()` → `StringSplitter` |
