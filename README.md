[简体中文](README.md) | [English](README_EN.md)

# Chat Canvas｜聊天画布

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green)
![Mod Loader](https://img.shields.io/badge/Loader-Fabric-lightyellow)
![Environment](https://img.shields.io/badge/Environment-Client-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

**Chat Canvas（聊天画布）** 是一款适用于 Minecraft 1.21.1 Fabric 的**纯客户端**聊天栏自定义模组。它通过可视化编辑器让玩家直接拖动、缩放并实时预览聊天栏，同时提供文字、背景、玩家颜色、艾特通知、玩家快捷操作和持久化命令剪贴板。

---

## 目录

- [核心特色](#核心特色)
- [安装要求](#安装要求)
- [安装方法](#安装方法)
- [快速开始](#快速开始)
- [可视化编辑器](#可视化编辑器)
- [文字设置](#文字设置)
- [背景设置](#背景设置)
- [玩家颜色](#玩家颜色)
- [艾特和通知](#艾特和通知)
- [玩家快捷操作](#玩家快捷操作)
- [命令剪贴板](#命令剪贴板)
- [配置和数据](#配置和数据)
- [兼容性](#兼容性)
- [已知限制](#已知限制)
- [常见问题](#常见问题)
- [故障排查](#故障排查)
- [开发和构建](#开发和构建)
- [项目结构](#项目结构)
- [作者与视频主页](#作者与视频主页)
- [许可证](#许可证)

---

## 核心特色

### 可视化编辑器
- 实时游戏画面预览，所见即所得
- 拖动聊天框自由移动位置
- 八方向缩放调整大小
- 屏幕边缘和中心自动吸附对齐
- 设置面板自动避让（检测聊天框位置后左右换边）
- 撤销（Ctrl+Z）和重做（Ctrl+Y）
- 保存和取消，无需手动修改配置文件

### 高度可自定义的文字
- 文字大小、行间距、字间距独立调节
- 文字透明度和阴影开关
- 左对齐、居中、右对齐
- 中文、英文和 Emoji 正常渲染
- 正确换行和点击检测，链接点击和 HoverEvent 不受影响

### 背景自定义
- 消息背景颜色和透明度
- 跟随文字、整行宽度、隐藏三种显示模式
- 水平和垂直内边距
- 输入框背景颜色、透明度和边框独立设置

### 玩家名字颜色
- 基于 UUID 的稳定自动颜色分配
- 可手动覆盖单个玩家的颜色
- 保留服务器原始颜色模式（不覆盖服务器设定的彩色名字）
- 在线玩家搜索和实时预览
- 不会给 Chat Heads 头像染色

### 艾特和通知
- 双击聊天中玩家名字自动插入 `@玩家名`
- 艾特文本高亮（可自定义颜色和粗体）
- 艾特提示音（多种音效可选，可调音量和音高）
- Toast 通知（可调摘要长度）
- 全屏闪烁提醒（可调颜色、透明度和时长）
- 可选择忽略自己发送的艾特
- 每种通知方式可单独开关

### 玩家快捷操作
- 右键聊天中玩家名字打开快捷菜单
- 艾特玩家、私聊玩家、复制玩家名字
- 自定义私聊命令模板（支持 `/msg {player}`、`/tell {player}`、`/w {player}`）
- 所有操作只填入输入框，**不自动发送**

### 命令剪贴板
- 全局客户端持久化，不绑定世界或服务器
- 保存当前输入框命令、自定义名称和分类
- 收藏、搜索、编辑、删除和调整顺序
- 最近使用和使用次数统计
- 内置常用命令预设（游戏模式、时间、天气、死亡不掉落等）
- 替换输入框或插入光标位置两种模式
- Shift 点击临时反转插入模式
- **永远不会自动执行命令**
- 敏感命令明文存储警告
- 退出世界或重启游戏后命令仍然保留

---

## 安装要求

| 依赖 | 类型 | 说明 |
|------|------|------|
| Minecraft | 必需 | 1.21.1 |
| Java | 必需 | ≥ 21 |
| Fabric Loader | 必需 | ≥ 0.19.3 |
| Fabric API | 必需 | ≥ 0.116.14+1.21.1 |
| owo-lib | 必需 | ≥ 0.12.15.4+1.21 |
| Mod Menu | 可选 | 从模组菜单打开编辑器 |

---

## 安装方法

1. 安装 Minecraft 1.21.1
2. 安装 [Fabric Loader](https://fabricmc.net/use/)（≥ 0.19.3）
3. 安装 [Fabric API](https://modrinth.com/mod/fabric-api)（≥ 0.116.14）
4. 安装 [owo-lib](https://modrinth.com/mod/owo-lib)（≥ 0.12.15.4）
5. 将 Chat Canvas JAR 文件放入 `mods` 文件夹
6. 启动游戏

> Chat Canvas 是**客户端模组**，服务器**不需要**安装。

---

## 快速开始

1. 进入游戏世界
2. 按默认快捷键 **K** 打开编辑器
3. 拖动预览聊天框到想要的位置
4. 拖动边缘调整大小
5. 切换布局、文字、背景、玩家颜色、艾特、命令输入等分类页面
6. 点击**保存**
7. 按 **T** 打开真实聊天界面验证效果
8. 双击玩家名字进行艾特
9. 右键玩家名字打开快捷菜单
10. 点击输入框旁的**命令**按钮打开命令剪贴板

---

## 可视化编辑器

编辑器分为左右两个区域：
- **左侧**：聊天预览区，可以直接拖动聊天框、调整大小，所见即所得
- **右侧**：设置面板，包含六个分类页面

### 数值拖拽

- 鼠标放在数值上，按住**左键**左右拖动
- **滚轮**微调（±1）
- 按住 **Shift** 精细调整（±0.1）
- 按住 **Ctrl** 快速调整（±10）
- **右键**恢复该数值的默认值

### 撤销和重做

- 顶部工具栏提供**撤销**和**重做**按钮
- 快捷键 **Ctrl+Z** 撤销，**Ctrl+Y** 重做

---

## 文字设置

| 选项 | 说明 | 范围 |
|------|------|------|
| 文字大小 | 聊天文字缩放 | 0.5 – 2.0 |
| 行间距 | 行与行之间的间距 | 0 – 20px |
| 字间距 | 字符之间的额外间距 | 0 – 8px |
| 文字透明度 | 文字不透明度 | 0 – 100% |
| 文字阴影 | 文字阴影开关 | 开 / 关 |
| 文字对齐 | 左对齐、居中、右对齐 | — |

> 字间距为 0 时，文字渲染与原版完全一致，不会产生额外性能开销。

---

## 背景设置

### 消息背景

| 选项 | 说明 |
|------|------|
| 显示模式 | 跟随文字 / 整行宽度 / 隐藏 |
| 背景颜色 | 自定义 RGB 颜色 |
| 背景透明度 | 0 – 100% |
| 水平内边距 | 文字与背景左右间距 |
| 垂直内边距 | 文字与背景上下间距 |

### 输入框背景

| 选项 | 说明 |
|------|------|
| 输入框颜色 | 自定义 RGB 颜色 |
| 输入框透明度 | 0 – 100% |
| 输入框边框 | 开关和颜色设置 |

---

## 玩家颜色

### 自动分配模式

基于玩家 UUID 计算稳定的 HSV 颜色，同一玩家在不同服务器中颜色一致。

### 原始颜色模式

保留服务器设定的玩家名字颜色，不分配新颜色。

### 手动覆盖

在线玩家列表支持：
- 搜索玩家名字
- 点击色块为特定玩家设置自定义颜色
- 右键重置为自动颜色
- 自定义 24 色调色板

---

## 艾特和通知

### 艾特插入

- 在聊天界面中**双击**玩家名字 → 自动插入 `@玩家名` 到输入框
- 仅插入，不自动发送
- 可设置双击时间间隔

### 艾特高亮

- 收到包含 `@你的名字` 的消息时，该条消息高亮
- 可自定义高亮颜色和是否粗体
- 可选择仅识别带 `@` 符号的艾特

### 通知方式

三种通知方式可独立开关：

1. **提示音**：多种音效可选（经验球、音符盒、紫水晶、按钮点击），可调音量和音高
2. **Toast**：屏幕顶部弹出消息摘要
3. **全屏闪烁**：短暂的全屏颜色闪烁提醒

---

## 玩家快捷操作

在聊天界面中**右键**玩家名字，弹出快捷菜单：

| 操作 | 效果 |
|------|------|
| 艾特玩家 | 在输入框插入 `@玩家名` |
| 私聊玩家 | 使用模板插入 `/msg 玩家名 ` |
| 复制玩家名字 | 复制到剪贴板 |

私聊模板可自定义，支持 `{player}` 占位符。默认 `/msg {player} `。

> 所有操作**只填入输入框，不自动发送**，玩家必须手动确认。

---

## 命令剪贴板

Chat Canvas 提供一个**持久化命令剪贴板**，独立于世界和服务器。

### 核心功能

- 保存当前输入的命令
- 自定义命令名称和分类
- 收藏常用命令
- 全文搜索
- 编辑命令名称和内容
- 删除、排序和批量清理
- 最近使用和使用次数统计

### 内置预设

| 预设 | 命令 |
|------|------|
| 生存模式 | `/gamemode survival` |
| 创造模式 | `/gamemode creative` |
| 旁观模式 | `/gamemode spectator` |
| 设为白天 | `/time set day` |
| 设为夜晚 | `/time set night` |
| 晴天 | `/weather clear` |
| 雨天 | `/weather rain` |
| 开启死亡不掉落 | `/gamerule keepInventory true` |
| 关闭死亡不掉落 | `/gamerule keepInventory false` |
| 和平难度 | `/difficulty peaceful` |
| 简单难度 | `/difficulty easy` |
| 普通难度 | `/difficulty normal` |
| 困难难度 | `/difficulty hard` |
| 设置出生点 | `/spawnpoint` |
| 自杀 | `/kill` |

> 点击命令**只填入输入框，不自动执行**。按 Enter 后由游戏或服务器处理。

### 插入模式

- **替换输入框**：点击命令替换输入框全部内容
- **插入光标位置**：点击命令插入到当前光标位置
- 按住 **Shift** 点击可临时反转插入模式

---

## 配置和数据

### 配置文件位置

主配置：
```
.minecraft/config/chat_canvas.json
```

命令剪贴板：
```
.minecraft/config/chat_canvas/command_clipboard.json
```

### 说明

- 两者都是**客户端全局数据**，不位于世界存档
- 删除主配置文件会恢复所有默认设置
- 删除命令剪贴板文件会清空所有用户保存的命令
- 修改前建议备份
- 游戏运行时**不建议**手动编辑
- 配置损坏时模组会尝试自动备份原文件并创建默认配置

> [!WARNING]
> 命令剪贴板以本地明文 JSON 保存命令。请勿在公用电脑或不受信任的环境中保存包含密码、令牌或隐私信息的命令，例如 `/login`、`/register` 和 `/password`。

Chat Canvas 不会上传命令，不会将命令发送给作者，点击预设不会自动执行。按 Enter 后命令才由游戏或服务器处理。

---

## 兼容性

Chat Canvas 针对以下模组进行了兼容设计，但不同版本组合仍可能存在差异。

| 模组 | 兼容说明 |
|------|----------|
| **Chat Heads** | Chat Canvas 不提供头像功能，会保留 Chat Heads 的头像，并尽量让头像与文字布局、背景、对齐和点击区域保持一致 |
| **More Chat History** | Chat Canvas 不修改聊天历史容量，由 More Chat History 负责历史上限 |
| **ChatAnimation** | Chat Canvas 不提供聊天消息进入动画，消息动画交由 ChatAnimation 处理 |
| **Smooth Scrolling** | Chat Canvas 不实现重复的聊天滚动动画，滚动由 Smooth Scrolling 处理 |

> 以上兼容模组**不是强制依赖**，未安装时 Chat Canvas 正常运行。安装了它们的玩家可获得更好的综合体验。

---

## 已知限制

1. 仅支持 **Fabric 1.21.1**，不支持 Forge、NeoForge 或其他 Minecraft 版本
2. 纯客户端模组，无法在专用服务器上运行
3. 服务器插件（如聊天格式插件）可能将玩家聊天转换成系统消息，导致玩家身份无法可靠识别，此时玩家颜色和艾特命中可能不生效
4. 无法可靠识别发送者时，玩家名字快捷操作可能不适用
5. 服务器是否支持 `/msg`、`/tell` 或 `/w` 由服务器决定，Chat Canvas 不绕过权限
6. 命令是否有权限执行由服务器决定
7. 自定义资源包和字体可能改变文字宽度和换行效果
8. 命令剪贴板保存为明文 JSON
9. Chat Canvas 不提供玩家头像功能
10. Chat Canvas 不提供聊天消息进入和滚动动画

---

## 常见问题

### 为什么安装后没有配置按钮？

Mod Menu 是可选依赖。可以安装 [Mod Menu](https://modrinth.com/mod/modmenu)，或者使用 Chat Canvas 默认快捷键 **K** 直接打开编辑器。

### 为什么命令点击后没有立即执行？

这是**安全设计**。Chat Canvas 只把命令填入聊天输入框，玩家必须自行确认并按 **Enter**。模组永远不会自动执行命令。

### 为什么某些服务器中玩家名字没有颜色？

服务器插件可能使用系统消息发送聊天内容，客户端无法可靠获得消息发送者的 UUID。这种情况同样会影响艾特命中。

### 为什么艾特没有通知？

请依次检查：
- 消息中是否使用了完整的 `@玩家名`
- 设置中 `requireAtSymbol` 是否开启（开启时只识别带 `@` 符号的艾特）
- 提示音、Toast、闪烁各项开关是否已开启
- 是否为自己发送的消息（默认忽略自己的艾特）
- 服务器是否修改了聊天格式

### 为什么保存的命令没有消失？

命令剪贴板是**全局持久化数据**，不绑定世界或服务器。可以在命令管理中清理，或备份后手动删除配置文件。

### Chat Canvas 需要服务器安装吗？

**不需要**。Chat Canvas 是纯客户端模组。

### 能否在 Forge 或 NeoForge 使用？

目前不能。当前版本只支持 Fabric 1.21.1。

### 是否包含 Chat Heads 功能？

不包含。可以单独安装 [Chat Heads](https://modrinth.com/mod/chat-heads)。

### 是否包含聊天动画？

不包含。可以使用 [ChatAnimation](https://modrinth.com/mod/chat-animation) 或 [Smooth Scrolling](https://modrinth.com/mod/smooth-scrolling)。

---

## 故障排查

### 基本检查

遇到问题时请先确认：
1. Minecraft 是否为 **1.21.1**
2. Fabric Loader 版本是否匹配
3. Fabric API 版本是否匹配（≥ 0.116.14）
4. owo-lib 版本是否匹配（≥ 0.12.15.4）
5. 是否误装了 NeoForge 或 Forge 版本的依赖
6. 是否使用旧版 Chat Canvas 配置
7. 是否有自定义字体或资源包影响
8. 是否有其他修改 ChatHud 或 ChatScreen 的模组

### 排查流程

1. 备份 `config/chat_canvas.json` 和 `config/chat_canvas/`
2. 删除或重命名 Chat Canvas 配置文件
3. 在仅保留 Fabric API、owo-lib、Chat Canvas 的环境下测试
4. 分批恢复其他模组，定位冲突来源
5. 提交 `latest.log`、完整模组列表、截图和复现步骤

---

## 开发和构建

### 环境要求
- Java 21
- Git

### 克隆和构建

**Windows：**
```powershell
git clone https://github.com/ikunkk02-afk/Chat-Canvas.git
cd Chat-Canvas
.\gradlew.bat clean build
```

**Linux / macOS：**
```bash
git clone https://github.com/ikunkk02-afk/Chat-Canvas.git
cd Chat-Canvas
chmod +x gradlew
./gradlew clean build
```

### 运行客户端
```powershell
.\gradlew.bat runClient
```

### 构建产物
```
build/libs/chat-canvas-1.0.0.jar
```

### 技术栈
- Java 21
- [Fabric Loom](https://fabricmc.net/wiki/documentation:fabric_loom) 1.17
- [Yarn mappings](https://github.com/FabricMC/yarn) 1.21.1+build.3
- [Fabric API](https://github.com/FabricMC/fabric) 0.116.14
- [owo-lib](https://github.com/wisp-forest/owo-lib) 0.12.15.4

---

## 项目结构

```
src/
├── main/java/io/github/ikunkk02/chatcanvas/
│   ├── animation/          动画引擎（SpringValue、MotionPreset）
│   ├── chat/
│   │   ├── command/        命令剪贴板存储和预设
│   │   ├── identity/       玩家消息身份识别
│   │   ├── interaction/    艾特插入交互
│   │   ├── layout/         聊天栏布局变换和度量
│   │   ├── mention/        艾特匹配分析
│   │   ├── notification/   艾特通知事件
│   │   ├── style/          文字样式覆盖和索引
│   │   └── text/           字间距字形测量
│   ├── config/             配置系统
│   ├── editor/             编辑器会话、历史和快照
│   └── ui/                 通用 UI 组件
│
├── client/java/io/github/ikunkk02/chatcanvas/
│   ├── chat/
│   │   ├── command/        命令剪贴板 UI
│   │   ├── identity/       玩家颜色运行时
│   │   ├── interaction/    双击和快捷菜单
│   │   ├── layout/         布局运行时缓存
│   │   ├── notification/   通知控制器
│   │   ├── render/         聊天渲染管线
│   │   ├── style/          样式覆盖管线
│   │   └── text/           字间距渲染
│   ├── editor/             ChatCanvasEditorScreen
│   ├── integration/        ModMenu 集成
│   ├── mixin/client/       客户端 Mixin（12 个）
│   └── ui/                 AnimatedSettingsPanel、ClippedPageViewport 等
│
└── test/java/              26 个单元测试
```

---

## 作者与视频主页

Chat Canvas 由 **寿云** 开发和维护。

- [哔哩哔哩主页](https://space.bilibili.com/1832031043?spm_id_from=333.1007.0.0)
- [抖音主页](https://www.douyin.com/user/MS4wLjABAAAAXPEr9Q0OnMztYvgDTXt6H3g9_626CRAmMbX1L64pBkxbvbHR2ACMWmL55mIL0-Gi)
- [GitHub 源代码](https://github.com/ikunkk02-afk/Chat-Canvas)
- [问题反馈](https://github.com/ikunkk02-afk/Chat-Canvas/issues)

---

## 许可证

本项目基于 [MIT License](LICENSE) 开源。

你可以在遵守 MIT License 的前提下使用、修改和分发本项目。
