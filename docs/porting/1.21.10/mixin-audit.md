# Mixin Audit: 1.21.10

All 15 mixins from mc/1.21.9 baseline. Verified against 1.21.10 MCP-decompiled sources.

## Summary: ALL MIXINS PASS

The input refactoring (KeyInput/Click/CharInput) occurred in 1.21.9, so the mc/1.21.9 codebase was already compatible with 1.21.10. No mixin changes were needed.

## Detailed Audit

### ChatScreenMixin
| Item | Status |
|------|--------|
| Target class | `ChatScreen` ✓ |
| `init` injection | `@At("RETURN")` ✓ |
| `setInitialFocus` injection | `@At("RETURN")` ✓ |
| `resize` injection | `@At("HEAD")` — `(MinecraftClient, int, int)` ✓ |
| `render` injection (HEAD) | `@At("HEAD")` — `(DrawContext, int, int, float, CallbackInfo)` ✓ |
| `render` injection (RETURN) | `@At("RETURN")` ✓ |
| `mouseClicked` injection | `@At("HEAD")` — `(Click, boolean, CallbackInfoReturnable)` ✓ |
| `mouseScrolled` injection | `@At("HEAD")` — `(double, double, double, double, CallbackInfoReturnable)` ✓ |
| `removed` injection | `@At("HEAD")` ✓ |
| `keyPressed` injection | `@At("HEAD")` — `(KeyInput, CallbackInfoReturnable)` ✓ |
| `insertText` injection | `@At("HEAD")` — `(String, boolean, CallbackInfo)` ✓ |
| `@WrapOperation fill` | `fill(IIIII)V` ordinal=0 in render — VERIFIED ✓ |
| `ChatInputSuggestor` calls | Uses `new KeyInput(...)` wrapper ✓ |
| `KeyBinding.matchesKey` | Takes `KeyInput` ✓ |
| Matrix API | Uses `pushMatrix()`/`popMatrix()` ✓ |

### KeyboardMixin
| Item | Status |
|------|--------|
| Target class | `Keyboard` ✓ |
| Target method | `onKey(long window, int action, KeyInput input)` ✓ |
| Injection point | `@At("TAIL")` ✓ |
| GLFW_RELEASE check | `action != GLFW.GLFW_RELEASE` ✓ |
| Voice shortcut release | `input.key()`, `input.scancode()` ✓ |

### ChatHudMixin
| Item | Status |
|------|--------|
| Target class | `ChatHud` ✓ |
| `render` HEAD injection | `(DrawContext, int, int, boolean, CallbackInfo)` — uses pushMatrix/popMatrix ✓ |
| `render` RETURN injection | ✓ |
| `@ModifyReturnValue getWidth/getHeight/getLineHeight` | ✓ |
| `@WrapOperation fill` ordinal 0,1 | Verified targets exist ✓ |
| `@ModifyArg addVisibleMessage` | `breakRenderedChatMessageLines` — VERIFIED ✓ |
| `@WrapOperation drawTextWithShadow` | Return type: `int`→`void` — Uses `SpacedTextRenderer.draw()` wrapper ✓ |
| `@Inject addMessage` | `(Text, MessageSignatureData, MessageIndicator)` ✓ |
| `@ModifyVariable toChatLineX/Y, mouseClicked` | ✓ |
| Matrix/Vector API | Uses `context.getMatrices()`, `matrix.transformPosition(new Vector2f(...))` ✓ |

### ClientPlayNetworkHandlerMixin
| Item | Status |
|------|--------|
| Target class | `ClientPlayNetworkHandler` ✓ |
| `onPlayerList` injection | `@At("RETURN")` — `(PlayerListS2CPacket)` ✓ |
| `onPlayerRemove` injection | `@At("RETURN")` — `(PlayerRemoveS2CPacket)` ✓ |

### ChatInputSuggestorMixin
| Item | Status |
|------|--------|
| Target class | `ChatInputSuggestor` ✓ |
| `@ModifyExpressionValue show/renderMessages` | Field `Screen.height:I` — VERIFIED ✓ |
| `showCommandSuggestions` | `@At("RETURN")` ✓ |
| `provideRenderText` | `@ModifyReturnValue` ✓ |

### TextFieldWidgetMixin
| Item | Status |
|------|--------|
| Target class | `TextFieldWidget` ✓ |
| `onClick` injection | `(Click, boolean)` ✓ |
| `keyPressed` injection | `(KeyInput)` ✓ |
| `write` injection | `(String)` ✓ |
| `setText` @ModifyVariable | ✓ |
| `updateFirstCharacterIndex` | ✓ |
| `getCharacterX` | ✓ |
| `renderWidget` | Uses `RenderPipelines.GUI` ✓, uses `context.drawSelection(...)` ✓ |

### AbstractParentElementMixin
| Item | Status |
|------|--------|
| Target class | `ParentElement` (interface) ✓ |
| `mouseDragged` | `(Click, double deltaX, double deltaY)` ✓ |
| `mouseReleased` | `(Click)` ✓ |
| `charTyped` | `(CharInput)` ✓ |

### TextRendererDrawerMixin
| Item | Status |
|------|--------|
| Target class | `TextRenderer$Drawer` (inner class) ✓ |
| Target method | `accept(int, Style, int)` — VERIFIED ✓ |

### Accessors (ChatHudAccessor, ChatScreenAccessor, ChatInputSuggestorAccessor, ScreenAccessor, SuggestionWindowAccessor, TextFieldWidgetAccessor, TextRendererAccessor)
| Item | Status |
|------|--------|
| All targets | VERIFIED in 1.21.10 source ✓ |

## Verification Method

All targets verified via `mcp__minecraft_dev__get_minecraft_source` against 1.21.10 with yarn mappings.
