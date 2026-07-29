# API Diff: Minecraft 1.21.1 → 1.21.7 (Fabric / Yarn)

## Overview

| Item | 1.21.1 (Baseline) | 1.21.7 (Target) |
|------|-------------------|-----------------|
| Yarn mappings | 1.21.1+build.3 | 1.21.7+build.8 |
| Fabric Loader | 0.19.3 | 0.19.3 |
| Fabric API | 0.116.14+1.21.1 | 0.129.0+1.21.7 |
| Java | 21 | 21 |

## Critical Breaking Changes

### 1. ChatHud.render — Lambda Refactor 🔴 CRITICAL

**Status**: BREAKING

The `render` method has been completely refactored. Instead of a flat procedural render with individual `context.fill()` and `context.drawTextWithShadow()` calls, the method now uses a helper `method_71990` that takes lambdas:

```java
// 1.21.7
this.method_71990(lineCount, currentTick, focused, yOffset, (x, y, width, line, index, opacity) -> {
    context.fill(x - 4, y, x + width + 8, y + height, ...);
    // indicator handling
});

this.method_71990(lineCount, currentTick, focused, yOffset, (x, y, width, line, index, opacity) -> {
    context.drawTextWithShadow(renderer, line.content(), x, y, color);
});
```

**Impact on ChatHudMixin**:
- `@WrapOperation` with `@Local` inside `render` method CANNOT capture variables from inside lambdas
- The `fill(IIIII)V` and `drawTextWithShadow` INVOKE instructions are now in synthetic lambda methods (`$render$lambda$0`, etc.), NOT in `render` directly
- All `@WrapOperation` annotations on `ChatHud.render` that target `fill` or `drawTextWithShadow` will fail at runtime with `InvalidInjectionException`

**Affected Mixins**:
- `ChatHudMixin.chat_canvas$drawCompactMessageBackground` — `@WrapOperation` on `fill(IIIII)V` ordinal 0
- `ChatHudMixin.chat_canvas$drawCompactIndicatorBackground` — `@WrapOperation` on `fill(IIIII)V` ordinal 1
- `ChatHudMixin.chat_canvas$drawConfiguredChatLine` — `@WrapOperation` on `drawTextWithShadow(TextRenderer, OrderedText, int, int, int)`
- `ChatHudMixin.chat_canvas$drawConfiguredQueueText` — `@WrapOperation` on `drawTextWithShadow(TextRenderer, Text, int, int, int)`

**Mitigation**: Since `DualChatHudRenderer.render()` already cancels the vanilla render when active (via `ci.cancel()` at HEAD), these `@WrapOperation` annotations can be removed. They only affected the fallback path (Chat Canvas configured but dual renderer inactive).

### 2. DrawContext.drawTextWithShadow — Return Type Change 🟡 MODERATE

**Status**: BREAKING for callers that capture return value

```java
// 1.21.1
public int drawTextWithShadow(TextRenderer renderer, String text, int x, int y, int color)

// 1.21.7
public void drawTextWithShadow(TextRenderer renderer, String text, int x, int y, int color)
```

All `drawTextWithShadow` and `drawText` overloads now return `void`.

**Impact**: Any code that captures the return value (`int width = context.drawTextWithShadow(...)`) will fail to compile. The `@WrapOperation` with `Operation<Integer>` for `drawTextWithShadow` will have mismatched return types.

**New methods added**:
- `drawWrappedTextWithShadow` — replaces `drawTextWrapped` (which was in 1.21.1)
- `drawWrappedText` — new, with boolean shadow parameter

**Critical pitfall — Alpha channel check**: `DrawContext.drawText()` now checks `ColorHelper.getAlpha(color) != 0` before rendering. Colors without explicit alpha (like `0xFFFFFF`, which has alpha=0x00) are silently dropped. All text colors must include the alpha byte: `0xFFFFFFFF` (not `0xFFFFFF`).

### 3. DrawContext.fill — RenderPipeline Parameter 🟡 MODERATE

**Status**: Compatible (5-param overload preserved)

```java
// 1.21.7 (new overload)
public void fill(RenderPipeline pipeline, int x1, int y1, int x2, int y2, int color)

// 1.21.7 (preserved)
public void fill(int x1, int y1, int x2, int y2, int color) {
    this.fill(RenderPipelines.GUI, x1, y1, x2, y2, color);
}
```

