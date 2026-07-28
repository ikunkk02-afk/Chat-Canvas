# Test Report: Chat Canvas 1.2.0 for Minecraft 1.21.3

## Environment

| Item | Value |
|------|-------|
| Minecraft | 1.21.3 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.106.1+1.21.3 |
| Fabric Loom | 1.17.17 (remap) |
| owo-lib | 0.12.18+1.21.2 |
| Java | 21 (OpenJDK) |
| OS | Windows 10 |

## Build

| Step | Result |
|------|--------|
| `./gradlew compileJava` | ✅ PASS (3m 2s, zero errors) |
| `./gradlew test` | ✅ PASS (all unit tests) |
| `./gradlew build` | ✅ PASS (16s) |
| JAR size | 27.9 MB |
| Sources JAR | 1.8 MB |

## Unit Tests

All existing unit tests passed without modification:
- Config tests (ChatCanvasConfig, ChatBackgroundConfig, ChatTextConfig, etc.)
- Command tests (ClipboardCommandParser, CommandClipboardStorage, CommandTextSanitizer, etc.)
- Emoji tests (EmojiRegistry, EmojiRecentManager, EmojiRecentStorage)
- History tests (ChatLogWriter, ChatLogContext, StoredChatMessage)
- Input tests (ChatCanvasInputController, MentionInsertionController)
- Layout tests (ChatBackgroundMetrics, ChatHudTransform, ChatTextLayout, etc.)
- Message tests (ChatCanvasMessageManager, ChatChannelHistory, DefaultMessageClassifier)
- Mention tests (MentionMatcher, MentionMessageIdRegistry)
- Text tests (SpacedAdvanceMath, UnicodeTextNavigator, TextIndexing)
- Voice tests (AudioLevelMeter, Pcm16MonoResampler, MicrophoneManagerLeaseConcurrency, etc.)
- Editor tests (ColorPickerState, EditorHistory, LayoutEditorMath, etc.)

## Mixin Verification

All 15 Mixin targets verified against MC 1.21.3 decompiled source (yarn mappings):
- **Zero Java source changes required**
- All method descriptors match 1.21.1 signatures
- `Keyboard.onKey(long, int, int, int, int)` confirmed identical
- `ChatHud.addMessage(Text, MessageSignatureData, MessageIndicator)` confirmed identical
- `ChatScreen` all injection targets confirmed identical
- See `docs/porting/1.21.3/mixin-audit.md` for full audit

## Runtime Tests

**Status: Requires user verification (no graphical environment)**

The following tests require a Minecraft client with display:

- [ ] Game reaches main menu
- [ ] Single-player world loads
- [ ] Press T → ChatScreen opens
- [ ] Press / → command mode
- [ ] Player chat input works
- [ ] Command input works
- [ ] Dual channel split layout
- [ ] Current player right-aligned
- [ ] Other players left-aligned
- [ ] Auto word wrap
- [ ] Double-click player name for @mention
- [ ] Mention notification
- [ ] Emoji picker panel
- [ ] Unicode cursor/grapheme cluster
- [ ] Mouse hold-to-talk voice input
- [ ] Keyboard hold-to-talk voice input
- [ ] Chinese voice recognition (no garbled text)
- [ ] Local chat log saving (JSONL)
- [ ] Config persistence across sessions
- [ ] No InvalidInjectionException in logs
- [ ] No NoSuchMethodError in logs
- [ ] No MixinApplyError in logs

## Compat Mods

Status: Not verified in this environment.

| Mod | 1.21.3 Status |
|-----|--------------|
| Chat Heads | Not verified |
| More Chat History | Not verified |
| ChatAnimation | Not verified |
| Smooth Scrolling | Not verified |

## Final JAR

| Property | Value |
|----------|-------|
| Filename | chat-canvas-1.2.0.jar |
| SHA-256 | a2998cb087ddfdd6bce2545956d8929236b0e8b05851afb9baa99bce0698629c |
| Contains fabric.mod.json | ✅ |
| Contains chat_canvas.client.mixins.json | ✅ |
| Contains vosk-0.3.45.jar (jar-in-jar) | ✅ |
| Contains language files (en_us, zh_cn) | ✅ |
| No test classes | ✅ |
| No local config | ✅ |
| No MC decompiled source | ✅ |

## Known Limitations

1. Runtime testing requires a graphical Minecraft client
2. Compat mod testing requires compatible mod versions installed
3. Voice testing requires Vosk model download
