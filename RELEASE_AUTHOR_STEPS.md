# Chat Canvas v1.1.0 Release — 作者操作说明

## ✅ 已自动完成

- 版本号 `gradle.properties`: 1.0.0 → **1.1.0**
- `README.md` 完整重写（简体中文）
- `README_EN.md` 完整重写（英文对等）
- `CHANGELOG.md` 更新 1.1.0
- `RELEASE_NOTES_1.1.0.md` 双语 Release Notes
- `RELEASE_CHECKLIST.md` 更新
- `./gradlew clean build`: **BUILD SUCCESSFUL**
- JAR: `build/libs/chat-canvas-1.1.0.jar` (约 1.9 MB)
- SHA-256: `build/libs/chat-canvas-1.1.0.jar.sha256`
- Git 提交: `6c4fea3` — "release: prepare Chat Canvas v1.1.0"
- Git 标签: `v1.1.0` — 已推送到 origin
- 所有代码已推送: `git push origin main` ✅

## 🔧 需要作者手动执行

### 1. 登录 GitHub CLI
```bash
gh auth login
```

### 2. 创建 GitHub Release
```bash
cd D:\chat-canvas-template-1.21.1

gh release create v1.1.0 \
  "build/libs/chat-canvas-1.1.0.jar" \
  "build/libs/chat-canvas-1.1.0.jar.sha256" \
  --repo ikunkk02-afk/Chat-Canvas \
  --title "Chat Canvas v1.1.0" \
  --notes-file RELEASE_NOTES_1.1.0.md \
  --latest
```

或通过 GitHub 网页端:
1. 打开 https://github.com/ikunkk02-afk/Chat-Canvas/releases/new
2. Tag: `v1.1.0`
3. 标题: `Chat Canvas v1.1.0`
4. 将 `RELEASE_NOTES_1.1.0.md` 内容粘贴到描述
5. 上传 `build/libs/chat-canvas-1.1.0.jar`
6. 上传 `build/libs/chat-canvas-1.1.0.jar.sha256`
7. 不勾选 Pre-release
8. 勾选 "Set as the latest release"

---

## JAR 信息
- **文件**: `chat-canvas-1.1.0.jar`
- **大小**: ~1.9 MB
- **SHA-256**: `95213553d6836207419b98e211a0a223f96fe39446bcd39d7711cc42409935d2`
