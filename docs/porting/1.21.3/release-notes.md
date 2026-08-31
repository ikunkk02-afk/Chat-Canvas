# Chat Canvas 1.3.0 for Minecraft 1.21.3

## Summary

Port of Chat Canvas v1.3.0 to Minecraft 1.21.3 (Fabric), using the 1.21.1 / 1.3.0
release as the functional reference.

All 1.3.0 source and resource changes are included. The Minecraft 1.21.1 → 1.21.3
Mixin audit found identical target signatures, so no version-specific Java API
adaptation was required.

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
- JAR: chat-canvas-1.3.0.jar (28,217,728 bytes)
- SHA-256: `56aad5ff319ae0362e318e86a6df1af34554780a254bd2b69572b45156b37746`

### Mixins
All 15 Mixin targets verified against MC 1.21.3 decompiled source (yarn):
- ChatScreenMixin, ChatHudMixin, KeyboardMixin, ClientPlayNetworkHandlerMixin
- ChatInputSuggestorMixin, TextFieldWidgetMixin, TextRendererDrawerMixin
- AbstractParentElementMixin + 7 accessors

## Installation

1. Install Fabric Loader 0.19.3+ for Minecraft 1.21.3
2. Install Fabric API 0.106.1+
3. Install owo-lib 0.12.18+
4. Place chat-canvas-1.3.0.jar in mods folder

## Files

| File | SHA-256 |
|------|---------|
| chat-canvas-1.3.0.jar | 56aad5ff319ae0362e318e86a6df1af34554780a254bd2b69572b45156b37746 |

## Known Limitations

- Runtime testing pending (requires graphical Minecraft client)
- Live UI, server interaction, microphone, model download, and Android/iOS hardware testing remain environment-dependent; see `test-report.md`.
