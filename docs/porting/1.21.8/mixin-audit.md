# Mixin Audit: Minecraft 1.21.8 (Fabric / Yarn)

Audit of all 15 Chat Canvas Mixin classes against Minecraft 1.21.8 Yarn mappings.
Source: mc/1.21.6 baseline → target: 1.21.8.

## Audit Summary

| Mixin | Status | Changes Required |
|-------|--------|-----------------|
| ChatScreenMixin | ✅ Compatible | None |
| ChatHudMixin | ✅ Compatible | None (existing 1.21.6 adaptations sufficient) |
| KeyboardMixin | ✅ Compatible | None |
| ClientPlayNetworkHandlerMixin | ✅ Compatible | None |
| ChatInputSuggestorMixin | ✅ Compatible | None |
| TextFieldWidgetMixin | ⚠️ Fixed | Removed `@Shadow drawSelectionHighlight`, replaced with `context.drawSelection()` |
| TextRendererDrawerMixin | ✅ Compatible | None |
| AbstractParentElementMixin | ✅ Compatible | None |
| ChatScreenAccessor | ✅ Compatible | None |
| ScreenAccessor | ✅ Compatible | None |
| ChatHudAccessor | ✅ Compatible | None |
| ChatInputSuggestorAccessor | ✅ Compatible | None |
| SuggestionWindowAccessor | ✅ Compatible | None |
| TextFieldWidgetAccessor | ✅ Compatible | None |
| TextRendererAccessor | ✅ Compatible | None |

## Detailed Audit

### ChatScreenMixin
- **Target**: `net.minecraft.client.gui.screen.ChatScreen`
- **Target Methods**:
  - `init()V` — ✅ same signature
  - `setInitialFocus()V` — ✅ same
  - `resize(MinecraftClient, int, int)V` — ✅ same
  - `render(DrawContext, int, int, float)V` — ✅ same
  - `mouseClicked(double, double, int)Z` — ✅ same
  - `mouseScrolled(double, double, double, double)Z` — ✅ same
  - `removed()V` — ✅ same
  - `keyPressed(int, int, int)Z` — ✅ same
  - `insertText(String, boolean)V` — ✅ same
- **@WrapOperation**: `DrawContext.fill(IIIII)V` ordinal 0 in `render` — ✅ target exists
- **Shadow fields**: `chatField` (protected), `chatInputSuggestor` (package-private) — ✅ present
- **Changes**: None

### ChatHudMixin
- **Target**: `net.minecraft.client.gui.hud.ChatHud`
- **Status**: 1.21.6 adaptations (commented-out @WrapOperation on render, Matrix3x2f) still valid.
- **Verified Methods**:
  - `render(DrawContext, int, int, int, boolean)V` — ✅ same
  - `addMessage(Text, MessageSignatureData, MessageIndicator)V` — ✅ same
  - `addMessage(ChatHudLine)V` — ✅ same
  - `refresh()V` — ✅ same
  - `clear(boolean)V` — ✅ same
  - `getWidth()I` — ✅ same
  - `getHeight()I` — ✅ same
  - `getLineHeight()I` — ✅ same
  - `getChatScale()D` — ✅ same
  - `getIndicatorX(ChatHudLine.Visible)I` — ✅ same
  - `getMessageLineIndex(double, double)I` — ✅ exists (also `getMessageIndex`)
  - `toChatLineX(double)D` — ✅ same
  - `toChatLineY(double)D` — ✅ same
  - `getTextStyleAt(double, double)LStyle;` — ✅ same
  - `mouseClicked(double, double)Z` — ✅ same
  - `addVisibleMessage(ChatHudLine)V` — ✅ same
- **Changes**: None

### KeyboardMixin
- **Target**: `net.minecraft.client.Keyboard`
- **Target Method**: `onKey(long, int, int, int, int)V` — ✅ same signature
- **Changes**: None

### ClientPlayNetworkHandlerMixin
- **Target**: `net.minecraft.client.network.ClientPlayNetworkHandler`
- **Target Methods**:
  - `onPlayerList(PlayerListS2CPacket)V` — ✅ same
  - `onPlayerRemove(PlayerRemoveS2CPacket)V` — ✅ same
- **Changes**: None

### ChatInputSuggestorMixin
- **Target**: `net.minecraft.client.gui.screen.ChatInputSuggestor`
- **Shadow Fields**: `owner`, `textField`, `chatScreenSized`, `messages`, `x`, `width` — ✅ all present
- **ModifyExpressionValue targets**: `Screen.height` field in `show`/`renderMessages` — ✅ field exists
- **Changes**: None

### TextFieldWidgetMixin (FIXED)
- **Target**: `net.minecraft.client.gui.widget.TextFieldWidget`
- **Issue**: `drawSelectionHighlight(DrawContext, int, int, int, int)` was removed in 1.21.7
- **Fix**: 
  - Removed `@Shadow private void drawSelectionHighlight(...)`
  - Replaced call with `context.drawSelection(x1, y1, x2, y2)` (added in 1.21.7)
- **Verified Methods**:
  - `renderWidget(DrawContext, int, int, float)V` — ✅ same
  - `keyPressed(int, int, int)Z` — ✅ same
  - `charTyped(char, int)Z` — ✅ same
  - `onClick(double, double)V` — ✅ same
  - `write(String)V` — ✅ same
  - `setText(String)V` — ✅ same
  - `updateFirstCharacterIndex(int)V` — ✅ same
  - `getCharacterX(int)I` — ✅ same
- **Shadow fields**: All verified — `textRenderer`, `text`, `maxLength`, `drawsBackground`, `editable`, `firstCharacterIndex`, `selectionStart`, `selectionEnd`, `editableColor`, `uneditableColor`, `suggestion`, `renderTextProvider`, `lastSwitchFocusTime`

### TextRendererDrawerMixin
- **Target**: `net.minecraft.client.font.TextRenderer$Drawer`
- **Target Method**: `accept(int, Style, int)Z` — ✅ same signature
- **Shadow Field**: `x` (float) — ✅ present
- **Changes**: None

### AbstractParentElementMixin
- **Target**: `net.minecraft.client.gui.ParentElement` (interface)
- **Injected Methods**:
  - `mouseDragged(double, double, int, double, double)Z` — ✅ same
  - `mouseReleased(double, double, int)Z` — ✅ same
  - `charTyped(char, int)Z` — ✅ same
- **Changes**: None

### Accessor Interfaces
All accessors verified against target classes:
- `ChatScreenAccessor` → `chatField` ✅
- `ScreenAccessor` → `addSelectableChild` ✅
- `ChatHudAccessor` → internal fields ✅
- `ChatInputSuggestorAccessor` → internal fields ✅
- `SuggestionWindowAccessor` → internal fields ✅
- `TextFieldWidgetAccessor` → `selectionEnd` ✅
- `TextRendererAccessor` → internal fields ✅
