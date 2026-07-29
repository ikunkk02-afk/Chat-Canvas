# Test Report: Chat Canvas 1.2.0 for Minecraft 1.21.9 (Fabric)

## Test Environment

| Item | Value |
|------|-------|
| Minecraft | 1.21.9 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.134.1+1.21.9 |
| Yarn mappings | 1.21.9+build.1 |
| Fabric Loom | 1.17.17 |
| owo-lib | 0.12.24+1.21.9 |
| Mod Menu | 16.0.1 (optional) |
| Java | 21 (OpenJDK) |
| OS | Windows 10 |
| Gradle | 9.5.1 |

## Unit Tests

| Test Suite | Result | Notes |
|-----------|--------|-------|
| VoskResultParserTest | PENDING | |
| MicrophoneManagerLeaseConcurrencyTest | PENDING | |
| Pcm16MonoResamplerTest | PENDING | |
| VoiceTextSanitizerTest | PENDING | |
| VoskEncodingBootstrapTest | PENDING | |
| UnicodeTextNavigatorTest | PENDING | |
| SpacedAdvanceMathTest | PENDING | |
| MentionMatcherTest | PENDING | |
| ChatCanvasConfigTest | PENDING | |
| LayoutConfigTest | PENDING | |
| MentionConfigTest | PENDING | |
| PlayerColorConfigTest | PENDING | |
| ChatLogConfigTest | PENDING | |
| ChatTextConfigTest | PENDING | |
| CommandClipboardConfigTest | PENDING | |
| ChatBackgroundConfigTest | PENDING | |
| PlayerIdentityResolverTest | PENDING | |
| RuntimeChatBoundsTest | PENDING | |
| ChatHudTransformTest | PENDING | |
| ChatTextLayoutTest | PENDING | |
| ChatBackgroundMetricsTest | PENDING | |
| PlayerChatLayoutStrategyTest | PENDING | |
| ChatCanvasInputControllerTest | PENDING | |
| ChatCanvasMessageManagerTest | PENDING | |
| ChatChannelHistoryTest | PENDING | |
| DefaultMessageClassifierTest | PENDING | |
| MentionInteractionStateTest | PENDING | |
| MentionInsertionControllerTest | PENDING | |
| PrivateMessageTemplateTest | PENDING | |
| MentionMessageIdRegistryTest | PENDING | |
| MentionNotificationDeduplicatorTest | PENDING | |
| EmojiRegistryTest | PENDING | |
| EmojiRecentStorageTest | PENDING | |
| EmojiRecentManagerTest | PENDING | |
| DangerousCommandDetectorTest | PENDING | |
| SensitiveCommandDetectorTest | PENDING | |
| SensitiveCommandMaskerTest | PENDING | |
| CommandTextSanitizerTest | PENDING | |
| CommandToolManagerTest | PENDING | |
| ChatLogWriterTest | PENDING | |
| ChatLogContextTest | PENDING | |
| StoredChatMessageTest | PENDING | |

## Build

| Step | Result | Notes |
|------|--------|-------|
| `./gradlew compileJava` | PENDING | |
| `./gradlew compileClientJava` | PENDING | |
| `./gradlew test` | PENDING | |
| `./gradlew build` | PENDING | |

## runClient

| Test Item | Result | Notes |
|-----------|--------|-------|
| Main menu | PENDING | |
| Singleplayer world | PENDING | |
| Press T (player chat) | PENDING | |
| Press / (command mode) | PENDING | |
| Auto-switch / at first char | PENDING | |
| Player chat panel | PENDING | |
| Command/system panel | PENDING | |
| Classic layout | PENDING | |
| Left/right split layout | PENDING | |
| Current player right-aligned | PENDING | |
| Other players left-aligned | PENDING | |
| Word wrapping | PENDING | |
| Double-click @mention | PENDING | |
| @mention notification | PENDING | |
| Emoji panel | PENDING | |
| Unicode cursor/delete | PENDING | |
| Mouse voice input | PENDING | |
| Keyboard voice input | PENDING | |
| Chinese encoding | PENDING | |
| Microphone concurrent release | PENDING | |
| Local chat log | PENDING | |
| Resource cleanup on exit | PENDING | |
| Re-entry stability | PENDING | |

## Final JAR

| Check | Result | Notes |
|-------|--------|-------|
| fabric.mod.json present | PENDING | |
| Runtime deps included | PENDING | |
| No Vosk model | PENDING | |
| No test classes | PENDING | |
| No chat logs | PENDING | |
| No decompiled MC source | PENDING | |
| JAR name correct | PENDING | |
| SHA-256 | PENDING | |

## Compatibility Mods

| Mod | Version | Result | Notes |
|-----|---------|--------|-------|
| Chat Heads | PENDING | PENDING | |
| More Chat History | PENDING | PENDING | |
| ChatAnimation | PENDING | PENDING | |
| Smooth Scrolling | PENDING | PENDING | |

## Known Limitations

- `Screen.hasShiftDown()` / `hasControlDown()` static methods removed; replaced with `MinecraftClient.getInstance().isShiftPressed()`/`isCtrlPressed()`
- `DrawContext.drawBorder()` removed; replaced with fill-based rectangle borders
- `GameProfile` now uses record-style accessors `name()`/`id()` instead of `getName()`/`getId()`
- owo-lib `Component.zIndex()` API change requires verification
- Vosk/JNA compatibility to be verified in runtime testing
