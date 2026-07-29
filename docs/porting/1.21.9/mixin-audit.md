# Mixin Audit: Minecraft 1.21.8 → 1.21.9 (Fabric / Yarn)

## Overview
15 Mixins audited against MC 1.21.9 Yarn-mapped sources.  
Source: MCP decompiled `chat-canvas-template-1.21.9/.gradle/loom-cache/minecraftMaven/`  
Branch: `mc/1.21.9` from baseline `mc/1.21.8` (tag `v1.2.0-mc1.21.8`)

## Audit Results Summary

| Mixin | Target Class | Method(s) | Status |
|-------|-------------|-----------|--------|
| ChatScreenMixin | ChatScreen | init, keyPressed, mouseClicked, render, removed, etc. | ✅ Fixed (keyPressed→KeyInput, mouseClicked→Click) |
| ChatHudMixin | ChatHud | render, addMessage, getTextStyleAt, etc. | ✅ No changes needed |
| KeyboardMixin | Keyboard | onKey | ✅ Fixed (onKey(long,int,int,int,int)→onKey(long,int,KeyInput), public→private) |
| ClientPlayNetworkHandlerMixin | ClientPlayNetworkHandler | onPlayerList, onPlayerRemove | ✅ No changes needed |
| TextFieldWidgetMixin | TextFieldWidget | keyPressed, onClick, write, renderWidget, etc. | ✅ Fixed (keyPressed→KeyInput, onClick→Click) |
| AbstractParentElementMixin | ParentElement | mouseDragged, mouseReleased, charTyped | ✅ Fixed (all→Click/CharInput) |
| ChatInputSuggestorMixin | ChatInputSuggestor | show, showCommandSuggestions, provideRenderText | ✅ No changes needed |
| TextRendererDrawerMixin | TextRenderer$Drawer | accept | ✅ No changes needed |
| ChatHudAccessor | ChatHud | refresh | ✅ No changes needed |
| ChatInputSuggestorAccessor | ChatInputSuggestor | window | ✅ No changes needed |
| ChatScreenAccessor | ChatScreen | chatField | ✅ No changes needed |
| ScreenAccessor | Screen | addSelectableChild | ✅ Still exists in 1.21.9 |
| SuggestionWindowAccessor | ChatInputSuggestor.SuggestionWindow | area | ✅ No changes needed |
| TextFieldWidgetAccessor | TextFieldWidget | maxLength, selectionEnd | ✅ Fields still exist |
| TextRendererAccessor | TextRenderer | fontHeight | ✅ No changes needed |

## Detailed Changes

### 1. KeyboardMixin (`onKey`)
- **1.21.8**: `public void onKey(long window, int key, int scancode, int action, int modifiers)`
- **1.21.9**: `private void onKey(long window, int action, KeyInput input)`
- **Fix**: Changed mixin to accept `(long, int, KeyInput)` and extract key/scanCode via `input.key()` and `input.scancode()`
- **Verification**: MCP decompiled source confirms new signature

### 2. ChatScreenMixin (`keyPressed`)
- **1.21.8**: `keyPressed(int keyCode, int scanCode, int modifiers)`
- **1.21.9**: `keyPressed(KeyInput input)`
- **Fix**: Accept `KeyInput`, extract via `input.key()`, `input.scancode()`, `input.modifiers()`

### 3. ChatScreenMixin (`mouseClicked`)
- **1.21.8**: `mouseClicked(double mouseX, double mouseY, int button)`
- **1.21.9**: `mouseClicked(Click click, boolean doubled)`
- **Fix**: Accept `(Click, boolean)`, extract via `click.x()`, `click.y()`, `click.button()`

### 4. TextFieldWidgetMixin (`keyPressed`, `onClick`)
- **1.21.8**: `keyPressed(int, int, int)`, `onClick(double, double)`
- **1.21.9**: `keyPressed(KeyInput)`, `onClick(Click, boolean)`
- **Fix**: Same extraction pattern as above

### 5. AbstractParentElementMixin (`mouseDragged`, `mouseReleased`, `charTyped`)
- All three methods changed from primitive parameters to Click/CharInput wrappers
- **Fix**: Accept new wrapper types, extract primitives inline

### 6. Non-Mixin Code
- **ChatCanvasEditorScreen**: keyPressed, mouseClicked, mouseDragged, mouseReleased → updated to KeyInput/Click
- **Screen.hasShiftDown/hasControlDown** → `MinecraftClient.getInstance().isShiftPressed()/isCtrlPressed()`
- **Screen.isSelectAll/isPaste/isCopy/isCut** → `KeyInput.isSelectAll/isPaste/isCopy/isCut`
- **Style.DEFAULT_FONT_ID** → `MinecraftClient.DEFAULT_FONT_ID`
- **GameProfile.getName()/getId()** → `name()/id()` (record accessors in 1.21.9 authlib)
- **DrawContext.drawBorder()** → replaced with custom fill-based border helper
- **Component.zIndex()** → owo-lib API change

## Unchanged Methods (Verified)
- `ChatHud.render(DrawContext, int, int, int, boolean)` — same signature across 1.21.8→9
- `ChatHud.addMessage(Text, MessageSignatureData, MessageIndicator)` — same
- `ChatHud.getTextStyleAt/getWidth/getHeight/getChatScale` — same
- `DrawContext.fill(IIIII)` — same (5-arg overload still exists)
- `DrawContext.drawTextWithShadow/drawText` — same (return void in 1.21.6+)
- `Matrix3x2fStack.pushMatrix/popMatrix/translate/scale` — same
- `RenderPipelines.GUI` — same
- `TextFieldWidget` API — keyPressed/charTyped/onClick changed; fields unchanged
