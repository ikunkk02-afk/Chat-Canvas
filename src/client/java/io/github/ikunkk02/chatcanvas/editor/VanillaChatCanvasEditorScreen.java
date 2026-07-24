package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.animation.AnimationClock;
import io.github.ikunkk02.chatcanvas.chat.command.ui.CommandClipboardPanel;
import io.github.ikunkk02.chatcanvas.chat.identity.*;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLayoutRuntime;
import io.github.ikunkk02.chatcanvas.chat.notification.MentionNotificationController;
import io.github.ikunkk02.chatcanvas.chat.render.PreviewChatState;
import io.github.ikunkk02.chatcanvas.config.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public final class VanillaChatCanvasEditorScreen extends Screen {

    private final @Nullable Screen parent;
    private final AnimationClock animationClock = new AnimationClock();
    boolean switchingUiStyle;
    private EditorSession session;

    private static final int CAT_LAYOUT = 0, CAT_TEXT = 1, CAT_BACKGROUND = 2,
            CAT_PLAYER_COLORS = 3, CAT_MENTION = 4, CAT_COMMAND = 5;
    private static final int CAT_COUNT = 6;
    private int activeCategory = CAT_LAYOUT;

    private ButtonWidget saveBtn, cancelBtn, undoBtn, redoBtn, uiStyleBtn;
    private ButtonWidget[] categoryBtns = new ButtonWidget[CAT_COUNT];
    private final List<ClickableWidget> catWidgets = new ArrayList<>();

    private PreviewChatState previewState = PreviewChatState.OPEN;

    public VanillaChatCanvasEditorScreen(@Nullable Screen parent) {
        super(Text.translatable("chat_canvas.editor.title"));
        this.parent = parent;
    }
    public VanillaChatCanvasEditorScreen(@Nullable Screen parent, EditorScreenState state) {
        this(parent);
        this.session = state.session();
        this.activeCategory = state.activeCategoryOrdinal();
    }

    @Override protected void init() {
        super.init();
        if (session == null)
            session = new EditorSession(ChatCanvasConfig.instance().settings(), width, height);
        buildPersistent();
        buildContent();
    }

    /* ── persistent widgets ─────────────────────────────────── */

    private void buildPersistent() {
        int fy = height - 30, x = width - 10;
        saveBtn   = btn(Text.translatable("chat_canvas.action.save"),   x -= 76, fy, 72, this::onSave);
        cancelBtn = btn(Text.translatable("chat_canvas.action.cancel"), x -= 78, fy, 72, this::onCancel);
        redoBtn   = btn(Text.translatable("chat_canvas.action.redo"),   x -= 78, fy, 72, this::redo);
        undoBtn   = btn(Text.translatable("chat_canvas.action.undo"),   x -= 78, fy, 72, this::undo);

        uiStyleBtn = btn(Text.translatable("chat_canvas.ui_style.current")
                .append(Text.literal(": "))
                .append(Text.translatable("chat_canvas.ui_style.vanilla")),
                10, 10, 130, this::onSwitchUiStyle);

        for (int i = 0; i < CAT_COUNT; i++) {
            final int cat = i;
            int cw = Math.max(50, (width - 20) / CAT_COUNT - 4);
            categoryBtns[i] = btn(Text.literal(catName(cat)), 10 + i * (cw + 4), 36, cw, () -> {
                activeCategory = cat; buildContent(); refreshCatBtns();
            });
        }
        refreshCatBtns(); refreshFooterBtns();
    }

    /* ── category content ────────────────────────────────────── */

    private void buildContent() {
        removeCatWidgets();
        switch (activeCategory) {
            case CAT_LAYOUT        -> buildLayout();
            case CAT_TEXT          -> buildText();
            case CAT_BACKGROUND    -> buildBackground();
            case CAT_PLAYER_COLORS -> buildPlayerColors();
            case CAT_MENTION       -> buildMention();
            case CAT_COMMAND       -> buildCommand();
        }
    }
    private void removeCatWidgets() { for (var w : catWidgets) remove(w); catWidgets.clear(); }

    // ── layout ─────────────────────────────────────────────────
    private void buildLayout() {
        int y = 64, c1 = 10;
        PixelLayout cfg = session.layout();
        int sw = width, sh = height;

        catBtn(Text.translatable("chat_canvas.preview.state"), c1, y, 110, () -> {
            previewState = (previewState == PreviewChatState.OPEN)
                    ? PreviewChatState.CLOSED : PreviewChatState.OPEN;
            buildContent();
        }); y += 26;

        int[] vals = {cfg.x(), cfg.y(), cfg.width(), cfg.height()};
        String[] keys = {"chat_canvas.option.x","chat_canvas.option.y",
                         "chat_canvas.option.width","chat_canvas.option.height"};
        int[] maxs = {sw, sh, sw, sh};

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            catSlider(Text.translatable(keys[i]), c1, y, 150, vals[i], 0, maxs[i], v -> {
                PixelLayout c = session.layout();
                int[] px = {c.x(), c.y(), c.width(), c.height()};
                px[idx] = v;
                session.setLayout(new PixelLayout(px[0], px[1], px[2], px[3]));
                session.commit(); onGeom();
            });
            y += 26;
        }
        catBtn(Text.translatable("chat_canvas.action.restore_defaults"), c1, y += 4, 150, () -> {
            session.restoreLayoutDefaults(); buildContent();
        });
    }

    // ── text ───────────────────────────────────────────────────
    private void buildText() {
        int y = 64, c1 = 10;
        ChatTextConfig cfg = session.text();
        catSlider(Text.literal(T("chat_canvas.option.font_scale")+" "+(int)(cfg.fontScale()*100)+"%"), c1, y, 200,
                (int)(cfg.fontScale()*100), 50, 200,
                v -> textApply(new ChatTextConfig(v/100.0, cfg.lineSpacing(), cfg.textOpacity(), cfg.alignment(), cfg.shadow(), cfg.characterSpacing())));
        catSlider(Text.literal(T("chat_canvas.option.line_spacing")+" "+(int)cfg.lineSpacing()), c1, y+=26, 200,
                (int)cfg.lineSpacing(), 0, 20,
                v -> textApply(new ChatTextConfig(cfg.fontScale(), (double)v, cfg.textOpacity(), cfg.alignment(), cfg.shadow(), cfg.characterSpacing())));
        catSlider(Text.literal(T("chat_canvas.option.text_opacity")+" "+(int)(cfg.textOpacity()*100)+"%"), c1, y+=26, 200,
                (int)(cfg.textOpacity()*100), 0, 100,
                v -> textApply(new ChatTextConfig(cfg.fontScale(), cfg.lineSpacing(), v/100.0, cfg.alignment(), cfg.shadow(), cfg.characterSpacing())));
        catSlider(Text.literal(T("chat_canvas.option.character_spacing")+" "+String.format("%.1f",cfg.characterSpacing())), c1, y+=26, 200,
                (int)(cfg.characterSpacing()*10), 0, 80,
                v -> textApply(new ChatTextConfig(cfg.fontScale(), cfg.lineSpacing(), cfg.textOpacity(), cfg.alignment(), cfg.shadow(), v/10.0)));

        ChatTextAlignment[] aligns = ChatTextAlignment.values();
        for (int i = 0; i < aligns.length; i++) {
            final ChatTextAlignment a = aligns[i];
            catBtn(Text.translatable(switch(a){case LEFT->"chat_canvas.alignment.left";case CENTER->"chat_canvas.alignment.center";case RIGHT->"chat_canvas.alignment.right";}),
                    c1+i*76, y+=26, 72, () -> textApply(new ChatTextConfig(cfg.fontScale(), cfg.lineSpacing(), cfg.textOpacity(), a, cfg.shadow(), cfg.characterSpacing())));
        }
        catBtn(cfg.shadow() ? onOff(true,"chat_canvas.option.text_shadow") : onOff(false,"chat_canvas.option.text_shadow"),
                c1, y+=26, 200, () -> {
                    ChatTextConfig c = session.text();
                    textApply(new ChatTextConfig(c.fontScale(), c.lineSpacing(), c.textOpacity(), c.alignment(), !c.shadow(), c.characterSpacing()));
                    buildContent();
                });
        catBtn(Text.translatable("chat_canvas.action.restore_text_defaults"), c1, y+=30, 200, () -> {
            session.restoreTextDefaults(); buildContent();
        });
    }
    private void textApply(ChatTextConfig c) { session.setText(c); session.commit(); onGeom(); }

    // ── background ─────────────────────────────────────────────
    private void buildBackground() {
        int y = 64, c1 = 10;
        ChatBackgroundConfig cfg = session.background();
        MessageBackgroundMode[] modes = MessageBackgroundMode.values();
        for (int i = 0; i < modes.length; i++) {
            final MessageBackgroundMode m = modes[i];
            catBtn(Text.translatable(switch(m){case FOLLOW_TEXT->"chat_canvas.background.mode.follow_text";case FULL_WIDTH->"chat_canvas.background.mode.full_width";case HIDDEN->"chat_canvas.background.mode.hidden";}),
                    c1+i*86, y, 82, () -> { session.setBackground(cfg.withMessageMode(m)); session.commit(); buildContent(); });
        }
        y += 26;
        catColorBtn(c1, y, cfg.messageColor(), c -> { session.setBackground(cfg.withMessageColor(c)); session.commit(); buildContent(); });
        catSlider(Text.literal(T("chat_canvas.background.opacity")+" "+(int)(cfg.messageOpacity()*100)+"%"), c1+110, y, 140,
                (int)(cfg.messageOpacity()*100), 0, 100,
                v -> bgApply(new ChatBackgroundConfig(cfg.messageMode(), cfg.messageColor(), v/100.0, cfg.horizontalPadding(), cfg.verticalPadding(), cfg.inputColor(), cfg.inputOpacity(), cfg.inputBorderEnabled(), cfg.inputBorderColor(), cfg.inputBorderOpacity())));
        y += 26;
        catSlider(Text.literal(T("chat_canvas.background.horizontal_padding")+" "+cfg.horizontalPadding()), c1, y, 200,
                cfg.horizontalPadding(), 0, 50,
                v -> bgApply(new ChatBackgroundConfig(cfg.messageMode(), cfg.messageColor(), cfg.messageOpacity(), v, cfg.verticalPadding(), cfg.inputColor(), cfg.inputOpacity(), cfg.inputBorderEnabled(), cfg.inputBorderColor(), cfg.inputBorderOpacity())));
        catSlider(Text.literal(T("chat_canvas.background.vertical_padding")+" "+cfg.verticalPadding()), c1, y+=26, 200,
                cfg.verticalPadding(), 0, 50,
                v -> bgApply(new ChatBackgroundConfig(cfg.messageMode(), cfg.messageColor(), cfg.messageOpacity(), cfg.horizontalPadding(), v, cfg.inputColor(), cfg.inputOpacity(), cfg.inputBorderEnabled(), cfg.inputBorderColor(), cfg.inputBorderOpacity())));
        y += 30;
        catColorBtn(c1, y, cfg.inputColor(), c -> { session.setBackground(cfg.withInputColor(c)); session.commit(); buildContent(); });
        catSlider(Text.literal(T("chat_canvas.background.input_opacity")+" "+(int)(cfg.inputOpacity()*100)+"%"), c1+110, y, 140,
                (int)(cfg.inputOpacity()*100), 0, 100,
                v -> bgApply(new ChatBackgroundConfig(cfg.messageMode(), cfg.messageColor(), cfg.messageOpacity(), cfg.horizontalPadding(), cfg.verticalPadding(), cfg.inputColor(), v/100.0, cfg.inputBorderEnabled(), cfg.inputBorderColor(), cfg.inputBorderOpacity())));
        y += 26;
        catBtn(cfg.inputBorderEnabled() ? onOff(true,"chat_canvas.background.input_border") : onOff(false,"chat_canvas.background.input_border"),
                c1, y, 200, () -> { session.setBackground(cfg.withInputBorderEnabled(!cfg.inputBorderEnabled())); session.commit(); buildContent(); });
        catColorBtn(c1, y+=26, cfg.inputBorderColor(), c -> { session.setBackground(cfg.withInputBorderColor(c)); session.commit(); buildContent(); });
        catSlider(Text.literal(T("chat_canvas.background.border_opacity")+" "+(int)(cfg.inputBorderOpacity()*100)+"%"), c1+110, y, 140,
                (int)(cfg.inputBorderOpacity()*100), 0, 100,
                v -> bgApply(new ChatBackgroundConfig(cfg.messageMode(), cfg.messageColor(), cfg.messageOpacity(), cfg.horizontalPadding(), cfg.verticalPadding(), cfg.inputColor(), cfg.inputOpacity(), cfg.inputBorderEnabled(), cfg.inputBorderColor(), v/100.0)));
        catBtn(Text.translatable("chat_canvas.action.restore_background_defaults"), c1, y+=30, 200, () -> {
            session.restoreBackgroundDefaults(); buildContent();
        });
    }
    private void bgApply(ChatBackgroundConfig c) { session.setBackground(c); session.commit(); onGeom(); }

    // ── player colors ──────────────────────────────────────────
    private void buildPlayerColors() {
        int y = 64, c1 = 10;
        PlayerColorConfig cfg = session.playerColors();
        catBtn(cfg.enabled() ? onOff(true,"chat_canvas.player_colors.enabled") : onOff(false,"chat_canvas.player_colors.enabled"),
                c1, y, 200, () -> { session.setPlayerColors(cfg.withEnabled(!cfg.enabled())); session.commit(); buildContent(); });
        y += 26;
        catBtn(Text.literal((cfg.mode()==PlayerColorMode.AUTOMATIC?"● ":"○ ")+T("chat_canvas.player_colors.automatic")), c1, y, 100, () -> {
            if (cfg.mode()!=PlayerColorMode.AUTOMATIC){session.setPlayerColors(cfg.withMode(PlayerColorMode.AUTOMATIC));session.commit();buildContent();}});
        catBtn(Text.literal((cfg.mode()==PlayerColorMode.VANILLA?"● ":"○ ")+T("chat_canvas.player_colors.vanilla")), c1+104, y, 100, () -> {
            if (cfg.mode()!=PlayerColorMode.VANILLA){session.setPlayerColors(cfg.withMode(PlayerColorMode.VANILLA));session.commit();buildContent();}});
        y += 30;
        for (int i = 0; i < Math.min(12, cfg.palette().size()); i++) {
            final int idx = i;
            catColorBtn(c1+(i%6)*44, y+(i/6)*26, cfg.palette().get(idx), c -> {
                session.setPlayerColors(cfg.withPaletteColor(idx, c)); session.commit(); buildContent();
            });
        }
        y += 56;
        List<PlayerChatIdentity> players = PlayerRosterTracker.editorPlayers();
        PlayerNameColorProvider prov = new PlayerNameColorProvider();
        for (int i = 0; i < Math.min(8, players.size()); i++) {
            PlayerChatIdentity p = players.get(i);
            prov.updateConfig(session.playerColors());
            int color = prov.colorFor(p).orElse(0xFFFFFF);
            catColorBtn(c1, y+i*22, color, c -> {
                session.setPlayerColors(session.playerColors().withUuidOverride(p.uuid(), c));
                session.commit(); buildContent();
            });
            // draw name label text
        }
        y += Math.min(8, players.size()) * 22 + 6;
        catBtn(Text.translatable("chat_canvas.player_colors.restore_defaults"), c1, y, 200, () -> {
            session.restorePlayerColorDefaults(); buildContent();
        });
    }

    // ── mention ────────────────────────────────────────────────
    private void buildMention() {
        int y = 64, c1 = 10;
        MentionConfig cfg = session.mention();
        catToggle("chat_canvas.mention.double_click", cfg.doubleClickEnabled(), c1, y,
                v -> { session.setMention(cfg.withDoubleClickEnabled(v)); session.commit(); buildContent(); });
        catSlider(Text.literal(T("chat_canvas.mention.double_click_interval")+" "+cfg.doubleClickIntervalMs()+"ms"), c1, y+=26, 200, cfg.doubleClickIntervalMs(), 100, 1000,
                v -> { session.setMention(cfg.withDoubleClickIntervalMs(v)); session.commit(); });
        catToggle("chat_canvas.mention.highlight", cfg.highlightEnabled(), c1, y+=26,
                v -> { session.setMention(cfg.withHighlightEnabled(v)); session.commit(); buildContent(); });
        catColorBtn(c1, y+=26, cfg.highlightColor(), c -> { session.setMention(cfg.withHighlightColor(c)); session.commit(); buildContent(); });
        catToggle("chat_canvas.mention.highlight_bold", cfg.highlightBold(), c1+110, y,
                v -> { session.setMention(cfg.withHighlightBold(v)); session.commit(); buildContent(); });
        catToggle("chat_canvas.mention.require_at", cfg.requireAtSymbol(), c1, y+=26,
                v -> { session.setMention(cfg.withRequireAtSymbol(v)); session.commit(); });
        y += 30;
        catToggle("chat_canvas.mention.sound_enabled", cfg.soundEnabled(), c1, y,
                v -> { session.setMention(cfg.withSoundEnabled(v)); session.commit(); buildContent(); });
        MentionSound[] sounds = MentionSound.values();
        catBtn(Text.translatable("chat_canvas.mention.sound_type").append(Text.literal("  "))
                .append(Text.translatable("chat_canvas.mention.sound."+cfg.sound().name().toLowerCase(Locale.ROOT))),
                c1, y+=26, 200, () -> {
                    session.setMention(cfg.withSound(sounds[(cfg.sound().ordinal()+1)%sounds.length]));
                    session.commit(); buildContent();
                });
        catSlider(Text.literal("🔊 "+(int)(cfg.soundVolume()*100)+"%"), c1, y+=26, 200, (int)(cfg.soundVolume()*100), 0, 100,
                v -> { session.setMention(cfg.withSoundVolume(v/100.0)); session.commit(); });
        catSlider(Text.literal("🎵 "+String.format("%.1f",cfg.soundPitch())), c1, y+=26, 200, (int)(cfg.soundPitch()*10), 5, 20,
                v -> { session.setMention(cfg.withSoundPitch(v/10.0)); session.commit(); });
        catBtn(Text.translatable("chat_canvas.mention.test_sound"), c1, y+=26, 200,
                () -> MentionNotificationController.instance().testSound(cfg));
        y += 30;
        catToggle("chat_canvas.mention.toast_enabled", cfg.toastEnabled(), c1, y,
                v -> { session.setMention(cfg.withToastEnabled(v)); session.commit(); });
        catToggle("chat_canvas.mention.toast_when_open", cfg.toastWhenChatOpen(), c1, y+=26,
                v -> { session.setMention(cfg.withToastWhenChatOpen(v)); session.commit(); });
        y += 26;
        catToggle("chat_canvas.mention.flash_enabled", cfg.flashEnabled(), c1, y,
                v -> { session.setMention(cfg.withFlashEnabled(v)); session.commit(); buildContent(); });
        catColorBtn(c1, y+=26, cfg.flashColor(), c -> { session.setMention(cfg.withFlashColor(c)); session.commit(); buildContent(); });
        catSlider(Text.literal(T("chat_canvas.mention.flash_opacity")+" "+(int)(cfg.flashOpacity()*100)+"%"), c1+110, y, 140, (int)(cfg.flashOpacity()*100), 0, 100,
                v -> { session.setMention(cfg.withFlashOpacity(v/100.0)); session.commit(); });
        y += 30;
        catToggle("chat_canvas.mention.ignore_own", cfg.ignoreOwnMessages(), c1, y,
                v -> { session.setMention(cfg.withIgnoreOwnMessages(v)); session.commit(); });
        catToggle("chat_canvas.mention.quick_actions", cfg.playerQuickActionsEnabled(), c1, y+=26,
                v -> { session.setMention(cfg.withPlayerQuickActionsEnabled(v)); session.commit(); });
        y += 28;
        TextFieldWidget pmField = new TextFieldWidget(textRenderer, c1, y+14, 200, 20, Text.empty());
        pmField.setText(cfg.privateMessageTemplate());
        pmField.setChangedListener(val -> { session.setMention(session.mention().withPrivateMessageTemplate(val)); session.commit(); });
        catWidget(pmField);
        y += 40;
        catBtn(Text.translatable("chat_canvas.mention.restore_defaults"), c1, y, 200, () -> {
            session.restoreMentionDefaults(); buildContent();
        });
    }

    // ── command ────────────────────────────────────────────────
    private void buildCommand() {
        int y = 64, c1 = 10;
        CommandClipboardConfig cfg = session.commandClipboard();
        catToggle("chat_canvas.command.enabled", cfg.enabled(), c1, y,
                v -> { session.setCommandClipboard(cfg.withEnabled(v)); session.commit(); buildContent(); });
        catToggle("chat_canvas.command.show_button", cfg.showPanelButton(), c1, y+=26,
                v -> { session.setCommandClipboard(cfg.withShowPanelButton(v)); session.commit(); });
        catBtn(Text.translatable("chat_canvas.command.insert_mode").append(Text.literal("  "))
                .append(Text.translatable(cfg.insertMode()==CommandInsertMode.REPLACE_INPUT?"chat_canvas.command.insert_replace":"chat_canvas.command.insert_cursor")),
                c1, y+=26, 200, () -> { session.setCommandClipboard(cfg.withInsertMode(cfg.insertMode().opposite())); session.commit(); buildContent(); });
        catToggle("chat_canvas.command.allow_duplicates", cfg.allowDuplicates(), c1, y+=26,
                v -> { session.setCommandClipboard(cfg.withAllowDuplicates(v)); session.commit(); });
        catToggle("chat_canvas.command.sensitive_warning", cfg.sensitiveWarning(), c1, y+=26,
                v -> { session.setCommandClipboard(cfg.withSensitiveWarning(v)); session.commit(); });
        catSlider(Text.literal(T("chat_canvas.command.max_commands")+" "+cfg.maxCommands()), c1, y+=26, 200, cfg.maxCommands(), 5, 200,
                v -> { session.setCommandClipboard(cfg.withMaxCommands(v)); session.commit(); });
        catBtn(Text.translatable("chat_canvas.command.manage"), c1, y+=30, 200, () -> {
            ChatCanvasConfig.instance().save(session.settings());
            CommandClipboardPanel.requestOpenNextChatScreen();
            if (client != null) client.setScreen(new ChatScreen(""));
        });
        catBtn(Text.translatable("chat_canvas.command.restore_defaults"), c1, y+=26, 200, () -> {
            session.restoreCommandClipboardDefaults(); buildContent();
        });
    }

    /* ── widget helpers ──────────────────────────────────────── */

    private ButtonWidget btn(Text msg, int x, int y, int w, Runnable act) {
        var b = ButtonWidget.builder(msg, btn -> act.run()).dimensions(x, y, w, 20).build();
        addDrawableChild(b); return b;
    }
    private void catBtn(Text msg, int x, int y, int w, Runnable act) {
        catWidget(ButtonWidget.builder(msg, btn -> act.run()).dimensions(x, y, w, 20).build());
    }
    private void catToggle(String key, boolean cur, int x, int y, java.util.function.Consumer<Boolean> setter) {
        catBtn(Text.translatable(key).append(Text.literal("  ")).append(Text.translatable(cur?"chat_canvas.state.on":"chat_canvas.state.off")), x, y, 200, () -> setter.accept(!cur));
    }
    private void catSlider(Text label, int x, int y, int w, int val, int min, int max, java.util.function.IntConsumer onChange) {
        double ratio = (double)(val - min) / Math.max(1, max - min);
        catWidget(new SliderWidget(x, y+14, w, 20, label, ratio) {
            @Override protected void updateMessage() {
                this.setMessage(Text.literal(label.getString()));
            }
            @Override protected void applyValue() {
                onChange.accept((int)(this.value * (max - min) + min));
            }
        });
    }
    private void catColorBtn(int x, int y, int color, java.util.function.IntConsumer onPicked) {
        catBtn(Text.literal("■ "+String.format("#%06X",color&0xFFFFFF)), x, y, 100, () -> {
            if (client != null) client.setScreen(new VanillaColorPickerScreen(this, color, onPicked));
        });
    }
    private void catWidget(ClickableWidget w) { catWidgets.add(w); addDrawableChild(w); }

    /* ── actions ─────────────────────────────────────────────── */

    private void onSave() {
        ChatCanvasConfig.instance().save(session.settings());
        ChatLayoutRuntime.applySavedSettings();
        returnToParent();
    }
    private void onCancel() {
        if (session != null) session.apply(session.original());
        returnToParent();
    }
    private void undo() { if (session!=null&&session.undo()) { onGeom(); refreshFooterBtns(); } }
    private void redo() { if (session!=null&&session.redo()) { onGeom(); refreshFooterBtns(); } }
    private void onGeom() {}
    private void refreshFooterBtns() {
        if (undoBtn!=null) undoBtn.active = session!=null && session.canUndo();
        if (redoBtn!=null) redoBtn.active = session!=null && session.canRedo();
    }
    private void refreshCatBtns() {
        for (int i = 0; i < CAT_COUNT; i++)
            if (categoryBtns[i]!=null) categoryBtns[i].active = (i != activeCategory);
    }

    private void onSwitchUiStyle() {
        switchingUiStyle = true;
        ChatCanvasSettings cur = ChatCanvasConfig.instance().settings();
        ChatCanvasConfig.instance().save(new ChatCanvasSettings(cur.layout(), cur.text(), cur.background(), cur.playerColors(), cur.mention(), cur.commandClipboard(), cur.recentColors(), EditorUiStyle.CHAT_CANVAS));
        EditorScreenState state = new EditorScreenState(session, activeCategory);
        if (client != null) client.setScreen(new ChatCanvasEditorScreen(parent, state));
    }

    private void returnToParent() { if (client != null) client.setScreen(parent); }

    /* ── render ──────────────────────────────────────────────── */

    @Override public void render(DrawContext ctx, int mx, int my, float d) {
        animationClock.tick();
        renderBackground(ctx, mx, my, d);
        super.render(ctx, mx, my, d);
    }
    @Override public void renderBackground(DrawContext ctx, int mx, int my, float d) {
        if (client != null && client.world == null) renderPanoramaBackground(ctx, d);
        ctx.fill(0, 0, width, height, 0x88070A10);
    }

    /* ── input ───────────────────────────────────────────────── */

    @Override public boolean keyPressed(int k, int s, int m) {
        if (Screen.hasControlDown() && k == GLFW.GLFW_KEY_Z) { undo(); return true; }
        if (Screen.hasControlDown() && k == GLFW.GLFW_KEY_Y) { redo(); return true; }
        return super.keyPressed(k, s, m);
    }
    @Override public void close() { if (switchingUiStyle) return; onCancel(); }
    @Override public void resize(MinecraftClient c, int w, int h) {
        if (session != null) session.resizeViewport(w, h);
        super.resize(c, w, h);
        clearChildren(); catWidgets.clear(); buildPersistent(); buildContent();
    }

    /* ── utilities ───────────────────────────────────────────── */

    private static String T(String key) { return Text.translatable(key).getString(); }
    private static Text onOff(boolean on, String key) {
        return Text.translatable(on?"chat_canvas.state.on":"chat_canvas.state.off")
                .append(Text.literal(" ")).append(Text.translatable(key));
    }
    private static String catName(int cat) { return switch(cat) {
        case 0->T("chat_canvas.category.layout"); case 1->T("chat_canvas.category.text");
        case 2->T("chat_canvas.category.background"); case 3->T("chat_canvas.category.player_colors");
        case 4->T("chat_canvas.category.mention"); case 5->T("chat_canvas.category.command");
        default->"?"; };}
}
