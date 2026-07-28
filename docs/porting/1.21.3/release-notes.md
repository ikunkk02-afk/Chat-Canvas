# Chat Canvas 1.2.0 for Minecraft 1.21.3

## Summary

Port of Chat Canvas v1.2.0 to Minecraft 1.21.3 (Fabric).

**Zero Java source changes required** — all 15 Mixin targets have identical method signatures between 1.21.1 and 1.21.3.

## Changes

### Dependencies
- Minecraft: 1.21.3
- Fabric Loader: 0.19.3
- Fabric API: 0.106.1+1.21.3
- owo-lib: 0.12.18+1.21.2
- Yarn mappings: 1.21.3+build.2

### Build
- `./gradlew build` ✅ PASS (all unit tests)
- Zero compilation errors
- JAR: chat-canvas-1.2.0.jar (27.9 MB)
- SHA-256: `a2998cb087ddfdd6bce2545956d8929236b0e8b05851afb9baa99bce0698629c`

### Mixins
All 15 Mixin targets verified against MC 1.21.3 decompiled source (yarn):
- ChatScreenMixin, ChatHudMixin, KeyboardMixin, ClientPlayNetworkHandlerMixin
- ChatInputSuggestorMixin, TextFieldWidgetMixin, TextRendererDrawerMixin
- AbstractParentElementMixin + 7 accessors

## Installation

1. Install Fabric Loader 0.19.3+ for Minecraft 1.21.3
2. Install Fabric API 0.106.1+
3. Install owo-lib 0.12.18+
4. Place chat-canvas-1.2.0.jar in mods folder

## Files

| File | SHA-256 |
|------|---------|
| chat-canvas-1.2.0.jar | a2998cb087ddfdd6bce2545956d8929236b0e8b05851afb9baa99bce0698629c |

## Known Limitations

- Runtime testing pending (requires graphical Minecraft client)
- Compat mods (Chat Heads, More Chat History, ChatAnimation, Smooth Scrolling) not verified for 1.21.3
