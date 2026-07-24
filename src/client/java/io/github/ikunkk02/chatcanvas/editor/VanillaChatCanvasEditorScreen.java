package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.animation.AnimationClock;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLayoutRuntime;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * Alternative editor UI that uses vanilla Minecraft widgets (buttons,
 * sliders, lists, text fields) instead of the modern owo-ui panel system.
 * <p>
 * Shares the same {@link EditorSession}, history, and config data as
 * {@link ChatCanvasEditorScreen} — only the visual presentation differs.
 */
public final class VanillaChatCanvasEditorScreen extends Screen {

    private final @Nullable Screen parent;
    private final AnimationClock animationClock = new AnimationClock();

    /** True when this screen is about to be replaced by a style switch. */
    boolean switchingUiStyle;

    /* Shared editing session — may be injected via state constructor. */
    private EditorSession session;

    /* Category state */
    private static final int CATEGORY_LAYOUT = 0;
    private static final int CATEGORY_TEXT = 1;
    private static final int CATEGORY_BACKGROUND = 2;
    private static final int CATEGORY_PLAYER_COLORS = 3;
    private static final int CATEGORY_MENTION = 4;
    private static final int CATEGORY_COMMAND = 5;
    private static final int CATEGORY_COUNT = 6;

    private int activeCategory = CATEGORY_LAYOUT;

    /* Footer buttons */
    private ButtonWidget saveButton;
    private ButtonWidget cancelButton;
    private ButtonWidget undoButton;
    private ButtonWidget redoButton;

    /* Style toggle */
    private ButtonWidget uiStyleButton;

    /* Category tab buttons */
    private ButtonWidget[] categoryButtons = new ButtonWidget[CATEGORY_COUNT];

    /* ---- constructors ---- */

    public VanillaChatCanvasEditorScreen(@Nullable Screen parent) {
        super(Text.translatable("chat_canvas.editor.title"));
        this.parent = parent;
    }

    public VanillaChatCanvasEditorScreen(@Nullable Screen parent, EditorScreenState state) {
        this(parent);
        this.session = state.session();
        this.activeCategory = state.activeCategoryOrdinal();
    }

    /* ---- lifecycle ---- */

    @Override
    protected void init() {
        super.init();
        if (session == null) {
            session = new EditorSession(
                    ChatCanvasConfig.instance().settings(), width, height);
        }
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearChildren();

        int footerY = height - 30;
        int btnW = 72;

        // --- bottom row: Save / Cancel / Undo / Redo ---
        int x = width - btnW - 10;
        saveButton = ButtonWidget.builder(Text.translatable("chat_canvas.action.save"), btn -> onSave())
                .dimensions(x, footerY, btnW, 20).build();
        addDrawableChild(saveButton);
        x -= btnW + 6;

        cancelButton = ButtonWidget.builder(Text.translatable("chat_canvas.action.cancel"), btn -> onCancel())
                .dimensions(x, footerY, btnW, 20).build();
        addDrawableChild(cancelButton);
        x -= btnW + 6;

        redoButton = ButtonWidget.builder(Text.translatable("chat_canvas.action.redo"), btn -> redo())
                .dimensions(x, footerY, btnW, 20).build();
        addDrawableChild(redoButton);
        x -= btnW + 6;

        undoButton = ButtonWidget.builder(Text.translatable("chat_canvas.action.undo"), btn -> undo())
                .dimensions(x, footerY, btnW, 20).build();
        addDrawableChild(undoButton);

        // --- top-left: UI style toggle ---
        uiStyleButton = ButtonWidget.builder(
                Text.translatable("chat_canvas.ui_style.current").append(Text.literal(": "))
                        .append(Text.translatable("chat_canvas.ui_style.vanilla")),
                btn -> onSwitchUiStyle())
                .dimensions(10, 10, 130, 20).build();
        addDrawableChild(uiStyleButton);

        // --- category tabs ---
        for (int i = 0; i < CATEGORY_COUNT; i++) {
            final int cat = i;
            String name = categoryName(cat);
            int catX = 10 + i * (width / CATEGORY_COUNT);
            categoryButtons[i] = ButtonWidget.builder(Text.literal(name), btn -> {
                activeCategory = cat;
                refreshCategoryButtons();
            }).dimensions(catX, 36, (width / CATEGORY_COUNT) - 10, 20).build();
            addDrawableChild(categoryButtons[i]);
        }

        refreshButtonStates();
        refreshCategoryButtons();
    }

