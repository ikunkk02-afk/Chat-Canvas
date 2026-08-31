# Mixin Audit: 1.21.1 → 1.21.2

Each Mixin was verified against decompiled 1.21.2 source via MC MCP (minecraft-dev-cli).
All 15 Mixin classes passed audit — zero runtime changes required.

## Verified Mixins

### ChatScreenMixin
- **Target**: `net.minecraft.client.gui.screen.ChatScreen`
- **Status**: ✅ PASS — No changes needed
- **Verified**: All injected methods (init, keyPressed, render, removed, resize, mouseClicked, mouseScrolled, insertText) have identical Yarn signatures in 1.21.2
- **Shadow fields**: `chatField` (TextFieldWidget), `chatInputSuggestor` (ChatInputSuggestor) — both present with same types

### KeyboardMixin
- **Target**: `net.minecraft.client.Keyboard`
- **Status**: ✅ PASS — No changes needed
- **Verified**: `onKey(long window, int key, int scancode, int action, int modifiers)` — identical signature
- **Injection**: `@At("TAIL")` — safe, non-cancellable

### ChatHudMixin
- **Target**: `net.minecraft.client.gui.hud.ChatHud`
- **Status**: ✅ PASS — No changes needed
- **Verified**: 
  - `render(DrawContext, int, int, int, boolean)` — identical
  - `addMessage(Text, MessageSignatureData, MessageIndicator)` — identical
  - `getTextStyleAt(double, double)` — identical
  - `refresh()` — visibility changed to `private` (Mixin-safe)
  - `getLineHeight()` — visibility changed to `private` (Mixin-safe)
  - All `@WrapOperation` targets (DrawContext.fill, ChatMessages.breakRenderedChatMessageLines, drawTextWithShadow) — identical

### ClientPlayNetworkHandlerMixin
- **Target**: `net.minecraft.client.network.ClientPlayNetworkHandler`
- **Status**: ✅ PASS — No changes needed
- **Verified**: `onPlayerList(PlayerListS2CPacket)` and `onPlayerRemove(PlayerRemoveS2CPacket)` — identical signatures

### ChatInputSuggestorMixin
- **Target**: `net.minecraft.client.gui.screen.ChatInputSuggestor`
- **Status**: ✅ PASS — No changes needed
- **Verified**: 
  - `show(boolean)` and `renderMessages(DrawContext)` targets — identical
  - `Screen.height` field reference (@ModifyExpressionValue) — still present
  - `provideRenderText(String, int)` — identical

### TextFieldWidgetMixin
- **Target**: `net.minecraft.client.gui.widget.TextFieldWidget`
- **Status**: ✅ PASS — No changes needed
- **Verified**: 
  - `onClick(double, double)` — identical
  - `keyPressed(int, int, int)` — identical
  - `write(String)` — identical
  - `renderWidget(DrawContext, int, int, float)` — identical
  - `updateFirstCharacterIndex(int)` — identical (now private, Mixin-safe)
  - `getCharacterX(int)` — identical
  - `drawSelectionHighlight(DrawContext, int, int, int, int)` — identical (now private, still shadowable)

### TextRendererDrawerMixin
- **Target**: `net.minecraft.client.font.TextRenderer$Drawer`
- **Status**: ✅ PASS — No changes needed
- **Verified**: `accept(int, Style, int)Z` — identical signature

### AbstractParentElementMixin
- **Target**: `net.minecraft.client.gui.ParentElement`
- **Status**: ✅ PASS — No changes needed
- **Verified**: `mouseDragged`, `mouseReleased`, `charTyped` — identical signatures

### Accessors (6 classes)
- **ChatHudAccessor** — fields unchanged
- **ChatInputSuggestorAccessor** — fields unchanged
- **SuggestionWindowAccessor** — fields unchanged
- **TextFieldWidgetAccessor** — fields unchanged (note: `updateFirstCharacterIndex` became private; still accessible via shadow)
- **TextRendererAccessor** — fields unchanged
- **ChatScreenAccessor** — fields unchanged
- **ScreenAccessor** — `addSelectableChild` method unchanged

## Runtime Verification

The 1.21.2 development client was launched with `runClient` after the port.
The startup/resource-reload smoke test completed without an
`InvalidInjectionException`, `MixinApplyError`, Chat Canvas resource error or
native initialization failure. Interactive chat, command, UI and voice-input
hardware scenarios still require manual in-game testing.

- [x] Client reaches resource reload with no Mixin apply error
- [x] No Chat Canvas resource or native initialization failure in startup log
- [ ] Chat screen opens normally and dual input fields are manually exercised
- [ ] Command suggestor and custom rendering are manually exercised
- [ ] Voice shortcut and microphone capture are manually exercised
