# Dual-UI Editor Implementation Plan

> **For Hermes:** Implement this plan task-by-task. Keep each task focused and build incrementally.

**Goal:** Add a selectable "Minecraft-style" vanilla editor UI alongside the existing modern Chat Canvas editor, sharing the same editing session and config data.

**Architecture:** A shared `EditorScreenState` holds the `EditorSession`, `EditorHistory`, and scroll state. `EditorScreenFactory` dispatches to either `ChatCanvasEditorScreen` (modern) or `VanillaChatCanvasEditorScreen` (vanilla). Both editors share core layout/rendering logic extracted into controller classes. A `EditorUiStyle` config field persists the user's preference.

**Tech Stack:** Java 21, Fabric Loom, Yarn 1.21.1, owo-lib, Minecraft native widgets

---

## Task 1: Create EditorUiStyle enum and config field

**Objective:** Add the `EditorUiStyle` enum and `editorUiStyle` field to the config system.

**Files:**
- Create: `src/main/java/io/github/ikunkk02/chatcanvas/editor/EditorUiStyle.java`
- Modify: `src/main/java/io/github/ikunkk02/chatcanvas/config/ChatCanvasConfig.java`

**Step 1: Create EditorUiStyle.java**

```java
package io.github.ikunkk02.chatcanvas.editor;

public enum EditorUiStyle {
    CHAT_CANVAS,
    VANILLA
}
```