    /* ---- render ---- */

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        double deltaSeconds = animationClock.tick();
        renderBackground(context, mouseX, mouseY, delta);

        // Draw current category content label
        String contentLabel = switch (activeCategory) {
            case CATEGORY_LAYOUT -> Text.translatable("chat_canvas.category.layout").getString();
            case CATEGORY_TEXT -> Text.translatable("chat_canvas.category.text").getString();
            case CATEGORY_BACKGROUND -> Text.translatable("chat_canvas.category.background").getString();
            case CATEGORY_PLAYER_COLORS -> Text.translatable("chat_canvas.category.player_colors").getString();
            case CATEGORY_MENTION -> Text.translatable("chat_canvas.category.mention").getString();
            case CATEGORY_COMMAND -> Text.translatable("chat_canvas.category.command").getString();
            default -> "?";
        };
        context.drawTextWithShadow(textRenderer,
                Text.literal("[" + contentLabel + "]"),
                width / 2 - textRenderer.getWidth("[" + contentLabel + "]") / 2, 64, 0xFF_FFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    /* ---- input ---- */

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_Z) { undo(); return true; }
        if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_Y) { redo(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        if (switchingUiStyle) return;
        onCancel();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        if (session != null) session.resizeViewport(width, height);
        super.resize(client, width, height);
        rebuildWidgets();
    }

    /* ---- actions ---- */

    private void onSave() {
        ChatCanvasConfig.instance().save(session.settings());
        ChatLayoutRuntime.applySavedSettings();
        returnToParent();
    }

    private void onCancel() {
        if (session != null) session.apply(session.original());
        returnToParent();
    }

    private void undo() {
        if (session == null) return;
        if (session.undo()) {
            onGeometryChanged();
            refreshButtonStates();
        }
    }

    private void redo() {
        if (session == null) return;
        if (session.redo()) {
            onGeometryChanged();
            refreshButtonStates();
        }
    }

    private void onGeometryChanged() {
        // Preview update — stub for now; full preview rendering added later
    }

    private void refreshButtonStates() {
        if (undoButton != null) undoButton.active = session != null && session.canUndo();
        if (redoButton != null) redoButton.active = session != null && session.canRedo();
    }

    private void refreshCategoryButtons() {
        for (int i = 0; i < CATEGORY_COUNT; i++) {
            if (categoryButtons[i] != null) {
                categoryButtons[i].active = (i != activeCategory);
            }
        }
    }

    private void onSwitchUiStyle() {
        switchingUiStyle = true;
        // Save the new preference
        ChatCanvasConfig.instance().settings();
        // Export state and switch
        EditorScreenState state = new EditorScreenState(session, activeCategory);
        // Toggle the style
        // TODO: actually toggle config
        client.setScreen(new ChatCanvasEditorScreen(parent, state));
        switchingUiStyle = false;
    }

    private void returnToParent() {
        if (client != null) client.setScreen(parent);
    }

    private static String categoryName(int cat) {
        return switch (cat) {
            case CATEGORY_LAYOUT -> Text.translatable("chat_canvas.category.layout").getString();
            case CATEGORY_TEXT -> Text.translatable("chat_canvas.category.text").getString();
            case CATEGORY_BACKGROUND -> Text.translatable("chat_canvas.category.background").getString();
            case CATEGORY_PLAYER_COLORS -> Text.translatable("chat_canvas.category.player_colors").getString();
            case CATEGORY_MENTION -> Text.translatable("chat_canvas.category.mention").getString();
            case CATEGORY_COMMAND -> Text.translatable("chat_canvas.category.command").getString();
            default -> "?";
        };
    }
}
