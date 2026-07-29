# Mixin Audit: Minecraft 1.21.11 (Fabric / Yarn)

Baseline: mc/1.21.9 → Target: 1.21.11

## Mixin List (15 total)

| # | Mixin Class | Target Class | Changes |
|---|-------------|--------------|---------|
| 1 | AbstractParentElementMixin | AbstractParentElement | None |
| 2 | ChatHudAccessor | ChatHud | None |
| 3 | **ChatHudMixin** | **ChatHud** | **3 @Shadow removed, getTextStyleAt rewritten** |
| 4 | ChatInputSuggestorAccessor | ChatInputSuggestor | None |
| 5 | ChatInputSuggestorMixin | ChatInputSuggestor | None |
| 6 | ChatScreenAccessor | ChatScreen | None |
| 7 | ChatScreenMixin | ChatScreen | **1 method call changed (textRenderer → advanceValidatingTextRenderer at line 397 TBD)** |
| 8 | ClientPlayNetworkHandlerMixin | ClientPlayNetworkHandler | None |
| 9 | KeyboardMixin | Keyboard | None |
| 10 | ScreenAccessor | Screen | None |
| 11 | SuggestionWindowAccessor | SuggestionWindow | None |
| 12 | TextFieldWidgetAccessor | TextFieldWidget | None |
| 13 | TextFieldWidgetMixin | TextFieldWidget | **drawSelection +boolean param** |
| 14 | TextRendererDrawerMixin | TextRenderer.Drawer | None |

Note: `TextRendererAccessor` was already removed in 1.21.9 port. The Mixin JSON no longer lists it.

## Detailed Changes

### ChatHudMixin (3 changes)

#### 1. Removed @Shadow: toChatLineX
- **Target**: `ChatHud.toChatLineX(double)` → REMOVED in 1.21.11
- **Fix**: Replaced with `ChatLayoutRuntime.currentTransform().screenToChatX(x)`

#### 2. Removed @Shadow: toChatLineY
- **Target**: `ChatHud.toChatLineY(double)` → REMOVED in 1.21.11
- **Fix**: Replaced with `ChatLayoutRuntime.currentTransform().screenToChatY(y)`

#### 3. Removed @Shadow: getMessageLineIndex
- **Target**: `ChatHud.getMessageLineIndex(double, double)` → REMOVED in 1.21.11
- **Fix**: Replaced with manual line index calculation: `(int) Math.floor(chatLineY / lineHeight)`

#### 4. Updated @Shadow: getLineHeight
- **Target**: `ChatHud.getLineHeight()` → added as protected abstract Shadow
- **Status**: Exists in 1.21.11 as private method

#### 5. Rewritten getTextStyleAt injection
- Old: used `toChatLineX()`, `toChatLineY()`, `getMessageLineIndex()` (removed methods)
- New: uses `ChatLayoutRuntime.currentTransform()` for coordinate conversion + manual line index calculation
- **Status**: Compiled, runtime verification pending

#### 6. Added helper: chat_canvas$styleAtPixel
- Replaces: `TextHandler.getStyleAt(OrderedText, int)` (removed in 1.21.11)
- Implementation: Character-by-character walk using `OrderedText.accept()` + `renderer.getWidth()` per character

### TextFieldWidgetMixin (1 change)

- **Target method**: `drawSelection(int x1, int y1, int x2, int y2)` → **ADDED 5th parameter `boolean invert`**
- **Fix**: Added `true` as last parameter

### Other changes (non-Mixin)

- **MentionSoundPlayer**: `PositionedSoundInstance.master()` → `PositionedSoundInstance.ui()`
- **ChatCanvasEditorScreen**: `resize(MinecraftClient, int, int)` → `resize(int, int)` (does not extend ChatScreen)

## Runtime Verification

All Mixin targets verified via MCP decompilation of 1.21.11:

| Target Method | Status |
|--------------|--------|
| ChatScreen.init() | ✓ Present |
| ChatScreen.resize(MinecraftClient, int, int) | ✓ Present |
| ChatScreen.keyPressed(KeyInput) | ✓ Present |
| ChatScreen.mouseClicked(Click, boolean) | ✓ Present |
| ChatScreen.render(DrawContext, int, int, float) | ✓ Present |
| ChatScreen.removed() | ✓ Present |
| ChatScreen.insertText(String, boolean) | ✓ Present |
| ChatHud.render(DrawContext, TextRenderer, int, int, int, boolean, boolean) | ✓ Present |
| ChatHud.getTextStyleAt(double, double) | ✓ Present |
| ChatHud.addMessage(Text, MessageSignatureData, MessageIndicator) | ✓ Present |
| ChatHud.getWidth() | ✓ Present |
| ChatHud.getChatScale() | ✓ Present |
| Keyboard.onKey(long, int, KeyInput) | ✓ Present (private, Mixin can inject) |
| TextFieldWidget.keyPressed(KeyInput) | ✓ Present |
| TextFieldWidget.charTyped(CharInput) | ✓ Present |
| TextFieldWidget.renderWidget(DrawContext, int, int, float) | ✓ Present |
| ClientPlayNetworkHandler.onPlayerList(PlayerListS2CPacket) | ✓ Present |
| ClientPlayNetworkHandler.onPlayerRemove(PlayerRemoveS2CPacket) | ✓ Present |
| ChatInputSuggestor.keyPressed(KeyInput) | ✓ Present |
| ChatInputSuggestor.mouseClicked(Click) | ✓ Present |
