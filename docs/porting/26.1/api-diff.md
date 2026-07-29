# API Diff: Minecraft 1.21.1 (Yarn) → Minecraft 26.1 (Mojang)

> 本文档记录 Chat Canvas 从 Minecraft 1.21.1 Fabric (Yarn mappings) 移植到
> Minecraft 26.1 Fabric (Mojang mappings) 的所有 API 变化。

## 版本信息

| 项目 | 1.21.1 (Yarn) | 26.1 (Mojang) |
|------|-------------|-------------|
| 映射系统 | Yarn (net.fabricmc:yarn) | Mojang (内置，无需 mappings) |
| Loom 插件 | fabric-loom-remap | fabric-loom |
| 依赖声明 | modImplementation | implementation |
| Java | 21 | 25 |
| Fabric Loader | 0.19.3 | 0.19.3 |
| Fabric API | 0.116.14+1.21.1 | 0.145.1+26.1 |
| owo-lib | 0.12.15.4+1.21 | 0.13.0+26.1 |
| Mod Menu | 11.0.4 | 18.0.0-alpha.8 |

## 核心类重命名映射

### 客户端类

| Yarn (1.21.1) | Mojang (26.1) |
|---|---|
| `net.minecraft.client.MinecraftClient` | `net.minecraft.client.Minecraft` |
| `net.minecraft.client.gui.screen.Screen` | `net.minecraft.client.gui.screens.Screen` |
| `net.minecraft.client.gui.screen.ChatScreen` | `net.minecraft.client.gui.screens.ChatScreen` |
| `net.minecraft.client.gui.screen.ChatInputSuggestor` | `net.minecraft.client.gui.components.CommandSuggestions` |
| `net.minecraft.client.gui.widget.TextFieldWidget` | `net.minecraft.client.gui.components.EditBox` |
| `net.minecraft.client.gui.hud.ChatHud` | `net.minecraft.client.gui.components.ChatComponent` |
| `net.minecraft.client.gui.hud.ChatHudLine` | `net.minecraft.client.multiplayer.chat.GuiMessage` |
| `net.minecraft.client.gui.hud.MessageIndicator` | `net.minecraft.client.multiplayer.chat.GuiMessageTag` |
| `net.minecraft.client.font.TextRenderer` | `net.minecraft.client.gui.Font` |
| `net.minecraft.client.Keyboard` | `net.minecraft.client.KeyboardHandler` |
| `net.minecraft.client.gui.DrawContext` | `net.minecraft.client.gui.GuiGraphics` |
| `net.minecraft.client.gui.AbstractParentElement` | `net.minecraft.client.gui.components.events.AbstractContainerEventHandler` |
| `net.minecraft.client.gui.ParentElement` | `net.minecraft.client.gui.components.events.AbstractContainerEventHandler` |

### 网络类

| Yarn | Mojang |
|------|--------|
| `net.minecraft.client.network.ClientPlayNetworkHandler` | `net.minecraft.client.multiplayer.ClientGamePacketListener` |
| `net.minecraft.client.network.PlayerListEntry` | `net.minecraft.client.multiplayer.PlayerInfo` |
| `net.minecraft.client.network.AbstractClientPlayerEntity` | `net.minecraft.client.player.AbstractClientPlayer` |
| `net.minecraft.client.network.ClientPlayerEntity` | `net.minecraft.client.player.LocalPlayer` |

### 文本/Chat 类

| Yarn | Mojang |
|------|--------|
| `net.minecraft.text.Text` | `net.minecraft.network.chat.Component` |
| `net.minecraft.text.Style` | `net.minecraft.network.chat.Style` |
| `net.minecraft.text.OrderedText` | `net.minecraft.util.FormattedCharSequence` |
| `net.minecraft.text.StringVisitable` | `net.minecraft.network.chat.FormattedText` |
| `net.minecraft.text.TranslatableTextContent` | `net.minecraft.network.chat.contents.TranslatableContents` |
| `net.minecraft.network.message.SignedMessage` | `net.minecraft.network.chat.PlayerChatMessage` |
| `net.minecraft.network.message.MessageType` | `net.minecraft.network.chat.ChatType` |
| `net.minecraft.network.message.MessageSignatureData` | `net.minecraft.network.chat.MessageSignature` |

### 工具类

| Yarn | Mojang |
|------|--------|
| `net.minecraft.util.math.MathHelper` | `net.minecraft.util.Mth` |
| `net.minecraft.util.StringHelper` | `net.minecraft.util.StringUtil` |
| `net.minecraft.util.Colors` | `net.minecraft.util.ARGB` |
| `net.minecraft.util.Formatting` | `net.minecraft.ChatFormatting` |
| `net.minecraft.util.Identifier` | `net.minecraft.resources.Identifier` |
| `net.minecraft.client.util.math.Rect2i` | `net.minecraft.client.gui.navigation.ScreenRectangle` |
| `net.minecraft.client.util.math.MatrixStack` | `org.joml.Matrix3x2f` |

### 渲染类

| Yarn | Mojang |
|------|--------|
| `net.minecraft.client.render.RenderLayer` | `net.minecraft.client.renderer.RenderPipelines` |
| `net.minecraft.client.font.TextVisitFactory` | `net.minecraft.util.StringDecomposer` |

## 方法名变更

| Yarn (1.21.1) | Mojang (26.1) | 类 |
|---|---|---|
| `MinecraftClient.getInstance()` | `Minecraft.getInstance()` | Minecraft |
| `Text.literal(...)` | `Component.literal(...)` | Component |
| `Text.translatable(...)` | `Component.translatable(...)` | Component |
| `Text.empty()` | `Component.empty()` | Component |
| `Identifier.of(ns, path)` | `Identifier.fromNamespaceAndPath(ns, path)` | Identifier |
| `component.getContent()` | `component.getContents()` | Component |
| `client.textRenderer.fontHeight` | `client.font.lineHeight` | Font |
| `client.getWindow().getScaledWidth()` | 待确认 | |
| `DrawContext.drawText(...)` | `GuiGraphics.drawString(...)` | 待确认 |

## Mixin 目标类变更

所有 `@Mixin` 目标需要更新为 Mojang 类名。详见 `mixin-audit.md`。

## 已知阻塞项

1. **MC MCP 无法反编译 26.1** — 使用 Loom genSources 替代
2. **owo-lib 0.13.0+26.1 API 变更** — 需要审查所有 owo 使用
3. **Vosk 0.3.45 与 Java 25 兼容性** — 待验证
4. **兼容模组** — Chat Heads, More Chat History, ChatAnimation, Smooth Scrolling 的 26.1 版本状态待确认
