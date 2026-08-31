# Test Report: Chat Canvas 1.2.0 for Minecraft 1.21.7

## Environment

| Item | Value |
|------|-------|
| Date | 2026-07-29 |
| Minecraft | 1.21.7 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.129.0+1.21.7 |
| owo-lib | 0.12.21+1.21.6 |
| Yarn mappings | 1.21.7+build.8 |
| Java | 21 |
| OS | Windows 10 |

## Build Results

| Step | Status | Notes |
|------|--------|-------|
| `./gradlew compileJava` | PENDING | Waiting for compilation |
| `./gradlew test` | PENDING | |
| `./gradlew build` | PENDING | |
| `./gradlew runClient` | PENDING | |

## Mixin Runtime Verification

| Mixin | Runtime Status | Notes |
|-------|---------------|-------|
| ChatScreenMixin | PENDING | All injections compatible |
| KeyboardMixin | PENDING | onKey signature unchanged |
| ChatHudMixin | PENDING | 4 @WrapOperation removed |
| ChatInputSuggestorMixin | PENDING | |
| TextFieldWidgetMixin | PENDING | RenderPipelines import fixed |
| TextRendererDrawerMixin | PENDING | |
| ClientPlayNetworkHandlerMixin | PENDING | |
| AbstractParentElementMixin | PENDING | |

## Functional Testing

| Test | Status | Notes |
|------|--------|-------|
| Enter main menu | PENDING | |
| Enter singleplayer world | PENDING | |
| Press T for player chat | PENDING | |
| Press / for command mode | PENDING | |
| Auto-switch to command mode | PENDING | |
| Player chat channel works | PENDING | |
| Command system channel works | PENDING | |
| Classic layout | PENDING | |
| Split left-right layout | PENDING | |
| Self messages right-aligned | PENDING | |
| Other messages left-aligned | PENDING | |
| Word wrapping | PENDING | |
| Double-click player name @mention | PENDING | |
| Mention notification (once) | PENDING | |
| Emoji panel | PENDING | |
| Unicode cursor/navigation | PENDING | |
| Mouse voice input | PENDING | |
| Keyboard voice input (V) | PENDING | |
| Chinese recognition | PENDING | |
| Mic concurrent release | PENDING | |
| Local chat log saving | PENDING | |
| Resource cleanup on world exit | PENDING | |
| Re-enter world (functionality intact) | PENDING | |

## Compatibility Mod Testing

| Mod | MC 1.21.7 Available | Test Result |
|-----|---------------------|-------------|
| Chat Heads | UNKNOWN | PENDING |
| More Chat History | UNKNOWN | PENDING |
| ChatAnimation | UNKNOWN | PENDING |
| Smooth Scrolling | UNKNOWN | PENDING |

## Known Limitations

1. Removed 4 @WrapOperation annotations on ChatHud.render due to lambda refactor — vanilla fallback rendering no longer applies custom backgrounds/text styling
2. DualChatHudRenderer still provides full custom rendering when Chat Canvas is active
3. owo-lib 0.12.21+1.21.6 is used (no 1.21.7-specific build exists)
4. No runtime testing performed yet
