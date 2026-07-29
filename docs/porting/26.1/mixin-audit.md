# Mixin Audit: Minecraft 1.21.1 → Minecraft 26.1

## 审计日期: 2026-07-29
## 目标 Minecraft 版本: 26.1
## 映射: Mojang (官方)

---

## 1. ChatScreenMixin

- **目标类 (Yarn)**: `net.minecraft.client.gui.screen.ChatScreen`
- **目标类 (Mojang)**: `net.minecraft.client.gui.screens.ChatScreen`
- **状态**: 🔴 需要大量修改

### Shadow 字段变更

| Yarn | Mojang (26.1) | 状态 |
|------|-------------|------|
| `chatField` (TextFieldWidget) | `input` (EditBox) | ✅ 已识别 |
| `chatInputSuggestor` (ChatInputSuggestor) | `commandSuggestions` (CommandSuggestions) | ✅ 已识别 |

### 注入方法变更

| Yarn 方法 | Mojang 方法 | 描述符 | 状态 |
|-----------|-----------|--------|------|
| `init` | `init` | `()V` | ⚠️ 内部字段名全部变更 |
| `keyPressed` | 可能变为 `keyPressed(KeyEvent)` | 记录参数 | 🔴 需确认 |
| `charTyped` | 可能需要适配 | | 🔴 |
| `render` | `render(GuiGraphics, int, int, float)` | | ⚠️ DrawContext→GuiGraphics |
| `mouseClicked` | `mouseClicked(MouseButtonEvent)` | 新事件类型 | 🔴 |
| `removed` | `removed` | `()V` | ⚠️ |
| `resize` | `resize(Minecraft, int, int)` | | ⚠️ 签名变更 |
| `insertText` | 可能已移除 | | 🔴 |

### 其他变更
- `DrawContext` → `GuiGraphics` (所有渲染方法签名变更)
- `Screen.height`, `Screen.width` → 可能通过 `Minecraft` 实例获取
- `client.currentScreen` → `minecraft.screen`
- 输入处理从原始 `(int, int, int)` 变为 `KeyEvent`, `MouseButtonEvent` 记录

---

## 2. KeyboardMixin

- **目标类 (Yarn)**: `net.minecraft.client.Keyboard`
- **目标类 (Mojang)**: `net.minecraft.client.KeyboardHandler`
- **状态**: 🔴 需要修改

### 注入方法变更

| Yarn | Mojang (26.1) | 描述符 | 状态 |
|------|-------------|--------|------|
| `onKey(long, int, int, int, int)` | `keyPress(long, int, int, int, int)` | 待确认 | 🔴 方法名可能变更 |

---

## 3. ChatHudMixin

- **目标类 (Yarn)**: `net.minecraft.client.gui.hud.ChatHud`
- **目标类 (Mojang)**: `net.minecraft.client.gui.components.ChatComponent`
- **状态**: 🔴 需要大量修改

### Shadow 字段/方法变更

| Yarn | Mojang (26.1) | 状态 |
|------|-------------|------|
| `client` (MinecraftClient) | `minecraft` (Minecraft) | ✅ |
| `visibleMessages` (List<ChatHudLine.Visible>) | `trimmedMessages` (List<GuiMessage.Line>) | ⚠️ 类型变更 |
| `messages` (List<ChatHudLine>) | `allMessages` (List<GuiMessage>) | ⚠️ 类型变更 |
| `getWidth()` | 待确认 | 🔴 |
| `getChatScale()` | 待确认 | 🔴 |
| `toChatLineX(double)` | 可能已移除 | 🔴 |
| `toChatLineY(double)` | 可能已移除 | 🔴 |

### 渲染方法变更
- `render(DrawContext, ...)` → `render(GuiGraphics, ...)` 

---

## 4. ClientPlayNetworkHandlerMixin

- **目标类 (Yarn)**: `net.minecraft.client.network.ClientPlayNetworkHandler`
- **目标类 (Mojang)**: `net.minecraft.client.multiplayer.ClientPacketListener`
- **状态**: ⚠️ 中等修改

| Yarn 方法 | Mojang 方法 | 状态 |
|-----------|-----------|------|
| `onPlayerList(PlayerListS2CPacket)` | 待确认 | 🔴 |
| `onPlayerRemove(PlayerRemoveS2CPacket)` | 待确认 | 🔴 |

---

## 5. ChatInputSuggestorMixin

