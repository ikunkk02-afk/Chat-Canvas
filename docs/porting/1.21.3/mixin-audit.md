# Mixin Audit: Chat Canvas 1.21.1 → 1.21.3

## Audit Date: 2026-08-31
## Source: MC MCP (yarn mappings, 1.21.3 decompiled)

---

## 1. ChatScreenMixin

| Field | Value |
|-------|-------|
| **Target class** | `net.minecraft.client.gui.screen.ChatScreen` |
| **Status** | ✅ VERIFIED — No changes needed |

### Injection Points

| Method | At | Descriptor | Status |
|--------|-----|-----------|--------|
| `init` | RETURN | `()V` | ✅ Identical |
| `setInitialFocus` | RETURN | `()V` | ✅ Identical |
| `resize` | HEAD | `(Lnet/minecraft/client/MinecraftClient;II)V` | ✅ Identical |
| `render` | HEAD | `(Lnet/minecraft/client/gui/DrawContext;IIF)V` | ✅ Identical |
| `render` | RETURN | `(Lnet/minecraft/client/gui/DrawContext;IIF)V` | ✅ Identical |
| `mouseClicked` | HEAD (cancellable) | `(DDI)Z` | ✅ Identical |
| `mouseScrolled` | HEAD (cancellable) | `(DDDD)Z` | ✅ Identical |
| `removed` | HEAD | `()V` | ✅ Identical |
| `keyPressed` | HEAD (cancellable) | `(III)Z` | ✅ Identical |
| `insertText` | HEAD (cancellable) | `(Ljava/lang/String;Z)V` | ✅ Identical |

### WrapOperation

| Target | ordinal | Status |
|--------|---------|--------|
| `DrawContext.fill(IIIII)V` in `render` | 0 | ✅ Identical |

### Shadow Fields

| Field | Type | Status |
|-------|------|--------|
| `chatField` | `TextFieldWidget` | ✅ Present, same type |
| `chatInputSuggestor` | `ChatInputSuggestor` | ✅ Present, same type |

### ChatCanvasVoiceShortcutHost Interface

- `chat_canvas$onVoiceShortcutReleased(int, int)` — implemented in ChatScreenMixin ✅
- Delegated from `KeyboardMixin` via `instanceof ChatCanvasVoiceShortcutHost` ✅

---

## 2. KeyboardMixin

| Field | Value |
|-------|-------|
| **Target class** | `net.minecraft.client.Keyboard` |
| **Target method** | `onKey` |
| **Descriptor** | `(JIIII)V` |
| **At** | `TAIL` |
| **Status** | ✅ VERIFIED — Signature identical to 1.21.1 |

Verified from MCP decompiled source:
```java
public void onKey(long window, int key, int scancode, int action, int modifiers)
```

### Rationale for Keyboard.onKey over ChatScreen.keyReleased

`keyReleased` is declared in `net.minecraft.client.gui.Element` as a default interface method.
`ChatScreen` does **not** declare `keyReleased` directly — it inherits from `Screen` → `Element`.
Mixin `@Inject` on `ChatScreen.keyReleased` would fail with `InvalidInjectionException`.
The `Keyboard.onKey` GLFW-level approach is correct and verified working.

---

## 3. ChatHudMixin

| Field | Value |
|-------|-------|
| **Target class** | `net.minecraft.client.gui.hud.ChatHud` |
| **Status** | ✅ VERIFIED — No changes needed |

### Injection Points

| Method | At | Status |
|--------|-----|--------|
| `render` | HEAD (cancellable) | ✅ Identical |
| `render` | RETURN | ✅ Identical |
| `getTextStyleAt` | HEAD (cancellable) | ✅ Identical |
| `getIndicatorX` | HEAD (cancellable) | ✅ Identical |
| `refresh` | HEAD | ✅ Identical |
| `clear` | HEAD | ✅ Identical |
| `addMessage(Text, MessageSignatureData, MessageIndicator)` | HEAD | ✅ Identical |
| `addMessage(ChatHudLine)` | RETURN | ✅ Identical |

