[English](README.md) | [简体中文](README_zh_CN.md)

<p align="center">
  <img src="src/main/resources/assets/chat_canvas/icon.png" width="180" alt="Chat Canvas">
</p>

# Chat Canvas｜聊天画布

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-green)
![Loader](https://img.shields.io/badge/Loader-Fabric-lightyellow)
![Side](https://img.shields.io/badge/Side-Client--only-blue)
![Java](https://img.shields.io/badge/Java-21%2B-orange)
![Version](https://img.shields.io/badge/Version-1.3.0-informational)
![License](https://img.shields.io/badge/License-MIT-brightgreen)

Chat Canvas 是一款针对 **Minecraft 1.21.11（Fabric）** 的纯客户端聊天增强与重设计模组，重点改善聊天阅读、聊天输入与界面布局，并加入 Emoji、离线语音输入、本地聊天记录，以及服务器交互消息的可靠支持。

所有设置都可以在游戏内的可视化编辑器中完成，无需手动编辑配置文件。

## 1.3.0 更新内容

- 重构离线语音输入，支持多套语音识别模型
- VAD 自动断句（语音开始与结束自动检测）
- 模型管理器：SHA-256 校验下载、模型热切换
- Android / iOS 语音兼容层
- 扩充 Emoji 表情面板（130+ 表情、10 个分类）
- 新增繁體中文（台灣）
- 改进服务器交互消息支持（ClickEvent / HoverEvent）
- 重做设置界面样式，适配不同分辨率

## 功能

### 聊天界面

- 可视化聊天编辑器，游戏内实时预览
- 拖拽移动聊天栏，八方向缩放，边缘与屏幕中心吸附
- **两套可切换的编辑器视觉主题**：Chat Canvas 现代主题 / Minecraft 原版风格，布局、功能与配置完全相同
- **双聊天频道**：玩家聊天与命令/系统消息栏，位置、大小、样式和滚动相互独立
- **左右分栏布局**：经典 / 左右分栏两种模式，自己与其他玩家的消息区分显示，可自定义消息宽度
- 通过 [Chat Heads](https://modrinth.com/mod/chat-heads) 显示玩家头像（本模组不内置头像）
- 文字大小、行间距、字间距、透明度、阴影与对齐
- 消息背景颜色、透明度、显示模式与内边距；输入框背景与边框
- 编辑时撤销 / 重做（Ctrl+Z / Ctrl+Y）

### 玩家颜色、艾特与快捷操作

- 按玩家 UUID 自动稳定分配颜色，支持自定义调色板和手动覆盖
- 可选择保留服务器原始名字颜色
- 双击玩家名字在光标处插入 `@名字`；艾特高亮支持自定义颜色与粗体
- 艾特通知：提示音、Toast 弹窗或全屏闪烁（默认忽略自己的消息）
- 右键玩家名字快捷菜单：艾特、私聊模板（`/msg`、`/tell`、`/w`）、复制名字
- 所有快捷操作只填写输入框，不会自动发送消息

## 服务器交互消息

Chat Canvas 保留原版组件交互，包括：

- `ClickEvent`：执行命令、建议命令、打开 URL、复制到剪贴板
- `HoverEvent`：悬浮提示

因此 `/tpa` 接受/拒绝按钮、可点击链接、可复制文本等服务器功能在重设计的聊天界面中依然可用。具体行为取决于服务器使用原版组件发送的内容。

## 语音输入

按 **V** 打开聊天并开始语音输入，再按一次 **V** 结束。安装模型后语音识别完全离线运行。

- 无需长按：按 V 开始，再按 V 结束，也可让 VAD 自动判断句子结束
- Silero VAD 端点检测，可配置监听超时、断句静音时长与尾部补录
- 说话时实时显示部分识别结果，最终结果插入光标位置
- 识别结果不会自动发送，可先检查再按回车
- 麦克风设备选择与音量测试
- 可选自动添加句末标点，以及聊天长度限制处理
- 最大录音时长、噪声阈值、识别线程数设置

## 离线语音模型

Chat Canvas 不会把语音模型打包进模组 JAR。首次使用时，模型管理器会提供以下模型供选择（仅在你确认后下载）：

| 模型 | 语言 | 大小 | 定位 |
|---|---|---|---|
| Streaming Zipformer 中文 | 普通话 | 约 30 MB | 低资源占用、流式、速度极快，适合低配设备与移动端 |
| SenseVoice INT8 | 普通话、粤语、英语、日语、韩语 | 约 240 MB | 均衡型；质量更高、响应快，离线解码 |
| Whisper Tiny INT8 | 多语言 | 约 104 MB | 离线解码，多语言覆盖广 |
| Vosk Small 中文（旧版） | 普通话 | 约 44 MB | 流式；兼容性选项，仅支持桌面 x86-64 |

Zipformer 与 Vosk 在说话过程中流式输出部分结果；SenseVoice 与 Whisper 在 VAD 检测到句子结束后解码。

## 模型管理器

- 在语音设置页选择模型，显示下载进度与体积
- 所有下载均经过 SHA-256 校验
- 已安装模型之间切换为热切换，无需重启游戏
- 可取消下载、释放已加载模型、一键打开模型目录

## 移动端支持

语音系统包含面向移动端启动器的兼容层：

- **Android**（FCL / Pojav 类 Java Minecraft 环境）：支持 ARM64 与 ARM32，原生运行库下载后暂存到应用私有缓存。
- **iOS arm64**：sherpa-onnx 原生运行库需由启动器通过运行时桥接提供。

麦克风与原生库的可用性取决于启动器（麦克风权限、原生运行库支持）。Chat Canvas 会在运行时检测能力，环境不支持时安全禁用语音输入，模组其余功能不受影响。

## Emoji 表情

- 130+ 表情，10 个分类（笑脸、人物、动物、食物、活动、旅行、物品、符号、爱心、最近使用）
- 搜索、最近使用记录（跨会话保存）、多语言表情名称
- 虚拟化滚动网格，支持悬浮提示与键盘操作
- 当前字体无法渲染的表情会自动隐藏

## 聊天记录

- 玩家聊天消息本地保存至 `.minecraft/chatcanvas/chat-logs/`，按世界/服务器分目录隔离
- UTF-8 JSON Lines 格式，按天与按大小自动轮换，异步写入不阻塞游戏
- 可分别开关保存自己的消息、他人消息与命令/系统频道
- 可配置保留天数（0 = 永久保留），一键打开日志目录

聊天记录为纯本地文件：没有云同步，也没有内置的历史查看器。

## 命令输入

- 输入 `/` 自动切换为命令模式，使用原版（Brigadier）命令建议
- 命令工具面板（命令模式下 Ctrl+F）：最近执行、可拖拽排序的收藏、常用预设、系统剪贴板
- 搜索、编辑、删除、分类管理已保存命令
- 光标插入或替换输入框；敏感命令（如 `/login`、`/password`）会提示并不写入日志
- 命令只会填入输入框，按回车后才由你自己执行

## 语言

- English (en_us)
- 简体中文 (zh_cn)
- 繁體中文（台灣） (zh_tw)

## 响应式界面

编辑器适配 Minecraft 的逻辑 GUI 尺寸，在各类 GUI Scale、窗口尺寸、常见屏幕比例（16:9、16:10、4:3）以及窗口化/全屏模式下均可正常使用。设置面板会自动避开聊天预览区域。

## 按键

| 按键 | 功能 |
| --- | --- |
| K | 打开/关闭 Chat Canvas 编辑器 |
| V | 开始 / 结束语音输入 |
| T | 打开聊天（原版） |
| / | 命令输入（原版） |
| Ctrl+E | 打开/关闭 Emoji 表情面板 |
| Ctrl+F | 命令工具 / Emoji 搜索 |
| Ctrl+Z / Ctrl+Y | 编辑器内撤销 / 重做 |

## 安装方法

1. 为 Minecraft 1.21.11 安装 [Fabric Loader](https://fabricmc.net/use/)
2. 安装 [Fabric API](https://modrinth.com/mod/fabric-api) 与 [owo-lib](https://modrinth.com/mod/owo-lib)
3. 从 [Releases](https://github.com/ikunkk02-afk/Chat-Canvas/releases) 下载 Chat Canvas JAR，放入 `.minecraft/mods/`
4. 使用 Java 21 或更高版本启动游戏
5. 可选：安装 [Mod Menu](https://modrinth.com/mod/modmenu)，在游戏内获得配置入口

## 环境要求

| 依赖 | 类型 | 版本 |
|---|---|---|
| Minecraft | 必需 | 1.21.11 |
| Java | 必需 | 21 或更高 |
| Fabric Loader | 必需 | 0.19.3 或更高 |
| Fabric API | 必需 | 0.141.6+1.21.11 或兼容版本 |
| owo-lib | 必需 | 0.13.0+1.21.11 或兼容版本 |
| Mod Menu | 可选 | 11.0.4 或兼容版本 |

## 语音模型存储位置

语音模型在运行时下载，不会打包进模组 JAR。

- 模型：`config/chatcanvas/voice-models/`
- sherpa-onnx 原生运行库与 Silero VAD：`config/chatcanvas/voice-runtime/sherpa-onnx/<版本>/`

删除对应文件夹即可释放磁盘空间，需要时模型管理器会提示重新下载。

## 隐私说明

- 安装离线模型后，**语音识别完全在本机运行**。麦克风与解码仅在语音会话期间启用，音频不会上传，也不会保存。
- 网络仅用于你主动发起的下载（模型与原生运行库）；首次配置语音时需要联网。
- 聊天记录与已保存命令以本地明文 JSON 存储。请勿在公用电脑上保存密码或令牌（如 `/login`、`/register`、`/password`）。

## 兼容性

Chat Canvas 为**纯客户端模组**，服务器无需安装，可与原版、Fabric、Paper 或 Spigot 服务器配合使用。它不会绕过服务器权限，命令仍由服务器正常处理。

与其他聊天模组已确认可共存：

- [Chat Heads](https://modrinth.com/mod/chat-heads) — 头像渲染进 Chat Canvas 布局
- [More Chat History](https://modrinth.com/mod/morechathistory) — 聊天历史容量交给 More Chat History 管理
- [ChatAnimation](https://modrinth.com/mod/chatanimation) — 消息进入动画交给 ChatAnimation
- [Smooth Scrolling](https://modrinth.com/mod/smooth-scroll) — 滚动动画交给 Smooth Scrolling

## 已知限制

- 仅支持 Fabric 1.21.11，暂无 Forge / NeoForge 版本。
- iOS 上语音输入需要启动器提供已签名的 sherpa-onnx 运行时桥接；Android 上依赖启动器提供可用的麦克风与原生库加载。环境不支持时语音会安全禁用，其余功能正常。
- 旧版 Vosk 模型仅支持桌面 x86-64。
- 部分服务器插件把玩家消息改写成系统消息时，发送者 UUID 可能丢失，玩家颜色与名字快捷操作无法可靠生效。
- Emoji 显示取决于当前字体，不支持的字符会自动隐藏。
- 聊天记录为纯本地文件，没有内置查看器，也不支持设备间同步。

## 配置

在游戏中按 **K**（或在 Mod Menu 中打开 Chat Canvas）进入编辑器，共 8 个设置分类：

- 布局 — 位置、大小、频道与分栏布局
- 文字 — 大小、间距、透明度、阴影、对齐
- 背景 — 消息与输入框背景
- 玩家颜色 — 自动配色、调色板与手动覆盖
- 艾特 — 双击行为、高亮与通知
- 命令 — 命令工具与已保存命令
- 语音 — 语音输入、模型管理器与麦克风设置
- 聊天记录 — 本地聊天记录选项

## 截图

1.3.0 的截图将随发布资源一并添加。

## 致谢

- [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx) — 离线语音识别运行库与 Silero VAD 模型
- [Vosk](https://alphacephei.com/vosk/) — 旧版中文小型语音模型
- 语音模型经由 [Hugging Face](https://huggingface.co/csukuangfj) 分发（运行时下载）
- [Fabric API](https://github.com/FabricMC/fabric)、[owo-lib](https://github.com/wisp-forest/owo-lib)、[Mod Menu](https://github.com/TerraformersMC/ModMenu)

依赖许可证详见 [THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md)。

## 许可证

[MIT](LICENSE) — Copyright &copy; 2026 寿云
