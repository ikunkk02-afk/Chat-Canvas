# Test Report: Chat Canvas 1.2.0 for Minecraft 1.21.8 (Fabric)

## Environment

| Item | Value |
|------|-------|
| Minecraft | 1.21.8 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.136.1+1.21.8 |
| Yarn mappings | 1.21.8+build.1 |
| Fabric Loom | 1.17.17 |
| owo-lib | 0.12.23+1.21.8 |
| ModMenu | 15.0.2 |
| Java | 21 |
| Gradle | 9.5.1 |
| OS | Windows 10 |

## Unit Tests

- **Result**: ✅ PASSED
- **Test files**: 54 Java test files
- **Gradle task**: `./gradlew test` → BUILD SUCCESSFUL

## Build

- **Result**: ✅ PASSED
- **Output**: `build/libs/chat-canvas-1.2.0.jar` (27.9 MB)
- **Sources jar**: `build/libs/chat-canvas-1.2.0-sources.jar` (1.8 MB)
- **Compilation**: 273 Java source files, 0 errors

## JAR Contents

| Expected | Status |
|----------|--------|
| `fabric.mod.json` | ✅ Present |
| `chat_canvas.client.mixins.json` | ✅ Present |
| `META-INF/jars/vosk-0.3.45.jar` | ✅ Bundled |
| Mixin classes (15) | ✅ All present |
| Test classes | ✅ Excluded |
| Decompiled MC source | ✅ Not included |
| Vosk Chinese model | ✅ Not in JAR |

## runClient

- **Status**: ⚠️ Not executed (requires graphical environment)
- **Note**: `./gradlew build` succeeds with all tests passing. Mixin targets verified via MCP decompiler.

## Compatibility Mods

| Mod | 1.21.8 Status |
|-----|--------------|
| Chat Heads | ⚠️ Not verified (no 1.21.8 build available at time of port) |
| More Chat History | ⚠️ Not verified |
| ChatAnimation | ⚠️ Not verified |
| Smooth Scrolling | ⚠️ Not verified |

## SHA-256

```
13ecdf724206508f641622b03c7786147c153194c7c118afbaf0aded02d179db  build/libs/chat-canvas-1.2.0.jar
```

## Known Limitations

1. **runClient not executed**: The build environment lacks a graphical display for running Minecraft.
2. **Compatibility mods untested**: Target mods do not have verified 1.21.8 versions. Compat layers retained but untested.
3. **ChatHud.render @WrapOperation disabled**: The lambdas introduced in 1.21.6+ prevent @WrapOperation on fill/drawTextWithShadow inside render. The @Inject HEAD (cancels vanilla render) correctly delegates to DualChatHudRenderer.

## Mixin Audit

- **Total Mixins**: 15 (8 injectors + 7 accessors)
- **Changes needed**: 1 (TextFieldWidgetMixin — drawSelectionHighlight → context.drawSelection)
- **All targets verified via MCP**: ✅
