package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.animation.MotionPreset;
import io.github.ikunkk02.chatcanvas.animation.SpringValue;
import io.github.ikunkk02.chatcanvas.chat.render.PreviewChatState;
import io.github.ikunkk02.chatcanvas.chat.notification.MentionNotificationController;
import io.github.ikunkk02.chatcanvas.config.ChatTextAlignment;
import io.github.ikunkk02.chatcanvas.config.CommandClipboardConfig;
import io.github.ikunkk02.chatcanvas.config.CommandInsertMode;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.chat.command.ui.CommandToolPanel;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import io.github.ikunkk02.chatcanvas.config.MessageBackgroundMode;
import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import io.github.ikunkk02.chatcanvas.config.MentionSound;
import io.github.ikunkk02.chatcanvas.config.PlayerColorConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerColorMode;
import io.github.ikunkk02.chatcanvas.config.PlayerChatLayoutMode;
import io.github.ikunkk02.chatcanvas.voice.VoiceInputManager;
import io.github.ikunkk02.chatcanvas.voice.VoiceInputState;
import io.github.ikunkk02.chatcanvas.voice.VoiceSettings;
import io.github.ikunkk02.chatcanvas.voice.VoiceModelDescriptor;
import io.github.ikunkk02.chatcanvas.voice.VoicePlatformSupport;
import io.github.ikunkk02.chatcanvas.chat.history.ChatLogConfigStorage;
import io.github.ikunkk02.chatcanvas.chat.history.LocalChatLogService;
import io.github.ikunkk02.chatcanvas.config.ChatLogConfig;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameColorProvider;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerRosterTracker;
import io.github.ikunkk02.chatcanvas.editor.EditorSession;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.Locale;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import io.github.ikunkk02.chatcanvas.editor.EditorUiStyle;

public final class AnimatedSettingsPanel {
	private static final boolean DEBUG_CLIP = false;

	private static final int PANEL_MARGIN = 16;
	private static final int LABEL_HEIGHT = 9;
	private static final int CATEGORY_COLUMNS = 4;
	private static final int CATEGORY_ROW_HEIGHT = 22;
	private static final int CATEGORY_GAP = 3;
	private static final int CATEGORY_ROWS = 2;
	private static final int CATEGORY_HEIGHT = CATEGORY_ROW_HEIGHT * CATEGORY_ROWS + CATEGORY_GAP;

	private final EditorSession session;
	private final Runnable geometryChanged;
	private final Runnable committed;
	private final Runnable saveAction;
	private final Runnable cancelAction;
	private final Supplier<PreviewChatState> previewState;
	private final Consumer<PreviewChatState> previewStateChanged;
	private final ColorPickerLauncher colorPickerLauncher;
	private final FlowLayout component;
	private final List<NumericScrubber> scrubbers = new ArrayList<>();
	private final List<LabelComponent> descriptionLabels = new ArrayList<>();
	private final Map<Category, List<ButtonComponent>> pageButtons = new EnumMap<>(Category.class);
	private final Map<Category, List<NumericScrubber>> pageScrubbers = new EnumMap<>(Category.class);
	private final Map<Category, CategoryPage> pages = new EnumMap<>(Category.class);
	private final SpringValue categorySpring;
	private final PlayerNameColorProvider playerColorProvider = new PlayerNameColorProvider();

	private ButtonComponent openPreviewButton;
	private ButtonComponent closedPreviewButton;
	private ButtonComponent classicLayoutButton;
	private ButtonComponent splitLayoutButton;
	private ButtonComponent shadowButton;
	private ButtonComponent messageColorButton;
	private ButtonComponent inputColorButton;
	private ButtonComponent borderColorButton;
	private ButtonComponent inputBorderButton;
	private ButtonComponent playerColorsEnabledButton;
	private ButtonComponent playerAutomaticButton;
	private ButtonComponent playerVanillaButton;
	private ButtonComponent hitboxDebugButton;
	private ButtonComponent mentionDoubleClickButton;
	private ButtonComponent mentionHighlightButton;
	private ButtonComponent mentionBoldButton;
	private ButtonComponent mentionRequireAtButton;
	private ButtonComponent mentionColorButton;
	private ButtonComponent mentionSoundEnabledButton;
	private ButtonComponent mentionSoundTypeButton;
	private ButtonComponent mentionToastEnabledButton;
	private ButtonComponent mentionToastWhenOpenButton;
	private ButtonComponent mentionFlashEnabledButton;
	private ButtonComponent mentionFlashColorButton;
	private ButtonComponent mentionIgnoreOwnButton;
	private ButtonComponent mentionQuickActionsButton;
	private TextBoxComponent mentionPrivateTemplateBox;
	private ButtonComponent commandEnabledButton;
	private ButtonComponent commandPanelButton;
	private ButtonComponent commandInsertModeButton;
	private ButtonComponent commandSensitiveButton;
	private ButtonComponent commandRecordRecentButton;
	private ButtonComponent commandClearRecentOnDisconnectButton;
	private ButtonComponent commandMaxRecentButton;
	private ButtonComponent voiceEnabledButton;
	private ButtonComponent voiceDeviceButton;
	private ButtonComponent voiceTestButton;
	private ButtonComponent voiceDurationButton;
	private ButtonComponent voiceLevelButton;
	private ButtonComponent voiceThresholdButton;
	private ButtonComponent voicePartialButton;
	private ButtonComponent voicePunctuationButton;
	private ButtonComponent voiceModelButton;
	private ButtonComponent voiceModelSelectButton;
	private ButtonComponent voiceNoSpeechButton;
	private ButtonComponent voiceEndpointButton;
	private ButtonComponent voiceTailButton;
	private ButtonComponent voiceThreadsButton;
	private ButtonComponent chatLogEnabledButton;
	private ButtonComponent chatLogSelfButton;
	private ButtonComponent chatLogOthersButton;
	private ButtonComponent chatLogCommandButton;
	private ButtonComponent chatLogRetentionButton;
	private ButtonComponent chatLogMaxSizeButton;
	private ButtonComponent chatLogOpenDirButton;
	private ButtonComponent chatLogFlushButton;
	private FlowLayout playerListBody;
	private String playerSearch = "";
	private long rosterRevision = Long.MIN_VALUE;
	private PlayerColorConfig lastPlayerColors;
	private ClippedPageViewport pageHost;
	private SpringValue spring;
	private Side side;
	private Category activeCategory = Category.LAYOUT;
	private boolean categoryTransitioning;
	private int screenWidth;
	private int screenHeight;
	private int panelWidth;
	private int panelHeight;
	private UiLayoutMetrics.EditorPanel editorMetrics;

	public AnimatedSettingsPanel(EditorSession session, int screenWidth, int screenHeight,
								 Runnable geometryChanged, Runnable committed,
								 Runnable saveAction, Runnable cancelAction,
								 Supplier<PreviewChatState> previewState,
								 Consumer<PreviewChatState> previewStateChanged,
								 ColorPickerLauncher colorPickerLauncher) {
		this.session = session;
		this.geometryChanged = geometryChanged;
		this.committed = committed;
		this.saveAction = saveAction;
		this.cancelAction = cancelAction;
		this.previewState = previewState;
		this.previewStateChanged = previewStateChanged;
		this.colorPickerLauncher = colorPickerLauncher;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.editorMetrics = UiLayoutMetrics.editorPanel(screenWidth, screenHeight);
		this.panelWidth = editorMetrics.width();
		this.panelHeight = editorMetrics.height();
		this.side = session.layout().centerX() > screenWidth * 0.5 ? Side.LEFT : Side.RIGHT;
		for (Category category : Category.values()) {
			pageButtons.put(category, new ArrayList<>());
			pageScrubbers.put(category, new ArrayList<>());
		}
		double initialX = targetX();
		this.spring = new SpringValue(initialX, MotionPreset.PANEL_SLIDE);
		this.categorySpring = new SpringValue(0.0, MotionPreset.CATEGORY_SLIDE);
		this.component = buildComponent();
		this.component.positioning(Positioning.absolute((int) Math.round(initialX), editorMetrics.top()));
		setPageButtonsActive(true);
		syncFromSession();

		if (DEBUG_CLIP) {
			MinecraftClient client = MinecraftClient.getInstance();
			int fbWidth = client.getWindow().getFramebufferWidth();
			int fbHeight = client.getWindow().getFramebufferHeight();
			double scale = client.getWindow().getScaleFactor();
			ChatCanvas.LOGGER.info(
				"[ChatCanvas DEBUG] panel={}x{} atX={} viewport={}x{} guiScale={} framebuffer={}x{} activePage=LAYOUT",
				panelWidth, panelHeight, (int) Math.round(targetX()),
				pageWidth(), contentHeight(),
				scale, fbWidth, fbHeight);
		}
	}

