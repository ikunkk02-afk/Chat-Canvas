# API Diff: Minecraft 1.21.8 → 1.21.9 (Fabric / Yarn)

## Overview
Minor point release with input API refactoring. Zero classes added/removed.
Only 3 registry entries added (debug_subscription, incoming_rpc_methods, outgoing_rpc_methods).

## Critical API Changes

### 1. Keyboard.onKey — Signature Refactor 🔴
| 1.21.8 | 1.21.9 |
|--------|--------|
| `public void onKey(long window, int key, int scancode, int action, int modifiers)` | `private void onKey(long window, int action, KeyInput input)` |
- Visibility: `public` → `private`
- Parameters condensed into `KeyInput` wrapper

### 2. Keyboard.onChar — Signature Refactor
| 1.21.8 | 1.21.9 |
|--------|--------|
| `private void onChar(long window, int codePoint, int modifiers)` | `private void onChar(long window, CharInput input)` |

### 3. Keyboard.setup — Signature Change
| 1.21.8 | 1.21.9 |
|--------|--------|
| `public void setup(long window)` | `public void setup(Window window)` |

### 4. Element / ParentElement / Screen — Input Method Refactor 🔴
All input methods consolidated into Click, KeyInput, CharInput wrappers:

| Method | 1.21.8 | 1.21.9 |
|--------|--------|--------|
| `keyPressed` | `(int keyCode, int scanCode, int modifiers)` | `(KeyInput input)` |
| `keyReleased` | `(int keyCode, int scanCode, int modifiers)` | `(KeyInput input)` |
| `charTyped` | `(char chr, int modifiers)` | `(CharInput input)` |
| `mouseClicked` | `(double mouseX, double mouseY, int button)` | `(Click click, boolean doubled)` |
| `mouseReleased` | `(double mouseX, double mouseY, int button)` | `(Click click)` |
| `mouseDragged` | `(double mouseX, double mouseY, int button, double deltaX, double deltaY)` | `(Click click, double offsetX, double offsetY)` |
| `mouseScrolled` | `(double mouseX, double mouseY, double horizontal, double vertical)` | Unchanged |

### 5. KeyInput API
```java
public record KeyInput(int key, int scancode, int modifiers) {
    public boolean hasShift() { ... }
    public boolean hasCtrl() { ... }
    public boolean hasAlt() { ... }
    public boolean isEnter() { ... }
    public boolean isEscape() { ... }
    public boolean isCopy/Paste/Cut/SelectAll() { ... }
}
```

### 6. CharInput API
```java
public record CharInput(int codepoint, int modifiers) {
    public boolean isValidChar() { ... }
    public String asString() { ... }
}
```

### 7. Click API
```java
public record Click(int button, double x, double y, ...) { }
```

### 8. ChatScreen Changes
- `init()`: uses `this.addDrawableChild(this.chatField)` 
- TextFieldWidget created with `client.advanceValidatingTextRenderer` (not textRenderer)

### 9. Unchanged Methods ✅
- `ChatHud.render(DrawContext, int, int, int, boolean)` — same signature
- `ChatHud.addMessage(Text, MessageSignatureData, MessageIndicator)` — same
- `ChatHud.addVisibleMessage(ChatHudLine)` — same
- `ChatHud.getTextStyleAt(double, double)` — same
- `ChatHud.getWidth/getHeight/getChatScale/getLineHeight` — same
- `DrawContext.fill(IIIII)` — same
- `DrawContext.drawTextWithShadow/drawText/drawWrappedTextWithShadow` — same (return void)
- `DrawContext.drawSelection(int, int, int, int)` — exists ✅
- `RenderPipelines.GUI` — same
- `Matrix3x2fStack.pushMatrix/popMatrix/translate/scale` — same
- `Screen.addSelectableChild/addDrawableChild` — both exist ✅
- `TextFieldWidget.setMaxLength/setDrawsBackground/setFocusUnlocked` — same

## Mixins Affected

| Mixin | Change Required | Status |
|-------|----------------|--------|
| KeyboardMixin | `onKey(long,int,int,int,int)` → `onKey(long,int,KeyInput)` | ✅ Fixed |
| ChatScreenMixin.keyPressed | `(int,int,int)` → `(KeyInput)` | ✅ Fixed |
| ChatScreenMixin.mouseClicked | `(double,double,int)` → `(Click,boolean)` | ✅ Fixed |
| TextFieldWidgetMixin.keyPressed | `(int,int,int)` → `(KeyInput)` | ✅ Fixed |
| TextFieldWidgetMixin.onClick | `(double,double)` → `(Click,boolean)` | ✅ Fixed |
| AbstractParentElementMixin (3 methods) | All → Click/CharInput | ✅ Fixed |
| ChatHudMixin | No changes needed | ✅ |
| ClientPlayNetworkHandlerMixin | No changes needed | ✅ |
| ChatInputSuggestorMixin | No changes needed | ✅ |
| TextRendererDrawerMixin | No changes needed | ✅ |
| ScreenAccessor | `addSelectableChild` still exists | ✅ |
| ChatHudAccessor | No changes needed | ✅ |
| TextFieldWidgetAccessor | `selectionEnd` field still exists | ✅ |
| Other accessors | No changes needed | ✅ |

## Registry Additions (no impact)
- `minecraft:debug_subscription` — entries, protocol_id
- `minecraft:incoming_rpc_methods` — entries, protocol_id
- `minecraft:outgoing_rpc_methods` — entries, protocol_id