**Step 2: Read ChatCanvasConfig to understand the config structure (it's likely a record)**

**Step 3: Add editorUiStyle field with default CHAT_CANVAS**

**Step 4: Add lang keys to zh_cn.json and en_us.json:**
- `chat_canvas.ui_style`: "界面风格" / "UI Style"
- `chat_canvas.ui_style.chat_canvas`: "Chat Canvas 界面" / "Chat Canvas UI"
- `chat_canvas.ui_style.vanilla`: "Minecraft 原版界面" / "Minecraft-style UI"
- `chat_canvas.ui_style.choose`: "选择界面风格" / "Choose UI Style"
- `chat_canvas.ui_style.chat_canvas_desc`: "现代化浮动面板与自定义控件" / "Modern floating panels and custom controls"
- `chat_canvas.ui_style.vanilla_desc`: "Minecraft 原版按钮、滑块和列表" / "Minecraft-style buttons, sliders and lists"
- `chat_canvas.ui_style.switch_note`: "切换界面不会保存当前修改" / "Switching the UI does not save your current changes"
- `chat_canvas.ui_style.current`: "当前界面" / "Current UI"

**Step 5: Commit**

```bash
git add .
git commit -m "feat: add EditorUiStyle enum and config field"
```

---

## Task 2: Create EditorScreenFactory

**Objective:** Centralize all editor screen creation through a factory that reads EditorUiStyle.

**Files:**
- Create: `src/main/java/io/github/ikunkk02/chatcanvas/editor/EditorScreenFactory.java`
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/ChatCanvasClient.java` (keybind entry)
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/integration/ModMenuIntegration.java` (ModMenu entry)

**Step 1: Create EditorScreenFactory.java**

```java
package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

public final class EditorScreenFactory {
    private EditorScreenFactory() {}

    public static Screen create(@Nullable Screen parent) {
        EditorUiStyle style = ChatCanvasConfig.instance().settings().editorUiStyle();
        return switch (style) {
            case CHAT_CANVAS -> new ChatCanvasEditorScreen(parent);
            case VANILLA -> new VanillaChatCanvasEditorScreen(parent);
        };
    }
}
```

Note: `VanillaChatCanvasEditorScreen` doesn't exist yet — this is scaffolding. The VANILLA case will compile once Task 3 creates the class.

**Step 2: Update ChatCanvasClient.java — replace `new ChatCanvasEditorScreen(client.currentScreen)` with `EditorScreenFactory.create(client.currentScreen)`**

**Step 3: Update ModMenuIntegration.java — replace direct screen creation with factory call**

**Step 4: Commit**

```bash
git add .
git commit -m "feat: add EditorScreenFactory for UI style dispatch"
```

---

## Task 3: Create VanillaChatCanvasEditorScreen skeleton

**Objective:** Create the vanilla editor screen class with empty body, basic layout only.

**Files:**
- Create: `src/client/java/io/github/ikunkk02/chatcanvas/editor/VanillaChatCanvasEditorScreen.java`

**Step 1: Create VanillaChatCanvasEditorScreen.java extending Screen (NOT BaseOwoScreen)**

```java
package io.github.ikunkk02.chatcanvas.editor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class VanillaChatCanvasEditorScreen extends Screen {
    private final @Nullable Screen parent;

    public VanillaChatCanvasEditorScreen(@Nullable Screen parent) {
        super(Text.translatable("chat_canvas.editor.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        // Will add vanilla widgets in later tasks
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
```

**Step 2: Verify build compiles**

```bash
./gradlew build --no-daemon --offline
```

**Step 3: Commit**

```bash
git add .
git commit -m "feat: create VanillaChatCanvasEditorScreen skeleton"
```

---

## Task 4: Extract shared editor state into EditorScreenState

**Objective:** Create a record that holds the EditorSession, EditorHistory, and category state so screens can be swapped without data loss.

**Files:**
- Create: `src/main/java/io/github/ikunkk02/chatcanvas/editor/EditorScreenState.java`
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/ChatCanvasEditorScreen.java` (add export/import)
- Modify: `src/main/java/io/github/ikunkk02/chatcanvas/editor/EditorSession.java` (if needed)

**Step 1: Create EditorScreenState.java**

```java
package io.github.ikunkk02.chatcanvas.editor;

import java.util.HashMap;
import java.util.Map;

public record EditorScreenState(
        EditorSession session,
        EditorHistory history,
        int activeCategoryOrdinal,
        Map<Integer, Double> scrollPositions
) {
    public EditorScreenState(EditorSession session, EditorHistory history, int activeCategoryOrdinal) {
        this(session, history, activeCategoryOrdinal, new HashMap<>());
    }
}
```

**Step 2: Add exportState() method to ChatCanvasEditorScreen**

The method captures the current session and history so they can be passed to a new screen.

**Step 3: Add a constructor to ChatCanvasEditorScreen that accepts EditorScreenState**

New constructor: `ChatCanvasEditorScreen(@Nullable Screen parent, EditorScreenState state)`

**Step 4: Build and verify**

**Step 5: Commit**

---

## Task 5: Add UI style toggle button to ChatCanvasEditorScreen

**Objective:** Add a "UI Style" button to the modern editor's toolbar.

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/ChatCanvasEditorScreen.java`

**Step 1: Add a UI style button to `buildToolbar()` between the title and undo button**

Use `ModernUiTheme.button()` with text "界面：Chat Canvas" / "UI: Chat Canvas".

**Step 2: On click, open a style selection screen**

Create a small inline selection (or use `ModernUiTheme` styled buttons).

**Step 3: On selection of "Minecraft-style UI", export EditorScreenState, save the new preference, then switch to VanillaChatCanvasEditorScreen**

**Step 4: Ensure the screen close doesn't trigger cancel (use a flag `switchingUiStyle`)**

**Step 5: Commit**

---

## Task 6: Implement vanilla category navigation

**Objective:** Build the category tab bar using vanilla ButtonWidget.

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/VanillaChatCanvasEditorScreen.java`

**Step 1: Define category buttons as a field list**

**Step 2: In `init()`, create 6 ButtonWidget instances for the 6 categories**

Position in a 2-row grid: [Layout] [Text] [Background] / [Player Colors] [Mentions] [Command Input]

**Step 3: On click, update the visible content area**

**Step 4: Add selected-state styling (darker/disabled appearance for active category)**

**Step 5: Build and verify category switching works**

**Step 6: Commit**

---

## Task 7: Implement vanilla layout page

**Objective:** Build the Layout settings page using vanilla widgets.

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/VanillaChatCanvasEditorScreen.java`

**Step 1: Create methods to build layout page content:**

- Chat preview state (open/closed): two ButtonWidget toggle buttons
- X position: SliderWidget (0 to screenWidth)
- Y position: SliderWidget (0 to screenHeight)
- Width: SliderWidget (100 to screenWidth)
- Height: SliderWidget (20 to screenHeight)
- Restore defaults button

**Step 2: Wire sliders to EditorSession. On change, call `geometryChanged.run()`**

**Step 3: Build and verify**

**Step 4: Commit**

---

## Task 8: Implement vanilla text page

**Objective:** Build the Text settings page.

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/VanillaChatCanvasEditorScreen.java`

**Step 1: Create content for text page:**

- Font scale: SliderWidget (0.5 to 2.0)
- Line spacing: SliderWidget (0 to 20)
- Character spacing: SliderWidget (0 to 8)
- Text opacity: SliderWidget (0 to 100)
- Alignment: 3 ButtonWidget toggles (Left, Center, Right)
- Shadow: ButtonWidget toggle (On/Off)
- Restore defaults

**Step 2: Wire to session.text()**

**Step 3: Commit**

---

## Task 9: Implement vanilla background page

**Objective:** Build the Background settings page.

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/VanillaChatCanvasEditorScreen.java`

**Step 1: Message background section:**

- Display mode: 3 ButtonWidget toggles (Follow Text / Full Width / Hidden)
- Color: opens VanillaColorPickerScreen (Task 11)
- Opacity: SliderWidget (0-100)
- Horizontal padding: SliderWidget
- Vertical padding: SliderWidget

**Step 2: Input background section:**

- Input color: opens VanillaColorPickerScreen
- Input opacity: SliderWidget
- Input border toggle: ButtonWidget
- Border color: opens VanillaColorPickerScreen
- Border opacity: SliderWidget

**Step 3: Restore defaults button**

**Step 4: Commit**

---

## Task 10: Implement vanilla player colors page

**Objective:** Build the Player Colors settings page with player list.

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/VanillaChatCanvasEditorScreen.java`

**Step 1: Enabled toggle**

**Step 2: Mode selector (Automatic / Vanilla) — two ButtonWidget toggles**

**Step 3: Color palette — display 24 color swatches, click to open color picker**

**Step 4: Player list — use a simple scrollable list of player names with color indicators and edit/reset buttons**

**Step 5: Restore defaults**

**Step 6: Commit**

---

## Task 11: Implement vanilla mention page

**Objective:** Build the Mentions settings page.

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/VanillaChatCanvasEditorScreen.java`

**Step 1: Double-click enable + interval slider**

**Step 2: Highlight enable + color picker + bold toggle + require @ toggle**

**Step 3: Sound enable + type cycling button + volume/pitch sliders + test button**

**Step 4: Toast enable + when-open toggle + length slider**

**Step 5: Flash enable + color picker + opacity/duration sliders**

**Step 6: Other: ignore own mentions, quick actions**

**Step 7: Private message template TextFieldWidget**

**Step 8: Restore defaults**

**Step 9: Commit**

---

## Task 12: Implement vanilla command input page

**Objective:** Build the Command Input settings page.

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/VanillaChatCanvasEditorScreen.java`

**Step 1: Enable toggle, show button toggle**

**Step 2: Insert mode toggle (Replace / Insert at Cursor)**

**Step 3: Allow duplicates toggle, sensitive warning toggle**

**Step 4: Max commands slider**

**Step 5: Manage button — opens existing CommandClipboardPanel**

**Step 6: Restore defaults**

**Step 7: Commit**

---

## Task 13: Create VanillaColorPickerScreen

**Objective:** Create a vanilla-style color picker using RGB sliders + hex input.

**Files:**
- Create: `src/client/java/io/github/ikunkk02/chatcanvas/editor/VanillaColorPickerScreen.java`

**Step 1: Create Screen with R/G/B SliderWidget + hex TextFieldWidget**

**Step 2: Color preview rectangle**

**Step 3: Realtime update of preview as sliders change**

**Step 4: "Done" saves and returns, "Cancel" reverts**

**Step 5: Wire into vanilla editor for all color buttons**

**Step 6: Build and verify**

**Step 7: Commit**

---

## Task 14: Add UI style button to vanilla editor

**Objective:** Add the style toggle button to the vanilla editor so users can switch back.

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/VanillaChatCanvasEditorScreen.java`

**Step 1: Add a ButtonWidget in the header area: "界面风格：Minecraft 原版" / "UI Style: Minecraft-style"**

**Step 2: On click, show a simple selection screen or directly switch**

**Step 3: On switch to Chat Canvas, export state, save preference, create ChatCanvasEditorScreen with state**

**Step 4: Build and verify round-trip switching**

**Step 5: Commit**

---

## Task 15: Add editor close reason handling

**Objective:** Prevent cancel/save on style switch, and prevent state loss.

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/ChatCanvasEditorScreen.java`
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/VanillaChatCanvasEditorScreen.java`

**Step 1: Add `boolean switchingUiStyle` flag to both editors**

**Step 2: In `close()`, check the flag — if switching, don't cancel the session**

**Step 3: In style switch code, set the flag before calling `client.setScreen()`, clear it after**

**Step 4: Verify: switch UI back and forth, undo still works, no auto-save/cancel**

**Step 5: Commit**

---

## Task 16: Extract shared chat preview controller

**Objective:** Extract preview state management from owo-ui PreviewChatWidget into a controller class.

**Files:**
- Create: `src/main/java/io/github/ikunkk02/chatcanvas/editor/preview/ChatPreviewController.java`
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/ui/PreviewChatWidget.java` (delegate to controller)

**Step 1: Create ChatPreviewController — holds PixelLayout, PreviewChatState, drag/resize logic, snap logic**

**Step 2: Move core logic from PreviewChatWidget into the controller (not rendering, just state+interaction)**

**Step 3: PreviewChatWidget uses the controller internally**

**Step 4: Vanilla editor creates a ChatPreviewController for its own preview rendering**

**Step 5: Commit**

---

## Task 17: Draw vanilla chat preview

**Objective:** Render the chat preview inside VanillaChatCanvasEditorScreen using the shared controller.

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/VanillaChatCanvasEditorScreen.java`

**Step 1: In `render()`, use ChatPreviewController + ChatRenderEngine to draw the preview**

**Step 2: Handle mouse drag for moving/resizing the preview chat box**

**Step 3: Show X, Y, Width, Height overlay text**

**Step 4: Show alignment guides**

**Step 5: Commit**

---

## Task 18: Add vanilla footer (Save/Cancel)

**Objective:** Add Save, Cancel, Undo, Redo buttons at the bottom of the vanilla editor.

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/VanillaChatCanvasEditorScreen.java`

**Step 1: Add 4 ButtonWidget instances for Save, Cancel, Undo, Redo**

**Step 2: Wire Save → session.save → close**

**Step 3: Wire Cancel → session.cancel (restore original) → close**

**Step 4: Wire Undo/Redo → EditorHistory**

**Step 5: Commit**

---

## Task 19: Handle window resize and responsive layout

**Objective:** Adapt vanilla editor layout for different screen sizes.

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/chatcanvas/editor/VanillaChatCanvasEditorScreen.java`

**Step 1: Override `resize()` to reposition widgets**

**Step 2: For narrow screens (< 800px wide), stack vertically (preview above, settings below)**

**Step 3: For wider screens, use left/right split**

**Step 4: Build and test at 1280×720, 1600×900, 1920×1080, GUI Scales 1-4**

**Step 5: Commit**

---

## Task 20: Add lang keys and localisation

**Objective:** Add all new translation keys to both language files.

**Files:**
- Modify: `src/main/resources/assets/chat_canvas/lang/zh_cn.json`
- Modify: `src/main/resources/assets/chat_canvas/lang/en_us.json`

**Step 1: Add all ui_style keys (see Task 1 for list)**

**Step 2: Verify no hardcoded Chinese strings in Java code**

**Step 3: Verify keys are consistent between zh_cn and en_us**

**Step 4: Commit**

---

## Task 21: Update documentation

**Objective:** Add dual-UI feature documentation to README and CHANGELOG.

**Files:**
- Modify: `README.md`
- Modify: `README_EN.md`
- Modify: `CHANGELOG.md`

**Step 1: Add "两种编辑器界面" / "Two Editor Interfaces" section to both READMEs**

**Step 2: Add CHANGELOG entries for the new feature**

**Step 3: Commit**

---

## Task 22: Final build and cleanup

**Objective:** Clean build, verify JAR contents, final commit.

**Step 1: Run clean build**

```bash
./gradlew clean build --no-daemon
```

**Step 2: Verify all 26 existing tests still pass**

**Step 3: Verify JAR contains all new classes**

**Step 4: Final commit**

```bash
git add .
git commit -m "feat: add selectable modern and vanilla editor UIs"
git push
```

---

## Summary

| Task | Files | Description |
|------|-------|-------------|
| 1 | 3 files | EditorUiStyle enum + config |
| 2 | 3 files | EditorScreenFactory |
| 3 | 1 file | VanillaChatCanvasEditorScreen skeleton |
| 4 | 3 files | EditorScreenState record |
| 5 | 1 file | UI toggle on modern editor |
| 6 | 1 file | Vanilla category navigation |
| 7 | 1 file | Vanilla layout page |
| 8 | 1 file | Vanilla text page |
| 9 | 1 file | Vanilla background page |
| 10 | 1 file | Vanilla player colors page |
| 11 | 1 file | Vanilla mention page |
| 12 | 1 file | Vanilla command page |
| 13 | 1 file | VanillaColorPickerScreen |
| 14 | 1 file | UI toggle on vanilla editor |
| 15 | 2 files | Close reason handling |
| 16 | 2 files | Shared preview controller |
| 17 | 1 file | Vanilla preview rendering |
| 18 | 1 file | Vanilla footer |
| 19 | 1 file | Responsive layout |
| 20 | 2 files | Localisation |
| 21 | 3 files | Documentation |
| 22 | — | Build + commit |

**Estimated: 9–11 files created, 8–10 files modified.**
