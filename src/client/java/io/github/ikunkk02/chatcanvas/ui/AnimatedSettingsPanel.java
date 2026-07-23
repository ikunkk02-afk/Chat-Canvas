package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.animation.MotionPreset;
import io.github.ikunkk02.chatcanvas.animation.SpringValue;
import io.github.ikunkk02.chatcanvas.chat.render.PreviewChatState;
import io.github.ikunkk02.chatcanvas.config.ChatTextAlignment;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import io.github.ikunkk02.chatcanvas.config.MessageBackgroundMode;
import io.github.ikunkk02.chatcanvas.editor.EditorSession;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AnimatedSettingsPanel {
	private static final int PANEL_MARGIN = 16;
	private static final int PANEL_TOP = 48;
	private static final int PANEL_PADDING = 12;
	private static final int PANEL_GAP = 8;
	private static final int LABEL_HEIGHT = 9;
	private static final int CATEGORY_HEIGHT = 24;
	private static final int FOOTER_HEIGHT = 30;

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
	private final Map<Category, List<ButtonComponent>> pageButtons = new EnumMap<>(Category.class);
	private final Map<Category, CategoryPage> pages = new EnumMap<>(Category.class);
	private final SpringValue categorySpring;

	private ButtonComponent openPreviewButton;
	private ButtonComponent closedPreviewButton;
	private ButtonComponent shadowButton;
	private ButtonComponent messageColorButton;
	private ButtonComponent inputColorButton;
	private ButtonComponent borderColorButton;
	private ButtonComponent inputBorderButton;
	private StackLayout pageHost;
	private SpringValue spring;
	private Side side;
	private Category activeCategory = Category.LAYOUT;
	private boolean categoryTransitioning;
	private int screenWidth;
	private int screenHeight;
	private int panelWidth;
	private int panelHeight;

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
		this.panelWidth = panelWidth(screenWidth);
		this.panelHeight = panelHeight(screenHeight);
		this.side = session.layout().centerX() > screenWidth * 0.5 ? Side.LEFT : Side.RIGHT;
		for (Category category : Category.values()) {
			pageButtons.put(category, new ArrayList<>());
		}
		double initialX = targetX();
		this.spring = new SpringValue(initialX, MotionPreset.PANEL_SLIDE);
		this.categorySpring = new SpringValue(0.0, MotionPreset.CATEGORY_SLIDE);
		this.component = buildComponent();
		this.component.positioning(Positioning.absolute((int) Math.round(initialX), PANEL_TOP));
		this.component.zIndex(20);
		setPageButtonsActive(true);
		syncFromSession();
	}

	private FlowLayout buildComponent() {
		FlowLayout panel = Containers.verticalFlow(Sizing.fixed(panelWidth), Sizing.fixed(panelHeight));
		panel.padding(Insets.of(PANEL_PADDING));
		panel.gap(PANEL_GAP);
		panel.surface(ModernUiTheme.PANEL_SURFACE);

		panel.child(Components.label(Text.translatable("chat_canvas.settings.title")
				.formatted(Formatting.WHITE, Formatting.BOLD)));
		panel.child(Components.label(Text.translatable("chat_canvas.settings.subtitle")
				.formatted(Formatting.GRAY)));
		panel.child(categoryTabs());

		pageHost = Containers.stack(
				Sizing.fill(100),
				Sizing.fixed(contentHeight(panelHeight))
		);
		pageHost.allowOverflow(false);
		CategoryPage layoutPage = buildPage(buildLayoutBody());
		CategoryPage textPage = buildPage(buildTextBody());
		CategoryPage backgroundPage = buildPage(buildBackgroundBody());
		pages.put(Category.LAYOUT, layoutPage);
		pages.put(Category.TEXT, textPage);
		pages.put(Category.BACKGROUND, backgroundPage);
		layoutPage.stack.positioning(Positioning.absolute(0, 0));
		textPage.stack.positioning(Positioning.absolute(pageWidth(), 0));
		backgroundPage.stack.positioning(Positioning.absolute(pageWidth() * 2, 0));
		pageHost.child(layoutPage.stack);
		pageHost.child(textPage.stack);
		pageHost.child(backgroundPage.stack);
		panel.child(pageHost);

		FlowLayout actions = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(FOOTER_HEIGHT));
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
					0x554F6079
			);
			context.fill(
					footer.x(),
					footer.y() + 1,
					footer.x() + footer.width(),
					footer.y() + footer.height(),
					0x33191C26
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

	private StackLayout categoryTabs() {
		StackLayout stack = Containers.stack(Sizing.fill(100), Sizing.fixed(24));
		stack.child(SelectionIndicatorComponent.following(
				this::categoryPageProgress, Category.values().length));
		FlowLayout buttons = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100));
		for (Category category : Category.values()) {
			ButtonComponent button = transparentButton(
					Text.translatable(category.translationKey),
					clicked -> switchCategory(category));
			button.sizing(Sizing.fill(100 / Category.values().length), Sizing.fill(100));
			buttons.child(button);
		}
		stack.child(buttons);
		return stack;
	}

	private FlowLayout buildLayoutBody() {
		FlowLayout body = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		body.padding(Insets.bottom(8));
		body.gap(7);
		body.child(sectionLabel("chat_canvas.category.layout"));
		body.child(Components.label(Text.translatable("chat_canvas.preview.state")
				.formatted(Formatting.GRAY)));
		body.child(previewStateRow());
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

		body.child(sectionLabel("chat_canvas.settings.coming_soon"));
		for (String key : new String[]{
				"chat_canvas.category.player_colors",
				"chat_canvas.category.mention",
				"chat_canvas.category.fade",
				"chat_canvas.category.command",
				"chat_canvas.category.compatibility"
		}) {
			ButtonComponent disabled = ModernUiTheme.button(
					Text.translatable(key).append(Text.literal(" · "))
							.append(Text.translatable("chat_canvas.status.not_implemented")),
					button -> {
					});
			disabled.active(false);
			disabled.sizing(Sizing.fill(100), Sizing.fixed(20));
			body.child(disabled);
		}
		return body;
	}

	private FlowLayout buildBackgroundBody() {
		FlowLayout body = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		body.padding(Insets.bottom(8));
		body.gap(7);

		body.child(sectionLabel("chat_canvas.background.message"));
		body.child(Components.label(Text.translatable("chat_canvas.background.mode")
				.formatted(Formatting.LIGHT_PURPLE)));
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
		body.child(Components.label(Text.translatable("chat_canvas.option.text_alignment")
				.formatted(Formatting.LIGHT_PURPLE)));
		body.child(alignmentSelector());

		shadowButton = ModernUiTheme.button(Text.empty(), button -> {
			ChatTextConfig before = session.text();
			session.setText(new ChatTextConfig(
					before.fontScale(), before.lineSpacing(), before.textOpacity(),
					before.alignment(), !before.shadow()));
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
				alignment, before.shadow()));
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
				Text.translatable(translationKey).formatted(Formatting.LIGHT_PURPLE),
				screenWidth,
				screenHeight,
				geometryChanged,
				committed
		);
		scrubbers.add(scrubber);
		return scrubber;
	}

	private TextNumericScrubberComponent textScrubber(TextNumericScrubberComponent.Property property,
													  String translationKey) {
		TextNumericScrubberComponent scrubber = new TextNumericScrubberComponent(
				session,
				property,
				Text.translatable(translationKey).formatted(Formatting.LIGHT_PURPLE),
				geometryChanged,
				committed
		);
		scrubbers.add(scrubber);
		return scrubber;
	}

	private BackgroundNumericScrubberComponent backgroundScrubber(
			BackgroundNumericScrubberComponent.Property property,
			String translationKey) {
		BackgroundNumericScrubberComponent scrubber = new BackgroundNumericScrubberComponent(
				session,
				property,
				Text.translatable(translationKey).formatted(Formatting.LIGHT_PURPLE),
				geometryChanged,
				committed
		);
		scrubbers.add(scrubber);
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
					? component.isHovered() ? 0xE04B5970 : 0xC8374256
					: 0x55343A48;
			ModernUiTheme.roundedRect(context, component.getX(), component.getY(),
					component.getWidth(), component.getHeight(), 5, background);
			ModernUiTheme.border(context, component.getX(), component.getY(),
					component.getWidth(), component.getHeight(), 0x554F6079);
			int color = target.read(session.background());
			ModernUiTheme.roundedRect(context, component.getX() + 5, component.getY() + 4,
					14, component.getHeight() - 8, 3, 0xFF000000 | color);
			context.drawRectOutline(component.getX() + 5, component.getY() + 4,
					14, component.getHeight() - 8, 0x997B899D);
		});
		button.id(translationKey);
		registerPageButton(Category.BACKGROUND, button);
		return button;
	}

	private void switchCategory(Category category) {
		if (category == activeCategory) return;
		activeCategory = category;
		categoryTransitioning = true;
		categorySpring.setTarget(category.ordinal() * pageWidth());
		setPageButtonsActive(false);
	}

	public void syncFromSession() {
		syncPreviewButtons();
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
		component.moveTo(x, Math.min(PANEL_TOP, Math.max(4, screenHeight - panelHeight - 4)));
	}

	private void updateCategoryTransition(double deltaSeconds) {
		double position = categorySpring.update(deltaSeconds);
		int pageOffset = (int) Math.round(position);
		for (Category category : Category.values()) {
			pages.get(category).stack.positioning(
					Positioning.absolute(category.ordinal() * pageWidth() - pageOffset, 0));
		}
		if (categoryTransitioning && categorySpring.settled()) {
			categoryTransitioning = false;
			setPageButtonsActive(true);
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

	public void resizeViewport(int width, int height) {
		double previousPageWidth = pageWidth();
		double pageProgress = previousPageWidth <= 0.0
				? activeCategory.ordinal()
				: categorySpring.value() / previousPageWidth;
		this.screenWidth = width;
		this.screenHeight = height;
		this.panelWidth = panelWidth(width);
		this.panelHeight = panelHeight(height);
		component.sizing(Sizing.fixed(panelWidth), Sizing.fixed(panelHeight));
		if (pageHost != null) {
			pageHost.sizing(Sizing.fill(100), Sizing.fixed(contentHeight(panelHeight)));
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
		updateCategoryTransition(0.0);
	}

	public @Nullable NumericScrubber scrubberAt(double mouseX, double mouseY) {
		if (categoryTransitioning) return null;
		for (NumericScrubber scrubber : scrubbers) {
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

	private static int panelWidth(int screenWidth) {
		return Math.min(300, Math.max(180, (int) Math.round(screenWidth * 0.42)));
	}

	private static int panelHeight(int screenHeight) {
		return Math.max(1, screenHeight - PANEL_TOP - 16);
	}

	private static int contentHeight(int panelHeight) {
		int fixedChildrenHeight = LABEL_HEIGHT * 2 + CATEGORY_HEIGHT + FOOTER_HEIGHT;
		int fixedGaps = PANEL_GAP * 4;
		int verticalPadding = PANEL_PADDING * 2;
		return Math.max(1, panelHeight - fixedChildrenHeight - fixedGaps - verticalPadding);
	}

	private int pageWidth() {
		return Math.max(1, panelWidth - PANEL_PADDING * 2);
	}

	private double categoryPageProgress() {
		return categorySpring.value() / pageWidth();
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static io.wispforest.owo.ui.component.LabelComponent sectionLabel(String key) {
		return Components.label(Text.translatable(key).formatted(Formatting.WHITE, Formatting.BOLD));
	}

	private static ButtonComponent transparentButton(Text text, Consumer<ButtonComponent> action) {
		ButtonComponent button = ModernUiTheme.button(text, action);
		button.renderer(ButtonComponent.Renderer.flat(0x00000000, 0x332F435A, 0x00000000));
		return button;
	}

	public FlowLayout component() {
		return component;
	}

	private enum Side {
		LEFT, RIGHT
	}

	private enum Category {
		LAYOUT("chat_canvas.category.layout"),
		TEXT("chat_canvas.category.text"),
		BACKGROUND("chat_canvas.category.background");

		private final String translationKey;

		Category(String translationKey) {
			this.translationKey = translationKey;
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
