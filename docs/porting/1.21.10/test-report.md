# Test Report: Chat Canvas 1.2.0 for Minecraft 1.21.10

## Test Environment

| Item | Value |
|------|-------|
| Minecraft | 1.21.10 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.138.4+1.21.10 |
| Yarn Mappings | 1.21.10+build.3 |
| Fabric Loom | 1.17.17 |
| owo-lib | 0.12.24+1.21.9 |
| ModMenu | 16.0.1 |
| Java | 22 (dev) / 21 (target) |
| Vosk | 0.3.45 (jar-in-jar, JNA excluded) |
| OS | Windows 10 |
| Mixin | 0.8.7 (SpongePowered) |
| MixinExtras | 0.5.4 |

## Unit Tests

| Status | Details |
|--------|---------|
| ✅ PASS | All 48 unit tests passed |
| ✅ PASS | `./gradlew test` — BUILD SUCCESSFUL |

## Build

| Status | Details |
|--------|---------|
| ✅ PASS | `./gradlew compileJava` — BUILD SUCCESSFUL |
| ✅ PASS | `./gradlew compileClientJava` — BUILD SUCCESSFUL |
| ✅ PASS | `./gradlew build` — BUILD SUCCESSFUL |
| ✅ PASS | JAR produced: `chat-canvas-1.2.0.jar` (~28MB) |
| ✅ PASS | `fabric.mod.json` included in JAR |
| ✅ PASS | Mixin configs included in JAR |
| ✅ PASS | vosk-0.3.45.jar bundled (jar-in-jar) |
| ✅ PASS | No test classes in JAR |
| ✅ PASS | No decompiled MC source in JAR |

## runClient

| Status | Details |
|--------|---------|
| ✅ PASS | Game reached main menu |
| ✅ PASS | Chat Canvas initialized |
| ✅ PASS | No `InvalidInjectionException` |
| ✅ PASS | No `MixinApplyError` |
| ✅ PASS | No `NoSuchMethodError` |
| ✅ PASS | No `NoSuchFieldError` |
| ✅ PASS | No `AbstractMethodError` |
| ✅ PASS | No `ClassCastException` |
| ✅ PASS | Resource reload succeeded |
| ✅ PASS | Sound engine started |
| ✅ PASS | All texture atlases created |

## Verified Features

| Feature | Status |
|---------|--------|
| Compilation | ✅ No errors |
| Unit tests | ✅ All pass |
| Game launch | ✅ Main menu reached |
| Mixin injection | ✅ All 15 mixins load cleanly |
| Resources | ✅ `chat_canvas` resources loaded |

## Unverified (requires full game session)

| Feature | Reason |
|---------|--------|
| Player chat / command split | Requires world join (dev environment tested to main menu only) |
| Emoji panel | Requires world join |
| Voice input | Requires microphone + Vosk model |
| Chat log saving | Requires world join |
| Editor screen (K key) | Requires world join |
| Compatibility mods | Chat Heads, More Chat History, etc. — not tested |
| Final JAR in clean instance | Pending |
| Multiplayer | Pending |

## Known Limitations

1. owo-lib 0.12.24+1.21.9 is used (no 1.21.10-specific build available; binary compatible)
2. Java 22 was used for development; target Java 21
3. Full gameplay testing requires a world join session

## Mixin Verification

All 15 Mixin classes verified against 1.21.10 Yarn-decompiled sources via MCP. See `docs/porting/1.21.10/mixin-audit.md` for detailed results.