The old 5-parameter `fill` is still available as a convenience method. However, `RenderLayer.getGuiOverlay()` has been replaced by `RenderPipelines.GUI`.

**Impact on TextFieldWidgetMixin**: `context.fill(RenderLayer.getGuiOverlay(), ...)` needs to change to `context.fill(RenderPipelines.GUI, ...)` or simply use the 5-param `fill(int, int, int, int, int)`.

### 4. Import Changes 🟢 MINOR

| 1.21.1 Import | 1.21.7 Import |
|---------------|---------------|
| `net.minecraft.client.render.RenderLayer` | `com.mojang.blaze3d.pipeline.RenderPipeline` + `net.minecraft.client.gl.RenderPipelines` |

## Unchanged (Compatible)

### ChatScreen
- `init()`, `keyPressed(int, int, int)`, `charTyped(char, int)`, `mouseClicked(double, double, int)`, `mouseScrolled(double, double, double, double)`, `render(DrawContext, int, int, float)`, `removed()`, `resize(MinecraftClient, int, int)` — ALL unchanged

### Keyboard
- `onKey(long window, int key, int scancode, int action, int modifiers)` — UNCHANGED

### ClientPlayNetworkHandler
- `onPlayerList(PlayerListS2CPacket)` — UNCHANGED
- `onPlayerRemove(PlayerRemoveS2CPacket)` — UNCHANGED

### ChatHud
- `addMessage(Text, MessageSignatureData, MessageIndicator)` — UNCHANGED
- `addMessage(ChatHudLine)` — UNCHANGED
- `clear(boolean)` — UNCHANGED
- `refresh()` — UNCHANGED
- `getWidth()`, `getHeight()`, `getChatScale()`, `getLineHeight()` — UNCHANGED
- `toChatLineX(double)`, `toChatLineY(double)` — UNCHANGED
- `getMessageLineIndex(double, double)` — STILL EXISTS (private, wraps getMessageIndex)
- `getMessageIndex(double, double)` — NEW (private, wraps getMessageLineIndex)

### ChatInputSuggestor
- `show()`, `renderMessages()` — referenced fields UNCHANGED
- `showCommandSuggestions()` — UNCHANGED
- `provideRenderText()` — UNCHANGED

### TextFieldWidget
- `renderWidget(DrawContext, int, int, float)` — UNCHANGED
- `onClick(double, double)`, `keyPressed(int, int, int)`, `write(String)` — UNCHANGED
- `setText(String)`, `updateFirstCharacterIndex(int)`, `getCharacterX(int)` — UNCHANGED

### TextRenderer
- `TextRenderer$Drawer.accept(int, Style, int)` — UNCHANGED (target of TextRendererDrawerMixin)

## Dependency Changes

| Dependency | 1.21.1 | 1.21.7 | Notes |
|------------|--------|--------|-------|
| Fabric Loader | 0.19.3 | 0.19.3 | Same |
| Fabric API | 0.116.14+1.21.1 | 0.129.0+1.21.7 | Latest for 1.21.7 |
| owo-lib | 0.12.15.4+1.21 | 0.12.21+1.21.6 | Closest stable; no 1.21.7-specific build |
| ModMenu | 11.0.4 | 11.0.4 | Same (compatible) |
| Vosk | 0.3.45 | 0.3.45 | Same |
| JUnit | 5.11.4 | 5.11.4 | Same |

## Summary of Required Changes

1. **ChatHudMixin**: Remove 4 `@WrapOperation` annotations on `render` method (fill ordinals 0/1, drawTextWithShadow x2). The `@Inject` at HEAD/TAIL and `@ModifyReturnValue`/`@ModifyArg` annotations remain.

2. **TextFieldWidgetMixin**: Change `RenderLayer.getGuiOverlay()` → use 5-param `fill(int, int, int, int, int)` or `RenderPipelines.GUI`.

3. **Anywhere drawTextWithShadow return value is used**: Change from capturing `int` result to ignoring `void` return.

4. **Import cleanup**: Replace `RenderLayer` imports with no imports needed (if using 5-param fill).
