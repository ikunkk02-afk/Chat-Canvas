# API Diff: Chat Canvas 1.21.1 → 1.21.3

## Summary

Minecraft 1.21.3 is a minor protocol patch over 1.21.2. Both share the same Fabric blog post (2024/10/14).
**Zero Java source changes were required** — all Mixin target class method signatures are identical
between 1.21.1 and 1.21.3.

## Baseline

| Property | 1.21.1 (Baseline) | 1.21.3 (Target) |
|----------|-------------------|-----------------|
| Minecraft | 1.21.1 | 1.21.3 |
| Fabric Loader | 0.19.3 | 0.19.3 |
| Fabric API | 0.116.14+1.21.1 | 0.106.1+1.21.3 |
| Yarn mappings | 1.21.1+build.3 | 1.21.3+build.2 |
| Fabric Loom (remap) | 1.17-SNAPSHOT | 1.17-SNAPSHOT (resolved 1.17.17) |
| owo-lib | 0.12.15.4+1.21 | 0.12.18+1.21.2 |
| Java | 21 | 21 |

## Mixin Target Classes Verified

All 15 Mixin target classes verified via MC MCP decompilation of 1.21.3 (yarn):

| Class | Status | Notes |
|-------|--------|-------|
| ChatScreen | ✅ Identical | init, keyPressed, render, removed, resize, mouseClicked, mouseScrolled, insertText, setInitialFocus all unchanged |
| ChatHud | ✅ Identical | render, addMessage, refresh, clear, getTextStyleAt, getIndicatorX, mouseClicked, addVisibleMessage, toChatLineX, toChatLineY, getMessageLineIndex |
| Keyboard | ✅ Identical | onKey(long, int, int, int, int) — same descriptor (JIIII)V |
| ClientPlayNetworkHandler | ✅ Identical | onPlayerList(PlayerListS2CPacket) |
| ChatInputSuggestor | ✅ Identical | Same constructor and method signatures |
| TextFieldWidget | ✅ Identical | Same class structure |
| Screen | ✅ Identical | addSelectableChild present |
| TextRenderer | ✅ Identical | No changes |
| DrawContext | ✅ Identical | fill, drawTextWithShadow, enableScissor all unchanged |

## Key Inheritance Chain (unchanged)

- `keyReleased` is declared in `net.minecraft.client.gui.Element` (default interface method)
- `ChatScreen` does NOT declare `keyReleased` — inherits from `Screen` → `Element`
- Mixin `@Inject` on `ChatScreen.keyReleased` would still fail — `Keyboard.onKey` approach remains correct

## ChatHud Method Signatures (unchanged)

- `addMessage(Text message)` — public
- `addMessage(Text, MessageSignatureData, MessageIndicator)` — public, same signature
- `addMessage(ChatHudLine)` — private
- `render(DrawContext, int, int, int, boolean)` — unchanged
- `refresh()` — unchanged
- `clear(boolean)` — unchanged

## Dependency Resolution

- Fabric API 0.106.1+1.21.3: Confirmed on Modrinth
- owo-lib 0.12.18+1.21.2: Confirmed on WispForest Maven. No specific 1.21.3 build exists, but 1.21.3 is binary-compatible with 1.21.2.
- Mod Menu: Optional dependency only (modCompileOnly), version 11.0.4 kept

## Changes Required

Only build configuration files needed updating:
- `gradle.properties`: minecraft_version, yarn_mappings, fabric_api_version, owo_version
- `fabric.mod.json`: minecraft version range, dependency version ranges

Zero Java source file changes.
