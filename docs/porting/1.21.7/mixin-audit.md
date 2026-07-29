# Mixin Audit: Minecraft 1.21.1 → 1.21.7

## Audit Date: 2026-07-29

## Summary

| Mixin | Status | Changes Required |
|-------|--------|-----------------|
| ChatScreenMixin | ✅ COMPATIBLE | None (all targeted methods unchanged) |
| KeyboardMixin | ✅ COMPATIBLE | None (onKey signature unchanged) |
| ChatHudMixin | 🔴 BREAKING | Removed 4 @WrapOperation (lambda refactor) |
| ChatInputSuggestorMixin | ✅ COMPATIBLE | None |
| TextFieldWidgetMixin | 🟡 MINOR | RenderLayer→RenderPipelines import |
| TextRendererDrawerMixin | ✅ COMPATIBLE | None |
| ClientPlayNetworkHandlerMixin | ✅ COMPATIBLE | None |
| AbstractParentElementMixin | ✅ COMPATIBLE | None |
| ChatHudAccessor | ✅ COMPATIBLE | None |
| ChatScreenAccessor | ✅ COMPATIBLE | None |
| ChatInputSuggestorAccessor | ✅ COMPATIBLE | None |
| ScreenAccessor | ✅ COMPATIBLE | None |
| SuggestionWindowAccessor | ✅ COMPATIBLE | None |
| TextFieldWidgetAccessor | ✅ COMPATIBLE | None |
| TextRendererAccessor | ✅ COMPATIBLE | None |

## Detailed Audit

### ChatScreenMixin — ✅ COMPATIBLE

| Method | Target | 1.21.1 | 1.21.7 | Status |
|--------|--------|--------|--------|--------|
| init | ChatScreen | `init()` | `init()` | ✅ Same |
| keyPressed | ChatScreen | `keyPressed(int,int,int)` | `keyPressed(int,int,int)` | ✅ Same |
| mouseClicked | ChatScreen | `mouseClicked(double,double,int)` | `mouseClicked(double,double,int)` | ✅ Same |
| mouseScrolled | ChatScreen | `mouseScrolled(double,double,double,double)` | `mouseScrolled(double,double,double,double)` | ✅ Same |
| render | ChatScreen | `render(DrawContext,int,int,float)` | `render(DrawContext,int,int,float)` | ✅ Same |
| removed | ChatScreen | `removed()` | `removed()` | ✅ Same |
| resize | ChatScreen | `resize(MinecraftClient,int,int)` | `resize(MinecraftClient,int,int)` | ✅ Same |
| insertText | ChatScreen | `insertText(String,boolean)` | `insertText(String,boolean)` | ✅ Same |
| setInitialFocus | ChatScreen | `setInitialFocus()` | `setInitialFocus()` | ✅ Same |

### ChatHudMixin — 🔴 BREAKING

**Root Cause**: `ChatHud.render()` now uses a helper method `method_71990` with lambdas for message rendering. `@WrapOperation` cannot target INVOKE instructions inside synthetic lambda methods from the `render` method scope.

**Removed injections**:
- `chat_canvas$drawCompactMessageBackground` — @WrapOperation on fill(IIIII)V ordinal 0
- `chat_canvas$drawCompactIndicatorBackground` — @WrapOperation on fill(IIIII)V ordinal 1  
- `chat_canvas$drawConfiguredChatLine` — @WrapOperation on drawTextWithShadow(TextRenderer,OrderedText,III)I
- `chat_canvas$drawConfiguredQueueText` — @WrapOperation on drawTextWithShadow(TextRenderer,Text,III)I

**Preserved injections** (all verified working):
- `chat_canvas$pushLayoutTransform` — @Inject at render HEAD (cancellable)
- `chat_canvas$popLayoutTransform` — @Inject at render RETURN
- `chat_canvas$useConfiguredWidth` — @ModifyReturnValue on getWidth
- `chat_canvas$useConfiguredHeight` — @ModifyReturnValue on getHeight
- `chat_canvas$useConfiguredLineSpacing` — @ModifyReturnValue on getLineHeight
- `chat_canvas$reserveBackgroundPaddingForWrapping` — @ModifyArg on addVisibleMessage
- `chat_canvas$bindVisiblePlayerNameRanges` — @WrapOperation on addVisibleMessage
- `chat_canvas$getAlignedTextStyle` — @Inject on getTextStyleAt
- `chat_canvas$getAlignedIndicatorX` — @Inject on getIndicatorX
- `chat_canvas$clearLineMetrics` — @Inject on refresh
- `chat_canvas$clearMessageMetadata` — @Inject on clear
- `chat_canvas$captureMessage` — @Inject on addMessage
- `chat_canvas$pruneMessageMetadata` — @Inject on addMessage(ChatHudLine)
- `chat_canvas$screenToChatX/Y` — @ModifyVariable on toChatLineX/Y
- `chat_canvas$queueClickX/Y` — @ModifyVariable on mouseClicked

### KeyboardMixin — ✅ COMPATIBLE

`Keyboard.onKey(long window, int key, int scancode, int action, int modifiers)` — UNCHANGED

### ChatInputSuggestorMixin — ✅ COMPATIBLE

All targeted fields (`Screen.height`, `owner.width`) and methods (`showCommandSuggestions`, `provideRenderText`) verified working.

### TextFieldWidgetMixin — 🟡 MINOR

- `renderWidget` — same signature
- `fill(RenderLayer.getGuiOverlay(), ...)` → `fill(RenderPipelines.GUI, ...)` — import fix
- All `@Inject` targets unchanged

### TextRendererDrawerMixin — ✅ COMPATIBLE

`TextRenderer$Drawer.accept(int, Style, int)` inner class structure unchanged.

### ClientPlayNetworkHandlerMixin — ✅ COMPATIBLE

`onPlayerList(PlayerListS2CPacket)` and `onPlayerRemove(PlayerRemoveS2CPacket)` both unchanged.
