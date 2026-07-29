# Mixin Targets

Complete list of all Chat Canvas Mixin classes, their target Minecraft classes,
and injected methods. Generated for Minecraft 1.21.8 (Yarn mappings).

## Client Mixins

All client mixins are registered in `src/client/resources/chat_canvas.client.mixins.json`.

### ChatScreenMixin

- **Target**: `net.minecraft.client.gui.screen.ChatScreen`
- **Purpose**: Independent player chat and command input fields, dual input modes, voice overlay, emoji panel, command tool panel, character suppression
- **Implements**: `ChatCanvasInputScreenBridge`, `ChatCanvasVoiceShortcutHost`
- **Injected methods**:
  - `init` — Initialize independent inputs, emoji, voice, command tools
  - `setInitialFocus` — Focus active input field
  - `resize` (HEAD) — Capture field state before resize
  - `render` (HEAD) — Keep input placement current
  - `render` (RETURN) — Render independent inputs, overlays, panels
  - `mouseClicked` (HEAD, cancellable) — Route mouse to emoji/voice/command/mention
  - `mouseScrolled` (HEAD, cancellable) — Route scroll to active input
  - `removed` (HEAD) — Save drafts, dispose resources
  - `keyPressed` (HEAD, cancellable) — Route keys by input mode, voice shortcut
  - `insertText` (HEAD, cancellable) — Route insert to active field
- **Wrap operations**:
  - `render` → `DrawContext.fill(IIIII)V` — Custom chat field background
- **Version sensitivity**: HIGH — depends on ChatScreen field layout, method signatures
- **1.21.8 status**: ✅ All targets verified. No changes needed.

### KeyboardMixin

- **Target**: `net.minecraft.client.Keyboard`
- **Purpose**: Listen for GLFW key release events to detect voice shortcut release
- **Injected methods**:
  - `onKey` (TAIL) — Check GLFW_RELEASE, delegate to ChatScreen via `ChatCanvasVoiceShortcutHost`
- **Version sensitivity**: MEDIUM — depends on `Keyboard.onKey` signature
- **1.21.8 status**: ✅ `onKey(long, int, int, int, int)V` unchanged.

### ChatHudMixin

- **Target**: `net.minecraft.client.gui.hud.ChatHud`
- **Purpose**: Dual-channel chat rendering (player chat + command/system)
- **Version sensitivity**: HIGH — depends on ChatHud internal message storage
- **1.21.8 status**: ✅ All targets verified. `@WrapOperation` on render fill/drawText disabled (lambdas in 1.21.6+). `@Inject` HEAD/RETURN working.
- **Accessor**: `ChatHudAccessor` exposes internal fields

### ClientPlayNetworkHandlerMixin

- **Target**: `net.minecraft.client.network.ClientPlayNetworkHandler`
- **Purpose**: Message ingress and classification
- **1.21.8 status**: ✅ `onPlayerList`/`onPlayerRemove` unchanged.

### ChatInputSuggestorMixin

- **Target**: `net.minecraft.client.gui.screen.ChatInputSuggestor`
- **Purpose**: Suggestion window integration for both input fields
- **1.21.8 status**: ✅ All fields and method targets verified.
- **Accessor**: `ChatInputSuggestorAccessor`, `SuggestionWindowAccessor`

### TextFieldWidgetMixin

- **Target**: `net.minecraft.client.gui.widget.TextFieldWidget`
- **Purpose**: Extended text field capabilities for independent inputs
- **1.21.8 status**: ✅ Fixed. Removed `@Shadow drawSelectionHighlight` (removed in 1.21.7), replaced with `context.drawSelection()`.
- **Accessor**: `TextFieldWidgetAccessor`

### TextRendererDrawerMixin

- **Target**: `net.minecraft.client.font.TextRenderer$Drawer`
- **Purpose**: Font rendering hooks for styled text and emoji
- **1.21.8 status**: ✅ `accept(int, Style, int)Z` unchanged.
- **Accessor**: `TextRendererAccessor`

### AbstractParentElementMixin

- **Target**: `net.minecraft.client.gui.AbstractParentElement`
- **Purpose**: Focus management for independent input fields
- **1.21.8 status**: ✅ All injected methods verified.

### ScreenAccessor

- **Target**: `net.minecraft.client.gui.screen.Screen`
- **Purpose**: Expose `addSelectableChild` for adding independent widgets
- **1.21.8 status**: ✅ Method exists.

### ChatScreenAccessor

- **Target**: `net.minecraft.client.gui.screen.ChatScreen`
- **Purpose**: Expose `chatField`
- **1.21.8 status**: ✅ Field exists.
