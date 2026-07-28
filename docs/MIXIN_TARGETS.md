# Mixin Targets

Complete list of all Chat Canvas Mixin classes, their target Minecraft classes,
and injected methods. Verified for Minecraft 1.21.1 and 1.21.2 (Yarn mappings).

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
  - `render` → `DrawContext.fill` — Custom chat field background
- **Version sensitivity**: HIGH — depends on ChatScreen field layout, method signatures
- **Notes**: Does NOT inject `keyReleased` (ChatScreen does not declare it in 1.21.1)

### KeyboardMixin

- **Target**: `net.minecraft.client.Keyboard`
- **Purpose**: Listen for GLFW key release events to detect voice shortcut release
- **Injected methods**:
  - `onKey` (TAIL) — Check GLFW_RELEASE, delegate to ChatScreen via `ChatCanvasVoiceShortcutHost`
- **Version sensitivity**: MEDIUM — depends on `Keyboard.onKey` signature
- **Notes**: Do NOT change back to ChatScreen.keyReleased. This Mixin does not cancel the event.

### ChatHudMixin

- **Target**: `net.minecraft.client.gui.hud.ChatHud`
- **Purpose**: Dual-channel chat rendering (player chat + command/system)
- **Version sensitivity**: HIGH — depends on ChatHud internal message storage
- **Accessor**: `ChatHudAccessor` exposes internal fields

### ClientPlayNetworkHandlerMixin

- **Target**: `net.minecraft.client.network.ClientPlayNetworkHandler`
- **Purpose**: Message ingress and classification
- **Version sensitivity**: MEDIUM

### ChatInputSuggestorMixin

- **Target**: `net.minecraft.client.gui.screen.ChatInputSuggestor`
- **Purpose**: Suggestion window integration for both input fields
- **Accessor**: `ChatInputSuggestorAccessor`, `SuggestionWindowAccessor`
- **Version sensitivity**: MEDIUM

### TextFieldWidgetMixin

- **Target**: `net.minecraft.client.gui.widget.TextFieldWidget`
- **Purpose**: Extended text field capabilities for independent inputs
- **Accessor**: `TextFieldWidgetAccessor`
- **Version sensitivity**: LOW

### TextRendererDrawerMixin

- **Target**: `net.minecraft.client.font.TextRenderer$Drawer`
- **Purpose**: Font rendering hooks for styled text and emoji
- **Version sensitivity**: LOW-MEDIUM
- **Accessor**: `TextRendererAccessor`

### AbstractParentElementMixin

- **Target**: `net.minecraft.client.gui.AbstractParentElement`
- **Purpose**: Focus management for independent input fields
- **Version sensitivity**: LOW

### ScreenAccessor

- **Target**: `net.minecraft.client.gui.screen.Screen`
- **Purpose**: Expose `addSelectableChild` for adding independent widgets
- **Version sensitivity**: LOW

### ChatScreenAccessor

- **Target**: `net.minecraft.client.gui.screen.ChatScreen`
- **Purpose**: Expose internal ChatScreen fields
- **Version sensitivity**: LOW