	private FlowLayout buildComponent() {
		FlowLayout panel = Containers.verticalFlow(Sizing.fixed(panelWidth), Sizing.fixed(panelHeight));
		panel.padding(Insets.of(editorMetrics.padding()));
		panel.gap(editorMetrics.gap());
		panel.surface(ModernUiTheme.PANEL_SURFACE);

		panel.child(Components.label(Text.translatable("chat_canvas.settings.title")
				.formatted(Formatting.WHITE, Formatting.BOLD)));
		if (editorMetrics.showSubtitle()) {
			panel.child(Components.label(Text.translatable("chat_canvas.settings.subtitle")
					.formatted(Formatting.GRAY)));
		}
		panel.child(categoryTabs());

		pageHost = new ClippedPageViewport(
				Sizing.fill(100),
				Sizing.fixed(contentHeight())
		);
		CategoryPage layoutPage = buildPage(buildLayoutBody());
		CategoryPage textPage = buildPage(buildTextBody());
		CategoryPage backgroundPage = buildPage(buildBackgroundBody());
		CategoryPage playerColorsPage = buildPage(buildPlayerColorsBody());
		CategoryPage mentionPage = buildPage(buildMentionBody());
		CategoryPage commandPage = buildPage(buildCommandBody());
		CategoryPage voicePage = buildPage(buildVoiceBody());
		CategoryPage chatLogPage = buildPage(buildChatLogBody());
		pages.put(Category.LAYOUT, layoutPage);
		pages.put(Category.TEXT, textPage);
		pages.put(Category.BACKGROUND, backgroundPage);
		pages.put(Category.PLAYER_COLORS, playerColorsPage);
		pages.put(Category.MENTION, mentionPage);
		pages.put(Category.COMMAND, commandPage);
		pages.put(Category.VOICE, voicePage);
		pages.put(Category.CHAT_LOG, chatLogPage);
		layoutPage.stack.positioning(Positioning.absolute(0, 0));
		textPage.stack.positioning(Positioning.absolute(pageWidth(), 0));
		backgroundPage.stack.positioning(Positioning.absolute(pageWidth() * 2, 0));
		playerColorsPage.stack.positioning(Positioning.absolute(pageWidth() * 3, 0));
		mentionPage.stack.positioning(Positioning.absolute(pageWidth() * 4, 0));
		commandPage.stack.positioning(Positioning.absolute(pageWidth() * 5, 0));
		voicePage.stack.positioning(Positioning.absolute(pageWidth() * 6, 0));
		chatLogPage.stack.positioning(Positioning.absolute(pageWidth() * 7, 0));
		pageHost.addPage(layoutPage.stack);
		pageHost.addPage(textPage.stack);
		pageHost.addPage(backgroundPage.stack);
		pageHost.addPage(playerColorsPage.stack);
		pageHost.addPage(mentionPage.stack);
		pageHost.addPage(commandPage.stack);
		pageHost.addPage(voicePage.stack);
		pageHost.addPage(chatLogPage.stack);
		panel.child(pageHost);
		pageHost.setActivePage(Category.LAYOUT.ordinal());

		FlowLayout actions = Containers.horizontalFlow(
				Sizing.fill(100), Sizing.fixed(editorMetrics.footerHeight()));
		actions.padding(Insets.top(4));
		actions.gap(6);
		actions.horizontalAlignment(HorizontalAlignment.RIGHT);
		actions.verticalAlignment(VerticalAlignment.CENTER);
		actions.surface((context, footer) -> {
			context.fill(
					footer.x(),
					footer.y(),
					footer.x() + footer.width(),
					footer.y() + 1,
					ModernUiTheme.DIVIDER
			);
			context.fill(
					footer.x(),
					footer.y() + 1,
					footer.x() + footer.width(),
					footer.y() + footer.height(),
					ModernUiTheme.CONTROL_BACKGROUND
			);
		});
		ButtonComponent cancel = ModernUiTheme.button(Text.translatable("chat_canvas.action.cancel"),
				button -> cancelAction.run());
		cancel.sizing(Sizing.fixed(72), Sizing.fixed(22));
		ButtonComponent save = ModernUiTheme.button(Text.translatable("chat_canvas.action.save"),
				button -> saveAction.run());
		save.sizing(Sizing.fixed(72), Sizing.fixed(22));
		actions.child(cancel);
		actions.child(save);
		panel.child(actions);
		return panel;
	}

	private FlowLayout categoryTabs() {
		FlowLayout grid = Containers.verticalFlow(
				Sizing.fill(100), Sizing.fixed(CATEGORY_HEIGHT));
		grid.gap(CATEGORY_GAP);
		grid.allowOverflow(false);

		int availableWidth = pageWidth();
		int tabWidth = Math.max(1,
				(availableWidth - CATEGORY_GAP * (CATEGORY_COLUMNS - 1)) / CATEGORY_COLUMNS);
		Category[] categories = Category.values();
		for (int rowIndex = 0; rowIndex < CATEGORY_ROWS; rowIndex++) {
			FlowLayout row = Containers.horizontalFlow(
					Sizing.fill(100), Sizing.fixed(CATEGORY_ROW_HEIGHT));
			row.gap(CATEGORY_GAP);
			for (int column = 0; column < CATEGORY_COLUMNS; column++) {
				int categoryIndex = rowIndex * CATEGORY_COLUMNS + column;
				if (categoryIndex >= categories.length) break;
				Category category = categories[categoryIndex];
				Text fullText = Text.translatable(category.translationKey);
				Text tabText = Text.translatable(category.tabTranslationKey);
				String fitted = ModernUiTheme.fitText(
						MinecraftClient.getInstance().textRenderer,
						tabText, Math.max(1, tabWidth - 6));
				ButtonComponent button = ModernUiTheme.transparentButton(
						Text.literal(fitted), clicked -> switchCategory(category));
				button.renderer((context, component, delta) ->
						ModernUiTheme.drawFixedTab(
								context,
								component.getX(), component.getY(),
								component.getWidth(), component.getHeight(),
								component.isHovered(), activeCategory == category,
								component.active()));
				button.sizing(Sizing.fixed(tabWidth), Sizing.fixed(CATEGORY_ROW_HEIGHT));
				button.tooltip(fullText);
				row.child(button);
			}
			grid.child(row);
		}
		return grid;
	}

	private FlowLayout buildLayoutBody() {
		FlowLayout body = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		body.padding(Insets.bottom(8));
		body.gap(7);
		body.child(sectionLabel("chat_canvas.category.layout"));
		body.child(Components.label(Text.translatable("chat_canvas.preview.state")
				.formatted(Formatting.GRAY)));
		body.child(previewStateRow());
		body.child(Components.label(Text.translatable("chat_canvas.player_layout.mode")
				.formatted(Formatting.GRAY)));
		body.child(playerLayoutSelector());
		SplitMessageRatioScrubberComponent splitRatio =
				new SplitMessageRatioScrubberComponent(
						session,
						settingLabel("chat_canvas.player_layout.max_width"),
						geometryChanged,
						committed);
		registerScrubber(Category.LAYOUT, splitRatio);
		body.child(splitRatio);
		body.child(layoutScrubber(NumericScrubberComponent.Property.X, "chat_canvas.option.x"));
		body.child(layoutScrubber(NumericScrubberComponent.Property.Y, "chat_canvas.option.y"));
		body.child(layoutScrubber(NumericScrubberComponent.Property.WIDTH, "chat_canvas.option.width"));
		body.child(layoutScrubber(NumericScrubberComponent.Property.HEIGHT, "chat_canvas.option.height"));

		ButtonComponent defaults = ModernUiTheme.button(
				Text.translatable("chat_canvas.action.restore_defaults"),
				button -> {
					session.restoreLayoutDefaults();
					syncFromSession();
					geometryChanged.run();
					committed.run();
				});
		defaults.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.LAYOUT, defaults);
		body.child(defaults);

