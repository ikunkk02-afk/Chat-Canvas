# Release Checklist

## Metadata

- [x] Version is correct (1.1.0)
- [x] `fabric.mod.json` is valid
- [x] License (MIT) is included
- [x] Icon (128×128 PNG) is included
- [x] Dependencies are correct
- [x] `environment: client` is set
- [x] Authors field is set

## Content

- [x] README.md rewritten (full Chinese)
- [x] README_EN.md rewritten (full English)
- [x] CHANGELOG.md updated for 1.1.0
- [x] RELEASE_NOTES_1.1.0.md created (bilingual)
- [x] `.gitignore` covers build outputs and user data
- [x] No debug code or test-only flags enabled
- [x] Icons present and correctly referenced
- [x] Command clipboard security warning included in README and release notes

## Build

- [x] `gradlew clean build` succeeds
- [x] No compilation errors
- [x] No missing translations
- [x] Fabric Loom remaps correctly
- [x] JAR contains `fabric.mod.json`, mixin config, icon and language files
- [x] JAR does not contain user config, logs, or IDE cache

## Testing

- [x] Modern theme displays correctly
- [x] Minecraft-style theme displays correctly
- [x] Theme switching preserves session
- [x] Command clipboard launcher hides when panel opens
- [x] Large modpack environment tested

## Release

- [x] GitHub Release created
