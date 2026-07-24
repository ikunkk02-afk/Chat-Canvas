# Release Checklist

## Metadata

- [x] Version is correct (1.0.0)
- [x] `fabric.mod.json` is valid
- [x] License (MIT) is included
- [x] Icon (128×128 PNG) is included
- [x] Dependencies are correct (Fabric Loader ≥ 0.19.3, Fabric API ≥ 0.116.14, owo-lib ≥ 0.12.15.4)
- [x] Mod Menu listed as optional (`suggests`, not `depends`)
- [x] Environment set to `client`

## Documentation

- [x] `README.md` (Chinese) is complete
- [x] `README_EN.md` (English) is complete
- [x] `CHANGELOG.md` is created
- [x] Social/author links are correct
- [x] Known limitations are documented
- [x] Security warning for command clipboard is included
- [ ] Issue templates created (optional)

## Code & Debug

- [x] Debug flags default to `false` (DEBUG_CLIP, DEBUG_BOUNDARIES)
- [x] No per-frame logging in render/mouse loops
- [x] No `System.out` or `printStackTrace` cruft
- [x] Obsolete placeholder UI removed
- [x] No hardcoded Chinese strings in Java code (all via `TranslatableText`)

## Build

- [x] `./gradlew clean build` succeeds
- [x] All 26 unit tests pass
- [x] Release JAR is `build/libs/chat-canvas-1.0.0.jar`
- [x] JAR contains `fabric.mod.json`, mixin config, language files, icon
- [x] No local config, logs, or development cache bundled in JAR
- [x] Build not committed to the repository

## Git

- [x] Working tree is clean
- [x] `.gitignore` covers `build/`, `run/`, `logs/`, `.gradle/`, IDE dirs
- [x] No sensitive data in repository

## Functional Tests (manual — verify before publishing)

- [ ] Editor opens with default keybind **K**
- [ ] Editor title is inside the toolbar
- [ ] All six category pages switch correctly
- [ ] No "Not implemented" placeholders
- [ ] Pages do not leak outside the viewport
- [ ] Page clicks, scrolls, and drags work
- [ ] Settings panel side avoidance works
- [ ] Save, Cancel, Undo, Redo work
- [ ] Chat HUD position and size update in real chat after save
- [ ] Text size, line spacing, character spacing, opacity, shadow, alignment all work
- [ ] Background colors, modes, padding, input field styling work
- [ ] Player colors apply and manual overrides work
- [ ] Mention insertion, highlight, sound, toast, flash work
- [ ] Quick-action menu works (mention, private message, copy name)
- [ ] Command clipboard: save, search, categorize, favorite, edit, delete, reorder work
- [ ] Sensitive command warning appears
- [ ] Commands persist after world exit and game restart
- [ ] GUI Scales 1–4, window sizes 1280×720 – 1920×1080
- [ ] Chat Heads, More Chat History, ChatAnimation, Smooth Scrolling compatible
- [ ] Mod works without optional compatibility mods

## Publishing (not performed yet — author's decision)

- [ ] Create Git tag `v1.0.0`
- [ ] Create GitHub Release
- [ ] Upload `chat-canvas-1.0.0.jar` to GitHub Release
- [ ] Publish to Modrinth (optional)
- [ ] Publish to CurseForge (optional)
