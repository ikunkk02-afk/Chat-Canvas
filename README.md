# Chat Canvas

Chat Canvas（聊天画布）是一个 Fabric 客户端模组。当前版本提供独立的可视化聊天框编辑器基础，用于预览布局、拖拽、缩放、吸附、撤销/重做和设置面板动画，不会修改原版真实聊天栏。

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.19.3+
- Fabric API 0.116.14+
- owo-lib 0.12.15.4+
- Mod Menu 11.0.4+（可选）

## Usage

按 `K` 打开编辑器。快捷键可以在 Minecraft 控制设置中重新绑定。安装 Mod Menu 后，也可以从 Chat Canvas 的配置按钮打开同一编辑器。

编辑器中的示例消息仅用于预览，不会写入真实聊天历史。只有点击“保存”才会把布局写入 `config/chat_canvas.json`；取消或按 Esc 会丢弃本次编辑。

## Development

```powershell
.\gradlew.bat clean build
.\gradlew.bat runClient
.\gradlew.bat runClient -PwithModMenu=true
```

## License

MIT