		return body;
	}

	private StackLayout playerLayoutSelector() {
		StackLayout stack = Containers.stack(Sizing.fill(100), Sizing.fixed(24));
		stack.child(new SelectionIndicatorComponent(
				() -> session.playerChatLayoutMode().ordinal(),
				PlayerChatLayoutMode.values().length));
		FlowLayout buttons = Containers.horizontalFlow(
				Sizing.fill(100), Sizing.fill(100));
		classicLayoutButton = transparentButton(Text.empty(),
				clicked -> selectPlayerLayout(PlayerChatLayoutMode.CLASSIC));
		splitLayoutButton = transparentButton(Text.empty(),
				clicked -> selectPlayerLayout(PlayerChatLayoutMode.SPLIT_ALIGNMENT));
		classicLayoutButton.sizing(Sizing.fill(50), Sizing.fill(100));
		splitLayoutButton.sizing(Sizing.fill(50), Sizing.fill(100));
		registerPageButton(Category.LAYOUT, classicLayoutButton);
		registerPageButton(Category.LAYOUT, splitLayoutButton);
		buttons.child(classicLayoutButton);
		buttons.child(splitLayoutButton);
		stack.child(buttons);
		return stack;
	}

	private void selectPlayerLayout(PlayerChatLayoutMode mode) {
		if (session.selectedChannel() != io.github.ikunkk02.chatcanvas.editor.EditorChannel.PLAYER_CHAT
				|| session.playerChatLayoutMode() == mode) {
			return;
		}
		session.setPlayerChatLayoutMode(mode);
		session.commit();
		geometryChanged.run();
		committed.run();
		syncFromSession();
	}

	private FlowLayout buildMentionBody() {
		FlowLayout body = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		body.padding(Insets.bottom(8));
		body.gap(7);
		body.child(sectionLabel("chat_canvas.category.mention"));
		body.child(descriptionLabel("chat_canvas.mention.hint"));

		mentionDoubleClickButton = mentionToggleButton(
				"chat_canvas.mention.double_click",
				config -> config.withDoubleClickEnabled(!config.doubleClickEnabled()));
		body.child(mentionDoubleClickButton);

		MentionNumericScrubberComponent interval = new MentionNumericScrubberComponent(
				session,
				MentionNumericScrubberComponent.Property.DOUBLE_CLICK,
				settingLabel("chat_canvas.mention.double_click_interval"),
				geometryChanged,
				committed);
		registerScrubber(Category.MENTION, interval);
		body.child(interval);

		mentionHighlightButton = mentionToggleButton(
				"chat_canvas.mention.highlight",
				config -> config.withHighlightEnabled(!config.highlightEnabled()));
		body.child(mentionHighlightButton);

		mentionColorButton = ModernUiTheme.button(Text.empty(), clicked -> {
			MentionConfig before = session.mention();
			colorPickerLauncher.open(clicked, new ModernColorPickerPopup.Request(
					before.highlightColor(),
					MentionConfig.DEFAULT.highlightColor(),
					session.recentColors().colors(),
					color -> {
						session.setMention(session.mention().withHighlightColor(color));
						geometryChanged.run();
						syncFromSession();
					},
					color -> {
						session.recentColors().add(color);
						session.commit();
						committed.run();
						syncFromSession();
					},
					() -> {
						session.setMention(before);
						geometryChanged.run();
						syncFromSession();
					}
			));
		});
		mentionColorButton.sizing(Sizing.fill(100), Sizing.fixed(22));
		mentionColorButton.renderer((context, component, delta) -> {
			int background = component.active()
					? component.isHovered() ? ModernUiTheme.CONTROL_HOVER : ModernUiTheme.CONTROL_BACKGROUND
					: ModernUiTheme.CONTROL_DISABLED;
			ModernUiTheme.roundedRect(context, component.getX(), component.getY(),
					component.getWidth(), component.getHeight(), 2, background);
			ModernUiTheme.border(context, component.getX(), component.getY(),
					component.getWidth(), component.getHeight(), ModernUiTheme.PANEL_BORDER);
			ModernUiTheme.roundedRect(context, component.getX() + 5, component.getY() + 4,
					14, component.getHeight() - 8, 1,
					0xFF000000 | session.mention().highlightColor());
			context.drawRectOutline(component.getX() + 5, component.getY() + 4,
					14, component.getHeight() - 8, ModernUiTheme.PANEL_BORDER);
		});
		registerPageButton(Category.MENTION, mentionColorButton);
		body.child(mentionColorButton);

		mentionBoldButton = mentionToggleButton(
				"chat_canvas.mention.highlight_bold",
				config -> config.withHighlightBold(!config.highlightBold()));
		body.child(mentionBoldButton);

		mentionRequireAtButton = mentionToggleButton(
				"chat_canvas.mention.require_at",
				config -> config.withRequireAtSymbol(!config.requireAtSymbol()));
		body.child(mentionRequireAtButton);

		body.child(sectionLabel("chat_canvas.mention.section.notification"));
		mentionSoundEnabledButton = mentionToggleButton(
				"chat_canvas.mention.sound_enabled",
				config -> config.withSoundEnabled(!config.soundEnabled()));
		body.child(mentionSoundEnabledButton);
		mentionSoundTypeButton = ModernUiTheme.button(Text.empty(), clicked -> {
			MentionConfig before = session.mention();
			MentionSound[] values = MentionSound.values();
			session.setMention(before.withSound(
					values[(before.sound().ordinal() + 1) % values.length]));
			session.commit();
			committed.run();
			syncFromSession();
		});
		mentionSoundTypeButton.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.MENTION, mentionSoundTypeButton);
		body.child(mentionSoundTypeButton);
		body.child(mentionScrubber(MentionNumericScrubberComponent.Property.SOUND_VOLUME,
				"chat_canvas.mention.sound_volume"));
		body.child(mentionScrubber(MentionNumericScrubberComponent.Property.SOUND_PITCH,
				"chat_canvas.mention.sound_pitch"));
		ButtonComponent testSound = ModernUiTheme.button(
				Text.translatable("chat_canvas.mention.test_sound"),
				clicked -> MentionNotificationController.instance().testSound(session.mention()));
		testSound.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.MENTION, testSound);
		body.child(testSound);

		body.child(sectionLabel("chat_canvas.mention.section.toast"));
		mentionToastEnabledButton = mentionToggleButton(
				"chat_canvas.mention.toast_enabled",
				config -> config.withToastEnabled(!config.toastEnabled()));
		body.child(mentionToastEnabledButton);
		mentionToastWhenOpenButton = mentionToggleButton(
				"chat_canvas.mention.toast_when_open",
				config -> config.withToastWhenChatOpen(!config.toastWhenChatOpen()));
		body.child(mentionToastWhenOpenButton);
		body.child(mentionScrubber(MentionNumericScrubberComponent.Property.TOAST_LENGTH,
				"chat_canvas.mention.toast_length"));

		body.child(sectionLabel("chat_canvas.mention.section.flash"));
		mentionFlashEnabledButton = mentionToggleButton(
				"chat_canvas.mention.flash_enabled",
				config -> config.withFlashEnabled(!config.flashEnabled()));
		body.child(mentionFlashEnabledButton);
		mentionFlashColorButton = ModernUiTheme.button(Text.empty(), clicked -> {
			MentionConfig before = session.mention();
			colorPickerLauncher.open(clicked, new ModernColorPickerPopup.Request(
					before.flashColor(),
					MentionConfig.DEFAULT.flashColor(),
					session.recentColors().colors(),
					color -> {
						session.setMention(session.mention().withFlashColor(color));
						geometryChanged.run();
						syncFromSession();
					},
					color -> {
						session.recentColors().add(color);
						session.commit();
						committed.run();
						syncFromSession();
					},
					() -> {
						session.setMention(before);
						geometryChanged.run();
						syncFromSession();
					}
			));
		});
		mentionFlashColorButton.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.MENTION, mentionFlashColorButton);
		body.child(mentionFlashColorButton);
		body.child(mentionScrubber(MentionNumericScrubberComponent.Property.FLASH_OPACITY,
				"chat_canvas.mention.flash_opacity"));
		body.child(mentionScrubber(MentionNumericScrubberComponent.Property.FLASH_DURATION,
				"chat_canvas.mention.flash_duration"));

		body.child(sectionLabel("chat_canvas.mention.section.other"));
		mentionIgnoreOwnButton = mentionToggleButton(
				"chat_canvas.mention.ignore_own",
				config -> config.withIgnoreOwnMessages(!config.ignoreOwnMessages()));
		body.child(mentionIgnoreOwnButton);
		mentionQuickActionsButton = mentionToggleButton(
				"chat_canvas.mention.quick_actions",
				config -> config.withPlayerQuickActionsEnabled(!config.playerQuickActionsEnabled()));
		body.child(mentionQuickActionsButton);
		body.child(Components.label(settingLabel("chat_canvas.mention.private_template")));
		mentionPrivateTemplateBox = Components.textBox(Sizing.fill(100));
		mentionPrivateTemplateBox.text(session.mention().privateMessageTemplate());
		mentionPrivateTemplateBox.onChanged().subscribe(value -> {
			MentionConfig before = session.mention();
			session.setMention(before.withPrivateMessageTemplate(value));
			if (!before.equals(session.mention())) {
				session.commit();
				committed.run();
			}
		});
		body.child(mentionPrivateTemplateBox);

		ButtonComponent defaults = ModernUiTheme.button(
				Text.translatable("chat_canvas.mention.restore_defaults"), button -> {
					MentionConfig before = session.mention();
					session.restoreMentionDefaults();
					if (!before.equals(session.mention())) {
						geometryChanged.run();
						committed.run();
					}
					syncFromSession();
				});
		defaults.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.MENTION, defaults);
		body.child(defaults);
		return body;
	}

	private FlowLayout buildCommandBody() {
		FlowLayout body = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		body.padding(Insets.bottom(8));
		body.gap(7);
		body.child(sectionLabel("chat_canvas.category.command"));
		body.child(descriptionLabel("chat_canvas.command.settings_hint"));
		commandEnabledButton = commandToggle("chat_canvas.command.enabled",
				config -> config.withEnabled(!config.enabled()));
		commandPanelButton = commandToggle("chat_canvas.command.show_button",
				config -> config.withShowPanelButton(!config.showPanelButton()));
		commandInsertModeButton = ModernUiTheme.button(Text.empty(), clicked -> {
			CommandClipboardConfig config = session.commandClipboard();
			session.setCommandClipboard(config.withInsertMode(config.insertMode().opposite()));
			session.commit();
			committed.run();
			syncFromSession();
		});
		commandInsertModeButton.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.COMMAND, commandInsertModeButton);
		commandSensitiveButton = commandToggle("chat_canvas.command.sensitive_warning",
				config -> config.withSensitiveWarning(!config.sensitiveWarning()));
		commandRecordRecentButton = commandToggle(
				"chat_canvas.command.record_recent",
				config -> config.withRecordRecentCommands(
						!config.recordRecentCommands()));
		commandClearRecentOnDisconnectButton = commandToggle(
				"chat_canvas.command.clear_recent_disconnect",
				config -> config.withClearRecentOnDisconnect(
						!config.clearRecentOnDisconnect()));
		commandMaxRecentButton = ModernUiTheme.button(Text.empty(), clicked -> {
			CommandClipboardConfig config = session.commandClipboard();
			int next = config.maxRecentCommands() >=
					CommandClipboardConfig.MAX_RECENT_COMMANDS
					? CommandClipboardConfig.MIN_RECENT_COMMANDS
					: config.maxRecentCommands() + 10;
			session.setCommandClipboard(config.withMaxRecentCommands(next));
			session.commit();
			committed.run();
			syncFromSession();
		});
		commandMaxRecentButton.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.COMMAND, commandMaxRecentButton);
		body.child(commandEnabledButton);
		body.child(commandPanelButton);
		body.child(commandInsertModeButton);
		body.child(commandSensitiveButton);
		body.child(commandRecordRecentButton);
		body.child(commandClearRecentOnDisconnectButton);
		body.child(commandMaxRecentButton);
		CommandMaxScrubberComponent maxCommands =
				new CommandMaxScrubberComponent(session, geometryChanged, committed);
		registerScrubber(Category.COMMAND, maxCommands);
		body.child(maxCommands);
		ButtonComponent manage = ModernUiTheme.button(
				Text.translatable("chat_canvas.command.manage"), clicked -> {
					MinecraftClient client = MinecraftClient.getInstance();
					if (client.player == null) return;
					ChatCanvasConfig.instance().save(session.settings());
					CommandToolPanel.requestOpenNextChatScreen();
					client.setScreen(new ChatScreen("/", false));
				});
		manage.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.COMMAND, manage);
		body.child(manage);
		ButtonComponent defaults = ModernUiTheme.button(
				Text.translatable("chat_canvas.command.restore_defaults"), clicked -> {
					session.restoreCommandClipboardDefaults();
					committed.run();
					syncFromSession();
				});
		defaults.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.COMMAND, defaults);
		body.child(defaults);
		return body;
	}

	private ButtonComponent commandToggle(
			String key, java.util.function.UnaryOperator<CommandClipboardConfig> toggle) {
		ButtonComponent button = ModernUiTheme.button(Text.empty(), clicked -> {
			CommandClipboardConfig before = session.commandClipboard();
			session.setCommandClipboard(toggle.apply(before));
			if (!before.equals(session.commandClipboard())) {
				session.commit();
				committed.run();
			}
			syncFromSession();
		});
		button.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.COMMAND, button);
		return button;
	}

	private FlowLayout buildVoiceBody() {
		FlowLayout body = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		body.padding(Insets.bottom(8));
		body.gap(7);
		body.child(sectionLabel("chat_canvas.category.voice"));
		body.child(descriptionLabel("chat_canvas.voice.settings_hint"));
		voiceEnabledButton = voiceButton(clicked -> updateVoice(settings ->
				settings.withEnabled(!settings.enabled())));
		voiceDeviceButton = voiceButton(clicked -> {
			var manager = VoiceInputManager.instance();
			var devices = manager.devices();
			VoiceSettings settings = manager.settings();
			if (devices.isEmpty()) return;
			int current = -1;
			for (int i = 0; i < devices.size(); i++) {
				if (devices.get(i).id().equals(settings.microphoneId())) current = i;
			}
			String next = devices.get((current + 1) % devices.size()).id();
			updateVoice(value -> value.withMicrophoneId(next));
		});
		voiceTestButton = voiceButton(clicked ->
				VoiceInputManager.instance().toggleMicrophoneTest());
		voiceDurationButton = voiceButton(clicked -> updateVoice(settings -> {
			int next = settings.maximumSeconds() >= 60 ? 5 : settings.maximumSeconds() + 5;
			return settings.withMaximumSeconds(next);
		}));
		voiceLevelButton = voiceButton(clicked -> updateVoice(settings ->
				settings.withShowInputLevel(!settings.showInputLevel())));
		voiceThresholdButton = voiceButton(clicked -> updateVoice(settings -> {
			double next = settings.noiseThreshold() >= 0.05
					? 0.005 : settings.noiseThreshold() + 0.005;
			return settings.withNoiseThreshold(next);
		}));
		voicePartialButton = voiceButton(clicked -> updateVoice(settings ->
				settings.withShowPartialResults(!settings.showPartialResults())));
		voicePunctuationButton = voiceButton(clicked -> updateVoice(settings ->
				settings.withFinalPunctuation(!settings.addFinalPunctuation())));
		voiceNoSpeechButton = voiceButton(clicked -> updateVoice(settings -> {
			int next = settings.noSpeechTimeoutMillis() >= 10_000
					? 3_000 : settings.noSpeechTimeoutMillis() + 1_000;
			return settings.withEndpointTiming(next, settings.endpointSilenceMillis(),
					settings.tailPaddingMillis());
		}));
		voiceEndpointButton = voiceButton(clicked -> updateVoice(settings -> {
			int next = settings.endpointSilenceMillis() >= 1_200
					? 600 : settings.endpointSilenceMillis() + 100;
			return settings.withEndpointTiming(settings.noSpeechTimeoutMillis(), next,
					settings.tailPaddingMillis());
		}));
		voiceTailButton = voiceButton(clicked -> updateVoice(settings -> {
			int next = settings.tailPaddingMillis() >= 400 ? 100 : settings.tailPaddingMillis() + 50;
			return settings.withEndpointTiming(settings.noSpeechTimeoutMillis(),
					settings.endpointSilenceMillis(), next);
		}));
		voiceThreadsButton = voiceButton(clicked -> updateVoice(settings ->
				settings.withInferenceThreads(settings.inferenceThreads() >= 4
						? 0 : settings.inferenceThreads() + 1)));
		ButtonComponent binding = voiceButton(clicked -> {});
		binding.active(false);
		binding.setMessage(Text.translatable("chat_canvas.voice.keybinding"));
		ButtonComponent insertMode = voiceButton(clicked -> {});
		insertMode.active(false);
		insertMode.setMessage(Text.translatable("chat_canvas.voice.insert_mode"));
		voiceModelSelectButton = voiceButton(clicked -> {
			VoiceInputManager manager = VoiceInputManager.instance();
			var available = manager.models();
			if (available.isEmpty()) return;
			int current = -1;
			for (int index = 0; index < available.size(); index++) {
				if (available.get(index).id().equals(manager.settings().selectedModelId())) current = index;
			}
			for (int offset = 1; offset <= available.size(); offset++) {
				VoiceModelDescriptor candidate = available.get((current + offset) % available.size());
				if (VoicePlatformSupport.supportsModel(VoicePlatformSupport.current(), candidate)) {
					manager.selectModel(candidate.id());
					break;
				}
			}
		});
		voiceModelButton = voiceButton(clicked -> {
			VoiceInputManager manager = VoiceInputManager.instance();
			VoiceModelDescriptor selected = manager.selectedModel();
			if (manager.state() == VoiceInputState.MODEL_DOWNLOADING
					|| manager.state() == VoiceInputState.MODEL_VERIFYING
					|| manager.state() == VoiceInputState.MODEL_EXTRACTING
					|| manager.state() == VoiceInputState.MODEL_INSTALLING) {
				manager.cancelModelInstall();
			} else if (selected == null || !manager.isModelInstalled(selected.id())) manager.installModel();
		});
		ButtonComponent openModel = voiceButton(clicked ->
				VoiceInputManager.instance().openModelsDirectory());
		openModel.setMessage(Text.translatable("chat_canvas.voice.open_model_directory"));
		ButtonComponent releaseModel = voiceButton(clicked ->
				VoiceInputManager.instance().releaseModel());
		releaseModel.setMessage(Text.translatable("chat_canvas.voice.release_model"));
		body.child(voiceEnabledButton);
		body.child(voiceDeviceButton);
		body.child(voiceTestButton);
		body.child(binding);
		body.child(voiceDurationButton);
		body.child(voiceNoSpeechButton);
		body.child(voiceEndpointButton);
		body.child(voiceTailButton);
		body.child(voiceThreadsButton);
		body.child(voiceLevelButton);
		body.child(voiceThresholdButton);
		body.child(voicePartialButton);
		body.child(insertMode);
		body.child(voicePunctuationButton);
		body.child(voiceModelSelectButton);
		body.child(voiceModelButton);
		body.child(openModel);
		body.child(releaseModel);
		body.child(descriptionLabel("chat_canvas.voice.privacy"));
		return body;
	}

	private FlowLayout buildChatLogBody() {
		FlowLayout body = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		body.padding(Insets.bottom(8));
		body.gap(7);
		body.child(sectionLabel("chat_canvas.category.chat_log"));
		body.child(descriptionLabel("chat_canvas.chat_log.settings_hint"));
		chatLogEnabledButton = chatLogToggle("chat_canvas.chat_log.enabled",
				config -> config.withEnabled(!config.enabled()));
		chatLogSelfButton = chatLogToggle("chat_canvas.chat_log.save_self",
				config -> config.withSaveSelfMessages(!config.saveSelfMessages()));
		chatLogOthersButton = chatLogToggle("chat_canvas.chat_log.save_others",
				config -> config.withSaveOtherPlayersMessages(!config.saveOtherPlayersMessages()));
		chatLogCommandButton = chatLogToggle("chat_canvas.chat_log.save_command",
				config -> config.withSaveCommandSystemMessages(!config.saveCommandSystemMessages()));
		chatLogRetentionButton = ModernUiTheme.button(Text.empty(), clicked -> {
			ChatLogConfig config = LocalChatLogService.instance().config();
			int next = config.retentionDays() >= 365 ? 0
					: config.retentionDays() >= 90 ? 365
					: config.retentionDays() >= 30 ? 90
					: config.retentionDays() >= 7 ? 30
					: config.retentionDays() > 0 ? 0 : 7;
			updateChatLog(config.withRetentionDays(next));
		});
		chatLogRetentionButton.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.CHAT_LOG, chatLogRetentionButton);
		chatLogMaxSizeButton = ModernUiTheme.button(Text.empty(), clicked -> {
			ChatLogConfig config = LocalChatLogService.instance().config();
			long next = config.maxFileSizeBytes() >= 100L * 1024 * 1024L
					? ChatLogConfig.MIN_FILE_SIZE_BYTES
					: config.maxFileSizeBytes() >= 50L * 1024 * 1024L
					? 100L * 1024 * 1024L
					: config.maxFileSizeBytes() >= 20L * 1024 * 1024L
					? 50L * 1024 * 1024L
					: config.maxFileSizeBytes() >= 10L * 1024 * 1024L
					? 20L * 1024 * 1024L
					: 10L * 1024 * 1024L;
			updateChatLog(config.withMaxFileSizeBytes(next));
		});
		chatLogMaxSizeButton.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.CHAT_LOG, chatLogMaxSizeButton);
		chatLogOpenDirButton = ModernUiTheme.button(
				Text.translatable("chat_canvas.chat_log.open_dir"),
				clicked -> LocalChatLogService.instance().openLogsDirectory());
		chatLogOpenDirButton.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.CHAT_LOG, chatLogOpenDirButton);
		chatLogFlushButton = ModernUiTheme.button(
				Text.translatable("chat_canvas.chat_log.flush"),
				clicked -> LocalChatLogService.instance().flush());
		chatLogFlushButton.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.CHAT_LOG, chatLogFlushButton);
		body.child(chatLogEnabledButton);
		body.child(chatLogSelfButton);
		body.child(chatLogOthersButton);
		body.child(chatLogCommandButton);
		body.child(chatLogRetentionButton);
		body.child(chatLogMaxSizeButton);
		body.child(chatLogOpenDirButton);
		body.child(chatLogFlushButton);
		body.child(descriptionLabel("chat_canvas.chat_log.privacy"));
		return body;
	}

	private ButtonComponent chatLogToggle(
			String key, java.util.function.UnaryOperator<ChatLogConfig> toggle) {
		ButtonComponent button = ModernUiTheme.button(Text.empty(), clicked -> {
			ChatLogConfig before = LocalChatLogService.instance().config();
			updateChatLog(toggle.apply(before));
		});
		button.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.CHAT_LOG, button);
		return button;
	}

	private void updateChatLog(ChatLogConfig value) {
		LocalChatLogService.instance().updateConfig(value);
		new ChatLogConfigStorage().save(value);
		syncChatLogButtons();
	}

	private void syncChatLogButtons() {
		ChatLogConfig config = LocalChatLogService.instance().config();
		setToggleMessage(chatLogEnabledButton, "chat_canvas.chat_log.enabled", config.enabled());
		setToggleMessage(chatLogSelfButton, "chat_canvas.chat_log.save_self", config.saveSelfMessages());
		setToggleMessage(chatLogOthersButton, "chat_canvas.chat_log.save_others", config.saveOtherPlayersMessages());
		setToggleMessage(chatLogCommandButton, "chat_canvas.chat_log.save_command", config.saveCommandSystemMessages());
		if (chatLogRetentionButton != null) {
			chatLogRetentionButton.setMessage(
				Text.translatable("chat_canvas.chat_log.retention_days")
					.append(Text.literal("  " + (config.retentionDays() == 0 ? "∞" : String.valueOf(config.retentionDays())))));
		}
		if (chatLogMaxSizeButton != null) {
			long mb = config.maxFileSizeBytes() / (1024 * 1024);
			chatLogMaxSizeButton.setMessage(
				Text.translatable("chat_canvas.chat_log.max_size_mb")
					.append(Text.literal("  " + mb + " MB")));
		}
	}

	private ButtonComponent voiceButton(Consumer<ButtonComponent> action) {
		ButtonComponent button = ModernUiTheme.button(Text.empty(), action);
		button.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.VOICE, button);
		return button;
	}

	private void updateVoice(java.util.function.UnaryOperator<VoiceSettings> operation) {
		VoiceInputManager manager = VoiceInputManager.instance();
		manager.updateSettings(operation.apply(manager.settings()));
		syncVoiceButtons();
	}

	private MentionNumericScrubberComponent mentionScrubber(
			MentionNumericScrubberComponent.Property property, String key) {
		MentionNumericScrubberComponent component = new MentionNumericScrubberComponent(
				session, property, settingLabel(key),
				geometryChanged, committed);
		registerScrubber(Category.MENTION, component);
		return component;
	}

	private ButtonComponent mentionToggleButton(
			String translationKey,
			java.util.function.UnaryOperator<MentionConfig> toggle) {
		ButtonComponent button = ModernUiTheme.button(Text.empty(), clicked -> {
			MentionConfig before = session.mention();
			session.setMention(toggle.apply(before));
			if (!before.equals(session.mention())) {
				session.commit();
				geometryChanged.run();
				committed.run();
			}
			syncFromSession();
		});
		button.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.MENTION, button);
		button.id(translationKey);
		return button;
	}

	private FlowLayout buildPlayerColorsBody() {
		FlowLayout body = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		body.padding(Insets.bottom(8));
		body.gap(7);
		body.child(sectionLabel("chat_canvas.category.player_colors"));

		playerColorsEnabledButton = ModernUiTheme.button(Text.empty(), button -> {
			PlayerColorConfig before = session.playerColors();
			session.setPlayerColors(before.withEnabled(!before.enabled()));
			session.commit();
			geometryChanged.run();
			committed.run();
			syncFromSession();
		});
		playerColorsEnabledButton.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.PLAYER_COLORS, playerColorsEnabledButton);
		body.child(playerColorsEnabledButton);

		body.child(Components.label(settingLabel("chat_canvas.player_colors.mode")));
		FlowLayout modes = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
		modes.gap(6);
		playerAutomaticButton = ModernUiTheme.button(Text.empty(), button ->
				setPlayerColorMode(PlayerColorMode.AUTOMATIC));
		playerVanillaButton = ModernUiTheme.button(Text.empty(), button ->
				setPlayerColorMode(PlayerColorMode.VANILLA));
		playerAutomaticButton.sizing(Sizing.fill(50), Sizing.fixed(22));
		playerVanillaButton.sizing(Sizing.fill(50), Sizing.fixed(22));
		registerPageButton(Category.PLAYER_COLORS, playerAutomaticButton);
		registerPageButton(Category.PLAYER_COLORS, playerVanillaButton);
		modes.child(playerAutomaticButton);
		modes.child(playerVanillaButton);
		body.child(modes);

		body.child(sectionLabel("chat_canvas.player_colors.palette"));
		FlowLayout palette = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		palette.gap(4);
		FlowLayout paletteRow = null;
		for (int index = 0; index < session.playerColors().palette().size(); index++) {
			if (index % 6 == 0) {
				paletteRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
				paletteRow.gap(4);
				palette.child(paletteRow);
			}
			final int paletteIndex = index;
			ButtonComponent swatch = ModernUiTheme.button(Text.empty(),
					button -> openPaletteColorPicker(button, paletteIndex));
			swatch.sizing(Sizing.fixed(22), Sizing.fixed(22));
			swatch.renderer((context, component, delta) -> {
				int color = session.playerColors().palette().get(
						Math.min(paletteIndex, session.playerColors().palette().size() - 1));
				ModernUiTheme.roundedRect(context, component.getX(), component.getY(),
						component.getWidth(), component.getHeight(), 1, 0xFF000000 | color);
				ModernUiTheme.border(context, component.getX(), component.getY(),
						component.getWidth(), component.getHeight(), ModernUiTheme.PANEL_BORDER);
			});
			registerPageButton(Category.PLAYER_COLORS, swatch);
			paletteRow.child(swatch);
		}
		body.child(palette);
		ButtonComponent restorePalette = ModernUiTheme.button(
				Text.translatable("chat_canvas.player_colors.restore_palette"), button -> {
					PlayerColorConfig before = session.playerColors();
					session.setPlayerColors(before.withDefaultPalette());
					if (!before.equals(session.playerColors())) {
						session.commit();
						geometryChanged.run();
						committed.run();
					}
					syncFromSession();
				});
		restorePalette.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.PLAYER_COLORS, restorePalette);
		body.child(restorePalette);

		body.child(sectionLabel("chat_canvas.player_colors.online"));
		TextBoxComponent search = Components.textBox(Sizing.fill(100));
		search.setPlaceholder(Text.translatable("chat_canvas.player_colors.search"));
		search.onChanged().subscribe(value -> {
			playerSearch = value == null ? "" : value;
			rebuildPlayerRows();
		});
		body.child(search);
		playerListBody = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		playerListBody.gap(4);
		body.child(playerListBody);
		rebuildPlayerRows();

		hitboxDebugButton = ModernUiTheme.button(Text.empty(), button -> {
			PlayerColorConfig before = session.playerColors();
			session.setPlayerColors(before.withShowNameHitboxes(!before.showNameHitboxes()));
			session.commit();
			geometryChanged.run();
			committed.run();
			syncFromSession();
		});
		hitboxDebugButton.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.PLAYER_COLORS, hitboxDebugButton);
		body.child(hitboxDebugButton);

		ButtonComponent defaults = ModernUiTheme.button(
				Text.translatable("chat_canvas.player_colors.restore_defaults"), button -> {
					PlayerColorConfig before = session.playerColors();
					session.restorePlayerColorDefaults();
					if (!before.equals(session.playerColors())) {
						geometryChanged.run();
						committed.run();
					}
					syncFromSession();
				});
		defaults.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.PLAYER_COLORS, defaults);
		body.child(defaults);
		return body;
	}

	private FlowLayout buildBackgroundBody() {
		FlowLayout body = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		body.padding(Insets.bottom(8));
		body.gap(7);

		body.child(sectionLabel("chat_canvas.background.message"));
		body.child(Components.label(settingLabel("chat_canvas.background.mode")));
		body.child(messageModeSelector());

		messageColorButton = colorButton(
				ColorTarget.MESSAGE,
				"chat_canvas.background.color",
				ChatBackgroundConfig.DEFAULT.messageColor());
		body.child(messageColorButton);
		body.child(backgroundScrubber(
				BackgroundNumericScrubberComponent.Property.MESSAGE_OPACITY,
				"chat_canvas.background.opacity"));
		body.child(backgroundScrubber(
				BackgroundNumericScrubberComponent.Property.HORIZONTAL_PADDING,
				"chat_canvas.background.horizontal_padding"));
		body.child(backgroundScrubber(
				BackgroundNumericScrubberComponent.Property.VERTICAL_PADDING,
				"chat_canvas.background.vertical_padding"));

		body.child(sectionLabel("chat_canvas.background.input"));
		inputColorButton = colorButton(
				ColorTarget.INPUT,
				"chat_canvas.background.input_color",
				ChatBackgroundConfig.DEFAULT.inputColor());
		body.child(inputColorButton);
		body.child(backgroundScrubber(
				BackgroundNumericScrubberComponent.Property.INPUT_OPACITY,
				"chat_canvas.background.input_opacity"));

		inputBorderButton = ModernUiTheme.button(Text.empty(), button -> {
			ChatBackgroundConfig before = session.background();
			session.setBackground(before.withInputBorderEnabled(!before.inputBorderEnabled()));
			session.commit();
			geometryChanged.run();
			committed.run();
			syncFromSession();
		});
		inputBorderButton.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.BACKGROUND, inputBorderButton);
		body.child(inputBorderButton);

		borderColorButton = colorButton(
				ColorTarget.BORDER,
				"chat_canvas.background.border_color",
				ChatBackgroundConfig.DEFAULT.inputBorderColor());
		body.child(borderColorButton);
		body.child(backgroundScrubber(
				BackgroundNumericScrubberComponent.Property.BORDER_OPACITY,
				"chat_canvas.background.border_opacity"));

		ButtonComponent defaults = ModernUiTheme.button(
				Text.translatable("chat_canvas.action.restore_background_defaults"),
				button -> {
					ChatBackgroundConfig before = session.background();
					session.restoreBackgroundDefaults();
					if (!before.equals(session.background())) {
						geometryChanged.run();
						committed.run();
					}
					syncFromSession();
				});
		defaults.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.BACKGROUND, defaults);
		body.child(defaults);
		return body;
	}

	private FlowLayout buildTextBody() {
		FlowLayout body = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		body.padding(Insets.bottom(8));
		body.gap(7);
		body.child(sectionLabel("chat_canvas.category.text"));
		body.child(textScrubber(TextNumericScrubberComponent.Property.FONT_SCALE,
				"chat_canvas.option.font_scale"));
		body.child(textScrubber(TextNumericScrubberComponent.Property.LINE_SPACING,
				"chat_canvas.option.line_spacing"));
		body.child(textScrubber(TextNumericScrubberComponent.Property.TEXT_OPACITY,
				"chat_canvas.option.text_opacity"));
		body.child(textScrubber(TextNumericScrubberComponent.Property.CHARACTER_SPACING,
				"chat_canvas.option.character_spacing"));
		body.child(Components.label(settingLabel("chat_canvas.option.text_alignment")));
		body.child(alignmentSelector());

		shadowButton = ModernUiTheme.button(Text.empty(), button -> {
			ChatTextConfig before = session.text();
			session.setText(new ChatTextConfig(
					before.fontScale(), before.lineSpacing(), before.textOpacity(),
					before.alignment(), !before.shadow(), before.characterSpacing()));
			session.commit();
			geometryChanged.run();
			committed.run();
			syncFromSession();
		});
		shadowButton.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.TEXT, shadowButton);
		body.child(shadowButton);

		ButtonComponent defaults = ModernUiTheme.button(
				Text.translatable("chat_canvas.action.restore_text_defaults"),
				button -> {
					ChatTextConfig before = session.text();
					session.restoreTextDefaults();
					if (!before.equals(session.text())) {
						geometryChanged.run();
						committed.run();
					}
					syncFromSession();
				});
		defaults.sizing(Sizing.fill(100), Sizing.fixed(22));
		registerPageButton(Category.TEXT, defaults);
		body.child(defaults);
		return body;
	}

	private CategoryPage buildPage(FlowLayout body) {
		ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(
				Sizing.fill(100), Sizing.fill(100), body);
		scroll.scrollbarThiccness(2);
		scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(ModernUiTheme.SCROLLBAR)));
		StackLayout stack = Containers.stack(Sizing.fill(100), Sizing.fill(100));
		stack.allowOverflow(false);
		stack.child(scroll);
		return new CategoryPage(stack, scroll);
	}

	private FlowLayout previewStateRow() {
		FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
		row.gap(6);
		openPreviewButton = ModernUiTheme.button(Text.empty(), button -> {
			previewStateChanged.accept(PreviewChatState.OPEN);
			syncPreviewButtons();
		});
		closedPreviewButton = ModernUiTheme.button(Text.empty(), button -> {
			previewStateChanged.accept(PreviewChatState.CLOSED);
			syncPreviewButtons();
		});
		openPreviewButton.sizing(Sizing.fill(50), Sizing.fixed(22));
		closedPreviewButton.sizing(Sizing.fill(50), Sizing.fixed(22));
		registerPageButton(Category.LAYOUT, openPreviewButton);
		registerPageButton(Category.LAYOUT, closedPreviewButton);
		row.child(openPreviewButton);
		row.child(closedPreviewButton);
		syncPreviewButtons();
		return row;
	}

	private StackLayout alignmentSelector() {
		StackLayout stack = Containers.stack(Sizing.fill(100), Sizing.fixed(24));
		stack.child(new SelectionIndicatorComponent(
				() -> session.text().alignment().ordinal(), ChatTextAlignment.values().length));
		FlowLayout buttons = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100));
		for (ChatTextAlignment alignment : ChatTextAlignment.values()) {
			ButtonComponent button = transparentButton(
					Text.translatable(switch (alignment) {
						case LEFT -> "chat_canvas.alignment.left";
						case CENTER -> "chat_canvas.alignment.center";
						case RIGHT -> "chat_canvas.alignment.right";
					}),
					clicked -> selectAlignment(alignment));
			button.sizing(Sizing.fill(33), Sizing.fill(100));
			registerPageButton(Category.TEXT, button);
			buttons.child(button);
		}
		stack.child(buttons);
		return stack;
	}

	private StackLayout messageModeSelector() {
		StackLayout stack = Containers.stack(Sizing.fill(100), Sizing.fixed(24));
		stack.child(new SelectionIndicatorComponent(
				() -> session.background().messageMode().ordinal(),
				MessageBackgroundMode.values().length));
		FlowLayout buttons = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100));
		for (MessageBackgroundMode mode : MessageBackgroundMode.values()) {
			ButtonComponent button = transparentButton(
					Text.translatable(switch (mode) {
						case FOLLOW_TEXT -> "chat_canvas.background.mode.follow_text";
						case FULL_WIDTH -> "chat_canvas.background.mode.full_width";
						case HIDDEN -> "chat_canvas.background.mode.hidden";
					}),
					clicked -> selectMessageMode(mode));
			button.sizing(Sizing.fill(33), Sizing.fill(100));
			registerPageButton(Category.BACKGROUND, button);
			buttons.child(button);
		}
		stack.child(buttons);
		return stack;
	}

	private void selectAlignment(ChatTextAlignment alignment) {
		ChatTextConfig before = session.text();
		if (before.alignment() == alignment) return;
		session.setText(new ChatTextConfig(
				before.fontScale(), before.lineSpacing(), before.textOpacity(),
				alignment, before.shadow(), before.characterSpacing()));
		session.commit();
		geometryChanged.run();
		committed.run();
	}

	private void selectMessageMode(MessageBackgroundMode mode) {
		ChatBackgroundConfig before = session.background();
		if (before.messageMode() == mode) return;
		session.setBackground(before.withMessageMode(mode));
		session.commit();
		geometryChanged.run();
		committed.run();
	}

	private NumericScrubberComponent layoutScrubber(NumericScrubberComponent.Property property,
													 String translationKey) {
		NumericScrubberComponent scrubber = new NumericScrubberComponent(
				session,
				property,
				settingLabel(translationKey),
				screenWidth,
				screenHeight,
				geometryChanged,
				committed
		);
		registerScrubber(Category.LAYOUT, scrubber);
		return scrubber;
	}

	private TextNumericScrubberComponent textScrubber(TextNumericScrubberComponent.Property property,
													  String translationKey) {
		TextNumericScrubberComponent scrubber = new TextNumericScrubberComponent(
				session,
				property,
				settingLabel(translationKey),
				geometryChanged,
				committed
		);
		registerScrubber(Category.TEXT, scrubber);
		return scrubber;
	}

	private BackgroundNumericScrubberComponent backgroundScrubber(
			BackgroundNumericScrubberComponent.Property property,
			String translationKey) {
		BackgroundNumericScrubberComponent scrubber = new BackgroundNumericScrubberComponent(
				session,
				property,
				settingLabel(translationKey),
				geometryChanged,
				committed
		);
		registerScrubber(Category.BACKGROUND, scrubber);
		return scrubber;
	}

	private ButtonComponent colorButton(ColorTarget target, String translationKey, int defaultColor) {
		ButtonComponent button = ModernUiTheme.button(Text.empty(), clicked -> {
			int initialColor = target.read(session.background());
			colorPickerLauncher.open(clicked, new ModernColorPickerPopup.Request(
					initialColor,
					defaultColor,
					session.recentColors().colors(),
					color -> {
						session.setBackground(target.write(session.background(), color));
						geometryChanged.run();
						syncFromSession();
					},
					color -> {
						session.recentColors().add(color);
						session.commit();
						committed.run();
						syncFromSession();
					},
					this::syncFromSession
			));
		});
		button.sizing(Sizing.fill(100), Sizing.fixed(22));
		button.renderer((context, component, delta) -> {
			int background = component.active()
					? component.isHovered() ? ModernUiTheme.CONTROL_HOVER : ModernUiTheme.CONTROL_BACKGROUND
					: ModernUiTheme.CONTROL_DISABLED;
			ModernUiTheme.roundedRect(context, component.getX(), component.getY(),
					component.getWidth(), component.getHeight(), 2, background);
			ModernUiTheme.border(context, component.getX(), component.getY(),
					component.getWidth(), component.getHeight(), ModernUiTheme.PANEL_BORDER);
			int color = target.read(session.background());
			ModernUiTheme.roundedRect(context, component.getX() + 5, component.getY() + 4,
					14, component.getHeight() - 8, 1, 0xFF000000 | color);
			context.drawRectOutline(component.getX() + 5, component.getY() + 4,
					14, component.getHeight() - 8, ModernUiTheme.PANEL_BORDER);
		});
		button.id(translationKey);
		registerPageButton(Category.BACKGROUND, button);
		return button;
	}

	private void setPlayerColorMode(PlayerColorMode mode) {
		PlayerColorConfig before = session.playerColors();
		if (before.mode() == mode) return;
		session.setPlayerColors(before.withMode(mode));
		session.commit();
		geometryChanged.run();
		committed.run();
		syncFromSession();
	}

	private void openPaletteColorPicker(ButtonComponent anchor, int index) {
		PlayerColorConfig before = session.playerColors();
		int initial = before.palette().get(index);
		int defaultColor = PlayerColorConfig.DEFAULT_PALETTE.get(
				Math.min(index, PlayerColorConfig.DEFAULT_PALETTE.size() - 1));
		colorPickerLauncher.open(anchor, new ModernColorPickerPopup.Request(
				initial,
				defaultColor,
				session.recentColors().colors(),
				color -> {
					session.setPlayerColors(session.playerColors().withPaletteColor(index, color));
					lastPlayerColors = session.playerColors();
					geometryChanged.run();
				},
				color -> {
					session.recentColors().add(color);
					session.commit();
					lastPlayerColors = session.playerColors();
					committed.run();
					rebuildPlayerRows();
				},
				() -> {
					session.setPlayerColors(before);
					lastPlayerColors = before;
					geometryChanged.run();
					rebuildPlayerRows();
				}
		));
	}

	private void rebuildPlayerRows() {
		if (playerListBody == null) return;
		playerListBody.clearChildren();
		if (PlayerRosterTracker.usingPreviewPlayers()) {
			playerListBody.child(Components.label(
					Text.translatable("chat_canvas.player_colors.offline_hint")
							.formatted(Formatting.GRAY)));
		}
		String query = playerSearch.trim().toLowerCase(Locale.ROOT);
		for (PlayerChatIdentity player : PlayerRosterTracker.editorPlayers()) {
			if (!query.isEmpty()
					&& !player.playerName().toLowerCase(Locale.ROOT).contains(query)) {
				continue;
			}
			playerListBody.child(playerRow(player));
		}
		rosterRevision = PlayerRosterTracker.revision();
		lastPlayerColors = session.playerColors();
	}

	private FlowLayout playerRow(PlayerChatIdentity player) {
		FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
		row.gap(4);
		row.verticalAlignment(VerticalAlignment.CENTER);
		ButtonComponent color = ModernUiTheme.button(Text.empty(),
				button -> openPlayerColorPicker(button, player));
		color.sizing(Sizing.fixed(24), Sizing.fixed(20));
		color.renderer((context, component, delta) -> {
			playerColorProvider.updateConfig(session.playerColors());
			int rgb = playerColorProvider.colorFor(player).orElse(0xFFFFFF);
			ModernUiTheme.roundedRect(context, component.getX(), component.getY(),
					component.getWidth(), component.getHeight(), 1, 0xFF000000 | rgb);
			ModernUiTheme.border(context, component.getX(), component.getY(),
					component.getWidth(), component.getHeight(), ModernUiTheme.PANEL_BORDER);
		});
		color.mouseDown().subscribe((click, doubled) -> {
			if (click.button() != 1) return false;
			restorePlayerAutomatic(player);
			return true;
		});
		row.child(color);

		var name = Components.label(Text.literal(player.playerName()).formatted(Formatting.WHITE));
		name.horizontalSizing(Sizing.fixed(112));
		row.child(name);
		boolean custom = session.playerColors().hasOverride(player.uuid(), player.playerName());
		var state = Components.label(Text.translatable(custom
				? "chat_canvas.player_colors.custom"
				: "chat_canvas.player_colors.automatic").formatted(
				custom ? Formatting.GOLD : Formatting.GRAY));
		state.horizontalSizing(Sizing.fixed(46));
		row.child(state);

		ButtonComponent reset = ModernUiTheme.button(
				Text.literal("↺"),
				button -> restorePlayerAutomatic(player));
		reset.tooltip(Text.translatable("chat_canvas.player_colors.restore_automatic"));
		reset.sizing(Sizing.fixed(24), Sizing.fixed(20));
		reset.active(custom);
		row.child(reset);
		return row;
	}

	private void openPlayerColorPicker(ButtonComponent anchor, PlayerChatIdentity player) {
		PlayerColorConfig before = session.playerColors();
		playerColorProvider.updateConfig(before);
		int initial = playerColorProvider.colorFor(player).orElse(0xFFFFFF);
		colorPickerLauncher.open(anchor, new ModernColorPickerPopup.Request(
				initial,
				initial,
				session.recentColors().colors(),
				color -> {
					session.setPlayerColors(session.playerColors()
							.withUuidOverride(player.uuid(), color));
					lastPlayerColors = session.playerColors();
					geometryChanged.run();
				},
				color -> {
					session.recentColors().add(color);
					session.commit();
					lastPlayerColors = session.playerColors();
					committed.run();
					rebuildPlayerRows();
				},
				() -> {
					session.setPlayerColors(before);
					lastPlayerColors = before;
					geometryChanged.run();
					rebuildPlayerRows();
				}
		));
	}

	private void restorePlayerAutomatic(PlayerChatIdentity player) {
		PlayerColorConfig before = session.playerColors();
		PlayerColorConfig after = before.withoutOverrides(player.uuid(), player.playerName());
		if (before.equals(after)) return;
		session.setPlayerColors(after);
		session.commit();
		geometryChanged.run();
		committed.run();
		rebuildPlayerRows();
		syncFromSession();
	}

	public int activeCategoryOrdinal() {
		return activeCategory.ordinal();
	}

	private static Text settingLabel(String key) {
		if (ModernUiTheme.currentStyle() == EditorUiStyle.VANILLA) {
			return Text.translatable(key);
		}
		return Text.translatable(key).styled(style -> style.withColor(
				ModernUiTheme.TEXT_SECONDARY & 0xFFFFFF));
	}

	private void switchCategory(Category category) {
		if (category == activeCategory) return;
		Category previous = activeCategory;
		activeCategory = category;
		double target = category.ordinal() * pageWidth();
		categorySpring.setValue(target);
		categorySpring.setTarget(target);
		categoryTransitioning = false;
		updateCategoryTransition(0.0);
		setPageButtonsActive(true);
		if (pageHost != null) {
			pageHost.setActivePage(category.ordinal());
			pageHost.setTransitionPage(-1);
		}
	}

	public void syncFromSession() {
		syncPreviewButtons();
		syncPlayerLayoutButtons();
		if (shadowButton != null) {
			boolean shadow = session.text().shadow();
			shadowButton.setMessage(
					Text.translatable("chat_canvas.option.text_shadow")
							.append(Text.literal("  "))
							.append(Text.translatable(shadow
									? "chat_canvas.state.on"
									: "chat_canvas.state.off")));
		}
		syncBackgroundButtons();
		syncPlayerColorButtons();
		syncMentionButtons();
		syncCommandButtons();
		syncVoiceButtons();
		syncChatLogButtons();
		if (lastPlayerColors == null || !lastPlayerColors.equals(session.playerColors())
				|| rosterRevision != PlayerRosterTracker.revision()) {
			rebuildPlayerRows();
		}
	}

	private void syncPlayerLayoutButtons() {
		boolean player = session.selectedChannel()
				== io.github.ikunkk02.chatcanvas.editor.EditorChannel.PLAYER_CHAT;
		if (classicLayoutButton != null) {
			classicLayoutButton.setMessage(Text.literal(
					session.playerChatLayoutMode() == PlayerChatLayoutMode.CLASSIC
							? "● " : "○ ")
					.append(Text.translatable("chat_canvas.player_layout.classic")));
			classicLayoutButton.active(player && !categoryTransitioning);
		}
		if (splitLayoutButton != null) {
			splitLayoutButton.setMessage(Text.literal(
					session.playerChatLayoutMode() == PlayerChatLayoutMode.SPLIT_ALIGNMENT
							? "● " : "○ ")
					.append(Text.translatable("chat_canvas.player_layout.split")));
			splitLayoutButton.active(player && !categoryTransitioning);
		}
	}

	private void syncCommandButtons() {
		CommandClipboardConfig config = session.commandClipboard();
		setToggleMessage(commandEnabledButton, "chat_canvas.command.enabled", config.enabled());
		setToggleMessage(commandPanelButton, "chat_canvas.command.show_button",
				config.showPanelButton());
		setToggleMessage(commandSensitiveButton, "chat_canvas.command.sensitive_warning",
				config.sensitiveWarning());
		setToggleMessage(commandRecordRecentButton, "chat_canvas.command.record_recent",
				config.recordRecentCommands());
		setToggleMessage(commandClearRecentOnDisconnectButton,
				"chat_canvas.command.clear_recent_disconnect",
				config.clearRecentOnDisconnect());
		if (commandMaxRecentButton != null) {
			commandMaxRecentButton.setMessage(Text.translatable(
					"chat_canvas.command.max_recent")
					.append(Text.literal("  " + config.maxRecentCommands())));
		}
		if (commandInsertModeButton != null) {
			commandInsertModeButton.setMessage(
					Text.translatable("chat_canvas.command.insert_mode")
							.append(Text.literal("  "))
							.append(Text.translatable(config.insertMode()
									== CommandInsertMode.REPLACE_INPUT
									? "chat_canvas.command.insert_replace"
									: "chat_canvas.command.insert_cursor")));
		}
	}

	private void syncVoiceButtons() {
		VoiceInputManager manager = VoiceInputManager.instance();
		VoiceSettings settings = manager.settings();
		setToggleMessage(voiceEnabledButton, "chat_canvas.voice.enabled", settings.enabled());
		setToggleMessage(voiceLevelButton, "chat_canvas.voice.show_level",
				settings.showInputLevel());
		setToggleMessage(voicePartialButton, "chat_canvas.voice.show_partial",
				settings.showPartialResults());
		setToggleMessage(voicePunctuationButton, "chat_canvas.voice.add_punctuation",
				settings.addFinalPunctuation());
		if (voiceDurationButton != null) voiceDurationButton.setMessage(
				Text.translatable("chat_canvas.voice.maximum_seconds")
						.append(Text.literal("  " + settings.maximumSeconds())));
		if (voiceThresholdButton != null) voiceThresholdButton.setMessage(
				Text.translatable("chat_canvas.voice.noise_threshold")
						.append(Text.literal(String.format(Locale.ROOT, "  %.3f",
								settings.noiseThreshold()))));
		if (voiceNoSpeechButton != null) voiceNoSpeechButton.setMessage(
				Text.translatable("chat_canvas.voice.no_speech_timeout")
						.append(Text.literal("  " + settings.noSpeechTimeoutMillis() + " ms")));
		if (voiceEndpointButton != null) voiceEndpointButton.setMessage(
				Text.translatable("chat_canvas.voice.endpoint_silence")
						.append(Text.literal("  " + settings.endpointSilenceMillis() + " ms")));
		if (voiceTailButton != null) voiceTailButton.setMessage(
				Text.translatable("chat_canvas.voice.tail_padding")
						.append(Text.literal("  " + settings.tailPaddingMillis() + " ms")));
		if (voiceThreadsButton != null) {
			String configured = settings.inferenceThreads() == 0
					? Text.translatable("chat_canvas.voice.threads.auto").getString()
					: Integer.toString(settings.inferenceThreads());
			voiceThreadsButton.setMessage(Text.translatable("chat_canvas.voice.inference_threads")
					.append(Text.literal("  " + configured + " (" + manager.effectiveInferenceThreads() + ")")));
		}
		if (voiceDeviceButton != null) {
			String name = manager.devices().stream()
					.filter(device -> device.id().equals(settings.microphoneId()))
					.map(device -> device.displayName()).findFirst()
					.orElse(Text.translatable("chat_canvas.voice.device.default").getString());
			voiceDeviceButton.setMessage(Text.translatable("chat_canvas.voice.device")
					.append(Text.literal("  " + name)));
		}
		if (voiceTestButton != null) {
			String suffix = manager.isMicrophoneTesting()
					? String.format(Locale.ROOT, "  %.0f%%",
							Math.min(100.0, manager.microphoneTestLevel() * 800.0))
					: "";
			voiceTestButton.setMessage(Text.translatable(
					manager.isMicrophoneTesting()
							? "chat_canvas.voice.test.stop"
							: "chat_canvas.voice.test.start").append(Text.literal(suffix)));
		}
		VoiceModelDescriptor selectedModel = manager.selectedModel();
		if (voiceModelSelectButton != null) {
			Text modelName = selectedModel == null ? Text.translatable("chat_canvas.voice.model.none")
					: Text.translatable(selectedModel.displayNameKey());
			voiceModelSelectButton.setMessage(Text.translatable("chat_canvas.voice.current_model")
					.append(Text.literal("  ")).append(modelName));
		}
		if (voiceModelButton != null) {
			boolean installed = selectedModel != null && manager.isModelInstalled(selectedModel.id());
			String key = manager.state() == VoiceInputState.MODEL_DOWNLOADING
					|| manager.state() == VoiceInputState.MODEL_VERIFYING
					|| manager.state() == VoiceInputState.MODEL_INSTALLING
					? "chat_canvas.voice.model.cancel_download"
					: installed ? "chat_canvas.voice.model.installed" : "chat_canvas.voice.model.download";
			voiceModelButton.setMessage(Text.translatable(key)
					.append(Text.literal("  " + manager.state().name())));
		}
	}

	private void syncMentionButtons() {
		MentionConfig config = session.mention();
		setToggleMessage(mentionDoubleClickButton, "chat_canvas.mention.double_click",
				config.doubleClickEnabled());
		setToggleMessage(mentionHighlightButton, "chat_canvas.mention.highlight",
				config.highlightEnabled());
		setToggleMessage(mentionBoldButton, "chat_canvas.mention.highlight_bold",
				config.highlightBold());
		setToggleMessage(mentionRequireAtButton, "chat_canvas.mention.require_at",
				config.requireAtSymbol());
		setToggleMessage(mentionSoundEnabledButton, "chat_canvas.mention.sound_enabled",
				config.soundEnabled());
		setToggleMessage(mentionToastEnabledButton, "chat_canvas.mention.toast_enabled",
				config.toastEnabled());
		setToggleMessage(mentionToastWhenOpenButton, "chat_canvas.mention.toast_when_open",
				config.toastWhenChatOpen());
		setToggleMessage(mentionFlashEnabledButton, "chat_canvas.mention.flash_enabled",
				config.flashEnabled());
		setToggleMessage(mentionIgnoreOwnButton, "chat_canvas.mention.ignore_own",
				config.ignoreOwnMessages());
		setToggleMessage(mentionQuickActionsButton, "chat_canvas.mention.quick_actions",
				config.playerQuickActionsEnabled());
		if (mentionSoundTypeButton != null) {
			mentionSoundTypeButton.setMessage(Text.translatable("chat_canvas.mention.sound_type")
					.append(Text.literal("  "))
					.append(Text.translatable("chat_canvas.mention.sound."
							+ config.sound().name().toLowerCase(Locale.ROOT))));
		}
		if (mentionColorButton != null) {
			mentionColorButton.setMessage(colorButtonText(
					"chat_canvas.mention.highlight_color", config.highlightColor()));
		}
		if (mentionFlashColorButton != null) {
			mentionFlashColorButton.setMessage(colorButtonText(
					"chat_canvas.mention.flash_color", config.flashColor()));
		}
	}

	private static void setToggleMessage(
			ButtonComponent button, String translationKey, boolean enabled) {
		if (button == null) return;
		button.setMessage(Text.translatable(translationKey)
				.append(Text.literal("  "))
				.append(Text.translatable(enabled
						? "chat_canvas.state.on"
						: "chat_canvas.state.off")));
	}

	private void syncPlayerColorButtons() {
		PlayerColorConfig config = session.playerColors();
		if (playerColorsEnabledButton != null) {
			playerColorsEnabledButton.setMessage(
					Text.translatable("chat_canvas.player_colors.enabled")
							.append(Text.literal("  "))
							.append(Text.translatable(config.enabled()
									? "chat_canvas.state.on"
									: "chat_canvas.state.off")));
		}
		if (playerAutomaticButton != null) {
			playerAutomaticButton.setMessage(Text.literal(
					config.mode() == PlayerColorMode.AUTOMATIC ? "● " : "○ ")
					.append(Text.translatable("chat_canvas.player_colors.automatic")));
		}
		if (playerVanillaButton != null) {
			playerVanillaButton.setMessage(Text.literal(
					config.mode() == PlayerColorMode.VANILLA ? "● " : "○ ")
					.append(Text.translatable("chat_canvas.player_colors.vanilla")));
		}
		if (hitboxDebugButton != null) {
			hitboxDebugButton.setMessage(
					Text.translatable("chat_canvas.player_colors.show_hitboxes")
							.append(Text.literal("  "))
							.append(Text.translatable(config.showNameHitboxes()
									? "chat_canvas.state.on"
									: "chat_canvas.state.off")));
		}
	}

	private void syncBackgroundButtons() {
		if (messageColorButton != null) {
			messageColorButton.setMessage(colorButtonText(
					"chat_canvas.background.color", session.background().messageColor()));
		}
		if (inputColorButton != null) {
			inputColorButton.setMessage(colorButtonText(
					"chat_canvas.background.input_color", session.background().inputColor()));
		}
		if (borderColorButton != null) {
			borderColorButton.setMessage(colorButtonText(
					"chat_canvas.background.border_color", session.background().inputBorderColor()));
		}
		if (inputBorderButton != null) {
			inputBorderButton.setMessage(
					Text.translatable("chat_canvas.background.input_border")
							.append(Text.literal("  "))
							.append(Text.translatable(session.background().inputBorderEnabled()
									? "chat_canvas.state.on"
									: "chat_canvas.state.off")));
		}
	}

	private static Text colorButtonText(String key, int color) {
		return Text.translatable(key)
				.append(Text.literal("  " + String.format(java.util.Locale.ROOT, "#%06X", color)));
	}

	private void syncPreviewButtons() {
		if (openPreviewButton == null || closedPreviewButton == null) return;
		boolean open = previewState.get() == PreviewChatState.OPEN;
		openPreviewButton.setMessage(Text.literal(open ? "● " : "○ ")
				.append(Text.translatable("chat_canvas.preview.state.open")));
		closedPreviewButton.setMessage(Text.literal(open ? "○ " : "● ")
				.append(Text.translatable("chat_canvas.preview.state.closed")));
	}

	public void update(double deltaSeconds) {
		updatePanelSide(deltaSeconds);
		updateCategoryTransition(deltaSeconds);
		if (rosterRevision != PlayerRosterTracker.revision()) {
			rebuildPlayerRows();
		}
	}

	private void updatePanelSide(double deltaSeconds) {
		double center = session.layout().centerX();
		if (side == Side.RIGHT && center > screenWidth * 0.55) {
			side = Side.LEFT;
			spring.setTarget(targetX());
		} else if (side == Side.LEFT && center < screenWidth * 0.45) {
			side = Side.RIGHT;
			spring.setTarget(targetX());
		}
		int maxX = Math.max(4, screenWidth - panelWidth - 4);
		int x = clamp((int) Math.round(spring.update(deltaSeconds)), 4, maxX);
		component.moveTo(x, Math.min(editorMetrics.top(),
				Math.max(4, screenHeight - panelHeight - 4)));
	}

	private void updateCategoryTransition(double deltaSeconds) {
		double position = categorySpring.update(deltaSeconds);
		int pageOffset = (int) Math.round(position);
		for (Category category : Category.values()) {
			pages.get(category).stack.positioning(
					Positioning.absolute(category.ordinal() * pageWidth() - pageOffset, 0));
		}
		if (categoryTransitioning && !categorySpring.settled()) {
			int targetOrdinal = (int) Math.round(categorySpring.target() / pageWidth());
			if (pageHost != null) pageHost.setTransitionPage(targetOrdinal);
		} else if (categoryTransitioning && categorySpring.settled()) {
			categoryTransitioning = false;
			setPageButtonsActive(true);
			if (pageHost != null) pageHost.setTransitionPage(-1);
		} else if (pageHost != null) {
			pageHost.setTransitionPage(-1);
		}
	}

	private void setPageButtonsActive(boolean active) {
		for (Map.Entry<Category, List<ButtonComponent>> entry : pageButtons.entrySet()) {
			boolean pageActive = active && entry.getKey() == activeCategory;
			for (ButtonComponent button : entry.getValue()) {
				if (button != null) button.active(pageActive);
			}
		}
	}

	private void registerPageButton(Category category, ButtonComponent button) {
		pageButtons.get(category).add(button);
	}

	private void registerScrubber(Category category, NumericScrubber scrubber) {
		scrubbers.add(scrubber);
		pageScrubbers.get(category).add(scrubber);
	}

	/** Descriptions may wrap; setting names, section titles and tabs never do. */
	private LabelComponent descriptionLabel(String key) {
		LabelComponent label = Components.label(
				Text.translatable(key).formatted(Formatting.GRAY));
		label.horizontalSizing(Sizing.fill(100));
		label.verticalSizing(Sizing.content());
		label.maxWidth(pageWidth());
		descriptionLabels.add(label);
		return label;
	}

	public void resizeViewport(int width, int height) {
		double previousPageWidth = pageWidth();
		double pageProgress = previousPageWidth <= 0.0
				? activeCategory.ordinal()
				: categorySpring.value() / previousPageWidth;
		this.screenWidth = width;
		this.screenHeight = height;
		this.editorMetrics = UiLayoutMetrics.editorPanel(width, height);
		this.panelWidth = editorMetrics.width();
		this.panelHeight = editorMetrics.height();
		component.sizing(Sizing.fixed(panelWidth), Sizing.fixed(panelHeight));
		component.padding(Insets.of(editorMetrics.padding()));
		component.gap(editorMetrics.gap());
		if (pageHost != null) {
			pageHost.sizing(Sizing.fill(100), Sizing.fixed(contentHeight()));
			pageHost.setActivePage(activeCategory.ordinal());
			pageHost.setTransitionPage(-1);
		}
		categorySpring.setValue(pageProgress * pageWidth());
		categorySpring.setTarget(activeCategory.ordinal() * pageWidth());
		categoryTransitioning = !categorySpring.settled();
		setPageButtonsActive(!categoryTransitioning);
		spring.setTarget(targetX());
		int maxX = Math.max(4, width - panelWidth - 4);
		if (spring.value() < 4 || spring.value() > maxX) {
			spring.setValue(clamp((int) Math.round(spring.value()), 4, maxX));
			spring.setTarget(targetX());
		}
		for (NumericScrubber scrubber : scrubbers) {
			scrubber.resizeViewport(width, height);
		}
		for (LabelComponent description : descriptionLabels) {
			description.maxWidth(pageWidth());
		}
		updateCategoryTransition(0.0);
	}

	public @Nullable NumericScrubber scrubberAt(double mouseX, double mouseY) {
		int x = (int) Math.floor(mouseX);
		int y = (int) Math.floor(mouseY);
		if (categoryTransitioning || pageHost == null || !pageHost.isInBoundingBox(x, y)) return null;
		for (NumericScrubber scrubber : pageScrubbers.get(activeCategory)) {
			if (scrubber.valueRegionContains(mouseX, mouseY)) return scrubber;
		}
		return null;
	}

	private double targetX() {
		if (side == Side.LEFT) {
			return Math.min(PANEL_MARGIN, Math.max(4, screenWidth - panelWidth - 4));
		}
		return Math.max(4, screenWidth - panelWidth - PANEL_MARGIN);
	}

	private int contentHeight() {
		return editorMetrics.contentHeight(LABEL_HEIGHT, CATEGORY_HEIGHT);
	}

	private int pageWidth() {
		return Math.max(1, panelWidth - editorMetrics.padding() * 2);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static io.wispforest.owo.ui.component.LabelComponent sectionLabel(String key) {
		return Components.label(Text.translatable(key).formatted(Formatting.WHITE, Formatting.BOLD));
	}

	private static ButtonComponent transparentButton(Text text, Consumer<ButtonComponent> action) {
		ButtonComponent button = ModernUiTheme.button(text, action);
		button.renderer(ButtonComponent.Renderer.flat(
				0x00000000, 0x333A3A3A, 0x00000000));
		return button;
	}

	public FlowLayout component() {
		return component;
	}

	private enum Side {
		LEFT, RIGHT
	}

	private enum Category {
		LAYOUT("chat_canvas.category.layout", "chat_canvas.tab.layout"),
		TEXT("chat_canvas.category.text", "chat_canvas.tab.text"),
		BACKGROUND("chat_canvas.category.background", "chat_canvas.tab.background"),
		PLAYER_COLORS("chat_canvas.category.player_colors", "chat_canvas.tab.player_colors"),
		MENTION("chat_canvas.category.mention", "chat_canvas.tab.mention"),
		COMMAND("chat_canvas.category.command", "chat_canvas.tab.command"),
		VOICE("chat_canvas.category.voice", "chat_canvas.tab.voice"),
		CHAT_LOG("chat_canvas.category.chat_log", "chat_canvas.tab.chat_log");

		private final String translationKey;
		private final String tabTranslationKey;

		Category(String translationKey, String tabTranslationKey) {
			this.translationKey = translationKey;
			this.tabTranslationKey = tabTranslationKey;
		}
	}

	@FunctionalInterface
	public interface ColorPickerLauncher {
		void open(ButtonComponent anchor, ModernColorPickerPopup.Request request);
	}

	private enum ColorTarget {
		MESSAGE {
			@Override
			int read(ChatBackgroundConfig config) {
				return config.messageColor();
			}

			@Override
			ChatBackgroundConfig write(ChatBackgroundConfig config, int color) {
				return config.withMessageColor(color);
			}
		},
		INPUT {
			@Override
			int read(ChatBackgroundConfig config) {
				return config.inputColor();
			}

			@Override
			ChatBackgroundConfig write(ChatBackgroundConfig config, int color) {
				return config.withInputColor(color);
			}
		},
		BORDER {
			@Override
			int read(ChatBackgroundConfig config) {
				return config.inputBorderColor();
			}

			@Override
			ChatBackgroundConfig write(ChatBackgroundConfig config, int color) {
				return config.withInputBorderColor(color);
			}
		};

		abstract int read(ChatBackgroundConfig config);

		abstract ChatBackgroundConfig write(ChatBackgroundConfig config, int color);
	}

	private static final class CategoryPage {
		private final StackLayout stack;
		@SuppressWarnings("unused")
		private final ScrollContainer<FlowLayout> scroll;

		private CategoryPage(StackLayout stack, ScrollContainer<FlowLayout> scroll) {
			this.stack = stack;
			this.scroll = scroll;
		}
	}
}