### WrapOperations

| Target | ordinal | Status |
|--------|---------|--------|
| `DrawContext.fill(IIIII)V` in `render` | 0 | ✅ Identical |
| `DrawContext.fill(IIIII)V` in `render` | 1 | ✅ Identical |
| `ChatMessages.breakRenderedChatMessageLines` in `addVisibleMessage` | — | ✅ Identical |
| `DrawContext.drawTextWithShadow` in `render` | — | ✅ Identical |
| `DrawContext.drawTextWithShadow(Text)` in `render` | — | ✅ Identical |

### ModifyReturnValue

| Method | Status |
|--------|--------|
| `getWidth` | ✅ Identical |
| `getHeight` | ✅ Identical |
| `getLineHeight` | ✅ Identical |

### ModifyArg / ModifyVariable

| Target | Status |
|--------|--------|
| `addVisibleMessage` width arg | ✅ Identical |
| `toChatLineX` screenX | ✅ Identical |
| `toChatLineY` screenY | ✅ Identical |
| `mouseClicked` x, y | ✅ Identical |

### Shadow Fields

| Field | Type | Status |
|-------|------|--------|
| `client` | `MinecraftClient` | ✅ Present |
| `visibleMessages` | `List<ChatHudLine.Visible>` | ✅ Present |
| `messages` | `List<ChatHudLine>` | ✅ Present |

---

## 4. ClientPlayNetworkHandlerMixin

| Field | Value |
|-------|-------|
| **Target class** | `net.minecraft.client.network.ClientPlayNetworkHandler` |
| **Status** | ✅ VERIFIED — No changes needed |

| Method | Descriptor | Status |
|--------|-----------|--------|
| `onPlayerList` | `(Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket;)V` | ✅ Identical |

---

## 5. ChatInputSuggestorMixin

| Field | Value |
|-------|-------|
| **Target class** | `net.minecraft.client.gui.screen.ChatInputSuggestor` |
| **Status** | ✅ VERIFIED — No changes needed |

Class structure and constructor signature unchanged.

---

## 6. TextFieldWidgetMixin

| Field | Value |
|-------|-------|
| **Target class** | `net.minecraft.client.gui.widget.TextFieldWidget` |
| **Status** | ✅ VERIFIED — No changes needed |

Class present at `net.minecraft.client.gui.widget.TextFieldWidget` with same structure.

---

## 7. TextRendererDrawerMixin

| Field | Value |
|-------|-------|
| **Target class** | `net.minecraft.client.font.TextRenderer$Drawer` |
| **Status** | ✅ VERIFIED — No changes needed |

Inner class structure unchanged.

---

## 8. AbstractParentElementMixin

| Field | Value |
|-------|-------|
| **Target class** | `net.minecraft.client.gui.AbstractParentElement` |
| **Status** | ✅ VERIFIED — No changes needed |

---

## Accessors (all verified present)

| Accessor | Target Class | Target Field | Status |
|----------|-------------|-------------|--------|
| `ChatHudAccessor` | `ChatHud` | Various | ✅ Present |
| `ChatScreenAccessor` | `ChatScreen` | Various | ✅ Present |
| `ChatInputSuggestorAccessor` | `ChatInputSuggestor` | Various | ✅ Present |
| `TextFieldWidgetAccessor` | `TextFieldWidget` | Various | ✅ Present |
| `SuggestionWindowAccessor` | `ChatInputSuggestor.SuggestionWindow` | Various | ✅ Present |
| `TextRendererAccessor` | `TextRenderer` | Various | ✅ Present |
| `ScreenAccessor` | `Screen` | `addSelectableChild` | ✅ Present |

---

## Conclusion

**All 15 Mixins require ZERO changes.** The Minecraft 1.21.3 client API is binary-compatible
with 1.21.1 for all Chat Canvas Mixin targets. The 1.3.0 source Mixin changes are
functional fixes from the 1.2.0 baseline, not version-specific API work.
