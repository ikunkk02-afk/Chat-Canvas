# Test Report: Chat Canvas 1.2.0 for Minecraft 1.21.4

## Environment

| Item | Value |
|------|-------|
| Minecraft | 1.21.4 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.119.4+1.21.4 |
| Yarn mappings | 1.21.4+build.8 |
| owo-lib | 0.12.20+1.21.4 |
| Java | JDK 21 (Microsoft build 21.0.8.9-hotspot) |
| OS | Windows 10 |
| Gradle | 9.5.1 |
| Loom | 1.17.17 |

## Test Results

### Unit Tests

```
./gradlew test
BUILD SUCCESSFUL
```

All 50+ unit tests passed:
- Config reading/writing ✅
- Damaged config fallback ✅
- Sensitive command detection/masking ✅
- Emoji Unicode handling ✅
- Emoji recent storage ✅
- Vosk JSON Chinese parsing ✅
- Microphone lease close (concurrent + duplicate) ✅
- Local chat log JSONL format ✅
- UTF-8 handling ✅
- Message deduplication ✅
- Command tool storage ✅
- Clipboard command parsing ✅
- Player identity resolution ✅
- Mention insertion/matching ✅
- Chat layout metrics ✅
- Editor math ✅
- Spaced text advance math ✅
- Unicode text navigation ✅

### Build

```
./gradlew build
BUILD SUCCESSFUL
```

Output: `chat-canvas-1.2.0.jar` (28MB, includes bundled vosk-0.3.45)

### runClient

```
./gradlew runClient
```

- [x] Game reaches main menu
- [x] All 56 mods load successfully
- [x] Chat Canvas initializes: `[chat_canvas] Initializing Chat Canvas`
- [x] All 15 Mixins inject without errors
- [x] No InvalidInjectionException
- [x] No MixinApplyError
- [x] No NoSuchMethodError
- [x] No NoSuchFieldError
- [x] No AbstractMethodError
- [x] No ClassCastException
- [x] Singleplayer world created successfully
- [x] Player joined world
- [x] World saved

### Singleplayer Test

- [x] Enter main menu
- [x] Enter singleplayer world
- [x] Press T opens chat

### JAR Verification

- [x] `fabric.mod.json` present
- [x] `chat_canvas.client.mixins.json` present
- [x] Bundled vosk-0.3.45.jar present
- [x] No test classes in JAR
- [x] No decompiled Minecraft source
- [x] No Vosk model data
- [x] No local config/log files

## Pending Verification

The following require manual interactive testing:

- [ ] Chat screen opens with independent player/command fields
- [ ] `/` prefix auto-switches to command mode
- [ ] Player chat channel works
- [ ] Command system channel works
- [ ] Classic layout works
- [ ] Split alignment layout works
- [ ] Right-aligned own messages, left-aligned others
- [ ] Auto word wrap
- [ ] Double-click name @mention
- [ ] @mention notification (once only)
- [ ] Emoji panel, search, categories, recent
- [ ] Unicode grapheme cluster cursor/delete
- [ ] Mouse hold-to-talk voice input
- [ ] Keyboard shortcut voice input
- [ ] Chinese voice recognition (Vosk)
- [ ] Chinese encoding correctly handled
- [ ] Microphone concurrent release no errors
- [ ] Voice result not auto-sent
- [ ] Local chat log JSONL writing
- [ ] Resource cleanup on world exit
- [ ] Re-enter world, all features still work

## Compatibility Mods

| Mod | 1.21.4 Version | Status |
|-----|---------------|--------|
| Chat Heads | Unknown | Not verified |
| More Chat History | Unknown | Not verified |
| ChatAnimation | Unknown | Not verified |
| Smooth Scrolling | Unknown | Not verified |

Note: Compatibility mods were not tested as their 1.21.4 versions were not available in the development environment.

## Known Limitations

- Mojang session server TLS handshake fails in this network environment (infrastructure issue, not mod-related)
- Voice input requires Vosk model download on first use
- Compatibility mods listed above require separate verification with actual mod JARs
