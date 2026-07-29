# Chat Canvas v1.1.0

---

## 主要更新 | Highlights

### 🎨 两套可切换的编辑器视觉主题 | Two Switchable Editor Visual Themes

Chat Canvas 现提供两套编辑器**视觉主题**（不是两套不同的编辑器）：

- **Chat Canvas 原生主题** — 半透明现代面板、圆角和自定义控件
- **Minecraft 原版风格主题** — Minecraft 风格按钮、边框、滑块和文本框

两套主题：
- ✅ 使用完全相同的编辑器布局
- ✅ 使用完全相同的功能
- ✅ 读写同一份配置文件
- ✅ 实时切换，不丢失未保存修改
- ✅ 不清空撤销和重做历史
- ✅ 主题偏好持久保存

> Chat Canvas now provides two editor **visual themes** (not two different editors):
> - **Chat Canvas Theme** — semi-transparent modern panels, rounded corners, custom controls
> - **Minecraft‑style Theme** — vanilla-style buttons, borders, sliders and text fields
>
> Both themes share the exact same layout, features, config, and editing session. Switching themes only changes the visual appearance.

---

### 🛠️ 命令剪贴板体验修复 | Command Clipboard UX Fix

- 面板打开时入口按钮自动隐藏，关闭后恢复
- 不再遮挡命令列表
- 不保留透明点击区域
- The launcher button now hides automatically while the clipboard panel is open

---

## 安装 | Installation

| 依赖 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader | 0.19.3+ |
| Fabric API | 0.116.14+1.21.1 |
| owo-lib | 0.12.15.4+1.21 |
| Mod Menu *(optional)* | 11.0.4+ |

## 升级说明 | Upgrade Notes

- 可直接覆盖旧 JAR 文件
- 建议备份 `config/chat_canvas.json`
- 旧配置文件会被正常读取
- 默认保留玩家上次使用的主题
- 首次加载时新增字段使用默认值
- You can overwrite the old JAR directly. Existing config files are read correctly. New fields default to their safe values.

## 已知限制 | Known Limitations

- 当前仅支持 Fabric 1.21.1
- 纯客户端模组
- 不提供玩家头像（请使用 Chat Heads）
- 命令剪贴板使用本地明文 JSON 存储
- Fabric 1.21.1 only, client-side. Player avatars require Chat Heads. The command clipboard uses local plain-text JSON.

---

**完整更新日志 / Full Changelog**: [CHANGELOG.md](CHANGELOG.md)
