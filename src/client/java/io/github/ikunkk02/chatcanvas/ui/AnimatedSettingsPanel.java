package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.animation.MotionPreset;
import io.github.ikunkk02.chatcanvas.animation.SpringValue;
import io.github.ikunkk02.chatcanvas.chat.render.PreviewChatState;
import io.github.ikunkk02.chatcanvas.config.ChatTextAlignment;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
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
	private final FlowLayout component;
	private final List<NumericScrubber> scrubbers = new ArrayList<>();
	private final Map<Category, List<ButtonComponent>> pageButtons = new EnumMap<>(Category.class);
	private final Map<Category, CategoryPage> pages = new EnumMap<>(Category.class);
	private final SpringValue categorySpring;

	private ButtonComponent openPreviewButton;
	private ButtonComponent closedPreviewButton;
	private ButtonComponent shadowButton;
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
								 Consumer<PreviewChatState> previewStateChanged) {
		this.session = session;
		this.geometryChanged = geometryChanged;
		this.committed = committed;
		this.saveAction = saveAction;
		this.cancelAction = cancelAction;
		this.previewState = previewState;
		this.previewStateChanged = previewStateChanged;
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
		pages.put(Category.LAYOUT, layoutPage);
		pages.put(Category.TEXT, textPage);
		layoutPage.stack.positioning(Positioning.absolute(0, 0));
		textPage.stack.positioning(Positioning.absolute(pageWidth(), 0));
		pageHost.child(layoutPage.stack);
		pageHost.child(textPage.stack);
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
			button.sizing(Sizing.fill(50), Sizing.fill(100));
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
				"chat_canvas.category.background",
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
		pages.get(Category.LAYOUT).stack.positioning(Positioning.absolute(-pageOffset, 0));
		pages.get(Category.TEXT).stack.positioning(
				Positioning.absolute(pageWidth() - pageOffset, 0));
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
		TEXT("chat_canvas.category.text");

		private final String translationKey;

		Category(String translationKey) {
			this.translationKey = translationKey;
		}
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
