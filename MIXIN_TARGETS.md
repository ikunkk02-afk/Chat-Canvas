# Mixin Targets

Chat Canvas retains all 15 Fabric 1.21.1 client Mixins. They are mapped to the
Minecraft 1.21.1 Mojang names used by NeoForge and registered in
`src/main/resources/chatcanvas.mixins.json`.

| Mixin | NeoForge / Mojmap target | Purpose |
| --- | --- | --- |
| `ChatScreenMixin` | `ChatScreen` | Independent chat/command fields, responsive placement, Emoji, voice and tools |
| `ChatScreenAccessor` | `ChatScreen` | Access chat-screen state used by integrations |
| `ChatHudMixin` | `ChatComponent` | Dual-channel custom chat rendering and interaction |
| `ChatHudAccessor` | `ChatComponent` | Access vanilla chat state without replacing it |
| `ClientPlayNetworkHandlerMixin` | `ClientPacketListener` | Preserve message component metadata and ingress classification |
| `KeyboardMixin` | `KeyboardHandler` | V-key press/repeat/release edge handling |
| `ChatInputSuggestorMixin` | `CommandSuggestions` | Bound and style suggestions for both inputs |
| `ChatInputSuggestorAccessor` | `CommandSuggestions` | Access suggestion state |
| `SuggestionWindowAccessor` | `CommandSuggestions.SuggestionsList` | Access the active suggestion window |
| `TextFieldWidgetMixin` | `EditBox` | Grapheme-safe Unicode editing and spaced text rendering |
| `TextFieldWidgetAccessor` | `EditBox` | Preserve cursor and selection state |
| `TextRendererDrawerMixin` | `StringRenderOutput` | Character-spacing render hook |
| `TextRendererAccessor` | `Font` | Access font sets for Emoji support checks |
| `AbstractParentElementMixin` | `ContainerEventHandler` | Route panel, Unicode and pointer input |
| `ScreenAccessor` | `Screen` | Add the independent selectable chat field |

Loader lifecycle, key registration, reload listening, GUI overlay rendering and
chat send/receive hooks use NeoForge events. The Mixins above remain where the
Fabric implementation depends on Minecraft GUI internals and no equivalent
NeoForge event provides the required behavior.
