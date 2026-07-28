# Chat Canvas Forge 1.20.1 — 移植终止报告

> **日期**: 2026-07-28  
> **分支**: `forge/1.20.1`  
> **仓库**: [ikunkk02-afk/Chat-Canvas](https://github.com/ikunkk02-afk/Chat-Canvas/tree/forge/1.20.1)  
> **终止原因**: 缺少 owo-lib 替代方案，编辑器 GUI 无法正常工作

---

## 一、移植完成度

| 类别 | 状态 | 说明 |
|------|------|------|
| **Forge MDK 工程** | ✅ | FG 6.x, Mojmap, Java 17, Mixin |
| **核心数据模型** | ✅ | config, animation, editor core |
| **Canvas UI 框架** | ✅ | 11 widgets (基于 CreativeCore) |
| **文本渲染管道** | ✅ | SpacedText, GlyphCache, ChatRenderContext |
| **布局引擎** | ✅ | ChatLayoutRuntime, ChatLineWidthCache |
| **消息入口** | ✅ | ChatCanvasMessageIngress |
| **通知系统** | ✅ | MentionController, FlashOverlay, SoundPlayer |
| **兼容层** | ✅ | ChatCanvasCompat |
| **Emoji 系统** | ✅ | Registry, RecentManager, PickerPanel |
| **命令工具** | ✅ | Manager, Storage, ClipboardPanel |
| **语音输入** | ✅ | 23 files + Vosk 0.3.45 (JarJar) |
| **编辑器屏幕** | ✅ | ChatCanvasEditorScreen + K 键打开 |
| **聊天记录** | ✅ | JSONL writer + rotation |
| **渲染链** | ✅ | ChatRenderEngine + DualChatHudRenderer |
| **右键菜单** | ✅ | PlayerQuickActionMenu (Canvas UI) |
| **双频道 HUD** | ✅ | 5 active Mixins |
| **AnimatedSettingsPanel** | ❌ | 1938 行 owo 深度依赖，未移植 |
| **PreviewChatWidget** | ❌ | owo 深度依赖，未移植 |
| **PlayerChatCapture** | ❌ | 需 Forge 事件适配 + Mixin 桥接 |

### 编译统计

```
主源码:  200 files
编译类:  259 classes
活跃 Mixin: 5
Jar 大小: 2.0 MB
构建:    ./gradlew clean build ✅
Commits: 29 (branch forge/1.20.1)
```

### 5 个活跃 Mixin

| Mixin | Target | 功能 |
|-------|--------|------|
| `ChatComponentMixin` | ChatComponent.render() | 双频道 HUD 注入 |
| `ChatScreenMixin` | ChatScreen | Ctrl+E/T 面板开关 |
| `KeyboardHandlerMixin` | keyPress() | 语音快捷键 |
| `ClientPacketListenerMixin` | 玩家列表事件 | 玩家名册追踪 |
| `ChatComponentAccessor` | @Invoker rescaleChat | 聊天缩放刷新 |

---

## 二、终止原因分析

### 2.1 owo-lib 依赖全貌

Fabric 原版深度使用 [owo-lib](https://github.com/wisp-forest/owo-lib) 的以下 API：

| owo 类 | 用途 | 涉及文件 | 行数 |
|--------|------|---------|------|
| `BaseOwoScreen` | 编辑器窗口基类 | ChatCanvasEditorScreen | ~750 |
| `FlowLayout` / `StackLayout` | 布局容器 | AnimatedSettingsPanel | ~1938 |
| `ScrollContainer` | 滚动面板 | AnimatedSettingsPanel | — |
| `Components.button/label/...` | UI 组件工厂 | AnimatedSettingsPanel | — |
| `OwoUIDrawContext` | 渲染上下文包装 | ChatRenderContext → 已替换 ✅ | 38 |
| `BaseComponent` | 自定义组件基类 | PreviewChatWidget | ~289 |
| `NumericScrubber` | 数值滑块 | AnimatedSettingsPanel | — |
| `TextBoxComponent` | 文本输入 | AnimatedSettingsPanel | — |
| `Sizing` / `Positioning` / `Insets` | 布局辅助 | AnimatedSettingsPanel | — |

### 2.2 已替换的 owo 依赖

| owo 原版 | Canvas UI 替代 |
|----------|---------------|
| `OwoUIDrawContext` | `GuiGraphics` (native) |
| `FlowLayout` | `CanvasFlowLayout` |
| `ButtonComponent` | `CanvasButton` |
| `ScrollContainer` | `CanvasScrollPanel` |
| `BaseComponent` | `CanvasWidget` |
| Tab bar | `CanvasTabBar` |
| Numeric scrubber | `CanvasNumericScrubber` |
| Color picker | `CanvasColorPicker` |

### 2.3 阻塞点

AnimatedSettingsPanel 是**阻塞器**：它包含 1938 行 owo 组件工厂调用（`Components.button(...)`, `Components.textBox(...)`, `FlowLayout.child(...)` 等），与 Canvas UI 的 API 完全不同。每一行都需要手工翻译。

此外，以下文件也需要 Canvas UI 重写但尚未进行：
- `PreviewChatWidget` (289 行)
- `PlayerChatCapture` (需 Forge 事件 + Mixin 桥接)

---

## 三、技术债务清单

| 文件 | 问题 | 建议方案 |
|------|------|---------|
| `ChatCanvasForge.java:29` | deprecated `FMLJavaModLoadingContext.get()` | 用 `ModLoadingContext.get()` |
| `ChatCanvasForge.java:61` | deprecated `ResourceLocation(String,String)` | 用 `ResourceLocation.of()` |
| `DualChatHudRenderer.java` | Client-server data path 未验证 | 需 `runClient` 测试 |
| ChatHeadsCompat | 已注释掉 | 用 CreativeCore 或手动渲染玩家头像 |
| Vosk JarJar | Vosk classes 未嵌入 jar | 需 Forge jarJar 配置调优 |
| voice unit tests (5 files) | 缺 JUnit 5 依赖 | 在 build.gradle 加 testImplementation |
| `ChatScreenMixin` | Ctrl+E/T 逻辑未端到端测试 | 需 `runClient` 验证 |

---

## 四、未来恢复建议

### 方案 A: 找 owo-lib 替代库 (推荐)

寻找一个 Forge 1.20.1 上的 UI 库替代 owo-lib：

| 候选库 | 优点 | 缺点 |
|--------|------|------|
| **Balm** (BlayTheNinth) | Forge 原生支持，配置 GUI | 不如 owo 灵活 |
| **Configured** | 自动生成配置界面 | 仅配置，非通用 UI |
| **Catalogue** | 模组菜单增强 | 非 UI 框架 |
| **自定义 Canvas UI** (当前) | 完全控制，无外部依赖 | 开发工作量大 |

### 方案 B: 重写编辑器为 Screen + 基础组件

将编辑器重写为简单的手写 Screen（像原版 Minecraft 设置界面一样），不用任何 UI 框架：

1. 用 `Screen` + `AbstractWidget` + `EditBox` + `Button`
2. 放弃动画面板，改用多个标签页 + 简单滑块
3. 参考原版 `OptionsScreen` 的布局模式

**优势**: 零外部依赖，长期稳定  
**劣势**: 没有动画过渡效果，UI 较简陋

### 方案 C: 扩展 Canvas UI 框架

当前 Canvas UI 已有 11 个 widget，但缺少：
- `TextBox` / `TextField` (输入框)
- Dropdown / ComboBox
- `CheckBox` / `Toggle`
- `Slider`
- `Tooltip`

补齐这些组件后，可以实现完整的设置面板。

---

## 五、Git 历史

```
5af57ba chore(forge): cleanup orphaned staging files
8756019 feat(forge): rewrite PlayerQuickActionMenu with Canvas UI
8359e85 feat(forge): port MentionToastManager + clean staging duplicates
424bfd9 feat(forge): port complete render chain + full DualChatHudRenderer
8407d0f chore(forge): clean staging duplicates, stage accessors for MCP verify
21e6dc6 feat(forge): port EmojiRuntime and CommandToolRuntime
04e0723 chore(forge): add ChatRenderContext + DualChatHudRenderer stub
38eec8a feat(forge): port VoiceInputOverlay with Mojmap-compatible rendering
d976863 feat(forge): port complete voice input system + Vosk dependency
feefc6d feat(forge): enable ChatScreenMixin with Emoji + Command panels
71ba59c feat(forge): port editor screen with Canvas UI + K-key binding
73f5568 feat(forge): add CreativeCore dependency and Canvas UI framework
c72c31e feat(forge): port notification modules
a11b0fd feat(forge): port text rendering pipeline
9141f74 feat(forge): port 112 core Java files
38d3f3c feat(forge): initialize Forge 1.20.1 workspace
```

---

## 六、总结

移植工作完成了 **约 85%** 的代码覆盖率（200/235 文件），编译通过且 jar 生成。但关键缺陷导致无法实际使用：

1. **AnimatedSettingsPanel 缺失** → 编辑器 GUI 混乱（用户反馈）
2. **owo-lib UI 组件未完全替换** → 部分面板空白或崩溃
3. **未进行 runClient 端到端测试** → 运行时行为未知

在不找到 owo-lib 替代方案的情况下，此项目无法进入可用状态。建议保留此仓库作为代码参考，等待社区出现成熟的 Forge UI 框架后再恢复移植。

---

*报告由 Hermes Agent 生成*  
*Branch: forge/1.20.1 · Commits: 29 · Last: 5af57ba*
