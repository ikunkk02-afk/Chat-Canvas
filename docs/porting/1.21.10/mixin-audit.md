# Mixin Audit: 1.21.10

The target has 14 client mixins. They were compiled, remapped, and checked by
starting the 1.21.10 development client.

## Result

| Check | Result |
|-------|--------|
| Java/client compilation | PASS |
| Mixin remap during `build` | PASS |
| Client startup | PASS — reached a generated integrated world |
| `InvalidInjectionException` / `MixinApplyError` | None after the final fix |
| Unsupported `TextRendererAccessor` | Removed; target has no matching font-storage API |

## Targeted Mixins

`AbstractParentElementMixin`, `ChatHudAccessor`, `ChatHudMixin`,
`ChatInputSuggestorAccessor`, `ChatInputSuggestorMixin`, `ChatScreenAccessor`,
`ChatScreenMixin`, `ClientPlayNetworkHandlerMixin`, `KeyboardMixin`,
`ScreenAccessor`, `SuggestionWindowAccessor`, `TextFieldWidgetAccessor`,
`TextFieldWidgetMixin`, and `TextRendererDrawerMixin` are listed in
`chat_canvas.client.mixins.json`.

The important 1.21.10 adaptations are:

- `Keyboard.onKey(long, int, KeyInput)` and ChatScreen input callbacks use the
  target records.
- `ParentElement` callbacks use `Click`/`CharInput` descriptors.
- Chat HUD and overlay borders use the existing target-compatible
  `ChatBackgroundDraw` helper.
- Text-field rendering uses target `RenderPipelines.GUI` and
  `DrawContext.drawSelection`.
- Emoji font probing no longer depends on the unavailable source
  `TextRenderer.getFontStorage()` accessor.

The launch log also contains unrelated optional-class and Mojang service
network warnings (`xyz.nucleoid...`, Quilt hooks, session/Realms); none are
Chat Canvas Mixin failures.