- **目标类 (Yarn)**: `net.minecraft.client.gui.screen.ChatInputSuggestor`
- **目标类 (Mojang)**: `net.minecraft.client.gui.components.CommandSuggestions`
- **状态**: 🔴 需要大量修改

### Shadow 字段变更

| Yarn | Mojang (26.1) | 状态 |
|------|-------------|------|
| `owner` (Screen) | 待确认 | 🔴 |
| `textField` (TextFieldWidget) | `input` (EditBox) | ⚠️ |
| `chatScreenSized` (boolean) | 可能已移除 | 🔴 |
| `messages` | 待确认 | 🔴 |
| `x`, `width` | 待确认 | 🔴 |

### 注入方法变更

| Yarn | Mojang | 状态 |
|------|--------|------|
| `show` | 可能已重命名 | 🔴 |
| `renderMessages` | 可能已重命名 | 🔴 |
| `showCommandSuggestions` | 可能已重命名 | 🔴 |
| `provideRenderText` | 可能已移除 | 🔴 |

---

## 6. TextFieldWidgetMixin

- **目标类 (Yarn)**: `net.minecraft.client.gui.widget.TextFieldWidget`
- **目标类 (Mojang)**: `net.minecraft.client.gui.components.EditBox`
- **状态**: 🔴 需要大量修改

### Shadow 字段/方法

| Yarn | Mojang (26.1) | 状态 |
|------|-------------|------|
| `textRenderer` (TextRenderer) | `font` (Font) | ⚠️ |
| `text` (String) | `value` (String) | ⚠️ |
| `maxLength` (int) | 待确认 | 🔴 |
| `drawsBackground` (boolean) | `bordered` (boolean) | ⚠️ |
| `editable` (boolean) | 待确认 | 🔴 |
| `firstCharacterIndex` | `displayPos` | ⚠️ |
| `selectionStart` | `cursorPos` | ⚠️ |
| `selectionEnd` | `highlightPos` | ⚠️ |
| `editableColor` | 待确认 | 🔴 |
| `renderTextProvider` | `formatter` | ⚠️ |
| `getInnerWidth()` | 待确认 | 🔴 |
| `setCursor(int, boolean)` | `moveCursorTo(int, boolean)` | ⚠️ |
| `drawSelectionHighlight(...)` | 待确认 | 🔴 |

---

## 7. TextRendererDrawerMixin

- **目标类 (Yarn)**: `net.minecraft.client.font.TextRenderer$Drawer`
- **目标类 (Mojang)**: `net.minecraft.client.gui.Font$...` (内部类名待确认)
- **状态**: 🔴 内部类名和方法均变更

---

## 8. AbstractParentElementMixin

- **目标类 (Yarn)**: `net.minecraft.client.gui.ParentElement`
- **目标类 (Mojang)**: `net.minecraft.client.gui.components.events.AbstractContainerEventHandler`
- **状态**: 🔴 接口方法全部变更

| Yarn 方法 | Mojang | 状态 |
|-----------|--------|------|
| `mouseDragged` | 待确认 | 🔴 |
| `mouseReleased` | 待确认 | 🔴 |
| `charTyped` | 待确认 | 🔴 |

---

## 9-15. Accessor Mixins

| Accessor | Yarn Target | Mojang Target | 状态 |
|----------|-----------|-------------|------|
| ChatHudAccessor | ChatHud | ChatComponent | 🔴 |
| ChatInputSuggestorAccessor | ChatInputSuggestor | CommandSuggestions | 🔴 |
| ChatScreenAccessor | ChatScreen | ChatScreen | ⚠️ |
| ScreenAccessor | Screen | Screen | ⚠️ |
| SuggestionWindowAccessor | SuggestionWindow | 待确认 | 🔴 |
| TextFieldWidgetAccessor | TextFieldWidget | EditBox | 🔴 |
| TextRendererAccessor | TextRenderer | Font | 🔴 |

---

## 总结

- **总 Mixin 数**: 15
- **需要完全重写**: 13 (所有涉及 Yarn → Mojang 名称映射的)
- **可能需要小改**: 2 (ChatScreenAccessor, ScreenAccessor — 如果字段名不变)
- **主要风险**: Input API 重构 (KeyEvent/MouseButtonEvent), 渲染 API 变更 (DrawContext→GuiGraphics), ChatHud→ChatComponent 结构变化
