package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.animation.MotionPreset;
import io.github.ikunkk02.chatcanvas.animation.SpringValue;
import io.github.ikunkk02.chatcanvas.chat.render.PreviewChatState;
import io.github.ikunkk02.chatcanvas.editor.EditorSession;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AnimatedSettingsPanel {
	private static final int PANEL_MARGIN = 16;
	private static final int PANEL_TOP = 48;

	private final EditorSession session;
	private final Runnable geometryChanged;
	private final Runnable committed;
	private final Runnable saveAction;
	private final Runnable cancelAction;
	private final Supplier<PreviewChatState> previewState;
	private final Consumer<PreviewChatState> previewStateChanged;
	private final FlowLayout component;
	private final Map<NumericScrubberComponent.Property, NumericScrubberComponent> scrubbers =
			new EnumMap<>(NumericScrubberComponent.Property.class);

	private ButtonComponent openPreviewButton;
	private ButtonComponent closedPreviewButton;
	private SpringValue spring;
	private Side side;
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
		double initialX = targetX();
		this.spring = new SpringValue(initialX, MotionPreset.PANEL_SLIDE);
		this.component = buildComponent();
		this.component.positioning(Positioning.absolute((int) Math.round(initialX), PANEL_TOP));
		this.component.zIndex(20);
		syncFromSession();
	}

	private FlowLayout buildComponent() {
		FlowLayout panel = Containers.verticalFlow(Sizing.fixed(panelWidth), Sizing.fixed(panelHeight));
		panel.padding(Insets.of(12));
		panel.gap(8);
		panel.surface(ModernUiTheme.PANEL_SURFACE);

		panel.child(Components.label(Text.translatable("chat_canvas.settings.title")
				.formatted(Formatting.WHITE, Formatting.BOLD)));
		panel.child(Components.label(Text.translatable("chat_canvas.settings.subtitle")
				.formatted(Formatting.GRAY)));

		FlowLayout body = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
		body.gap(7);
		body.child(sectionLabel("chat_canvas.category.layout"));
		body.child(Components.label(Text.translatable("chat_canvas.preview.state")
				.formatted(Formatting.GRAY)));
		body.child(previewStateRow());
		body.child(scrubber(NumericScrubberComponent.Property.X, "chat_canvas.option.x"));
		body.child(scrubber(NumericScrubberComponent.Property.Y, "chat_canvas.option.y"));
		body.child(scrubber(NumericScrubberComponent.Property.WIDTH, "chat_canvas.option.width"));
		body.child(scrubber(NumericScrubberComponent.Property.HEIGHT, "chat_canvas.option.height"));

		ButtonComponent defaults = ModernUiTheme.button(
				Text.translatable("chat_canvas.action.restore_defaults"),
				button -> {
					session.restoreDefaults();
					syncFromSession();
					geometryChanged.run();
					committed.run();
				});
		defaults.sizing(Sizing.fill(100), Sizing.fixed(22));
		body.child(defaults);

		body.child(sectionLabel("chat_canvas.settings.coming_soon"));
		for (String key : new String[]{
				"chat_canvas.category.text",
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

		ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(
				Sizing.fill(100), Sizing.fill(74), body);
		scroll.scrollbarThiccness(2);
		panel.child(scroll);

		FlowLayout actions = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
		actions.gap(6);
		actions.horizontalAlignment(HorizontalAlignment.RIGHT);
		actions.verticalAlignment(VerticalAlignment.CENTER);
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
		row.child(openPreviewButton);
		row.child(closedPreviewButton);
		syncPreviewButtons();
		return row;
	}

	private void syncPreviewButtons() {
		if (openPreviewButton == null || closedPreviewButton == null) return;
		boolean open = previewState.get() == PreviewChatState.OPEN;
		openPreviewButton.setMessage(Text.literal(open ? "● " : "○ ")
				.append(Text.translatable("chat_canvas.preview.state.open")));
		closedPreviewButton.setMessage(Text.literal(open ? "○ " : "● ")
				.append(Text.translatable("chat_canvas.preview.state.closed")));
	}

	private NumericScrubberComponent scrubber(NumericScrubberComponent.Property property, String translationKey) {
		NumericScrubberComponent scrubber = new NumericScrubberComponent(
				session,
				property,
				Text.translatable(translationKey).formatted(Formatting.LIGHT_PURPLE),
				screenWidth,
				screenHeight,
				geometryChanged,
				committed
		);
		scrubbers.put(property, scrubber);
		return scrubber;
	}

	public void syncFromSession() {
		// Scrubbers read their value directly from the session while drawing.
	}

	public void update(double deltaSeconds) {
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

	public void resizeViewport(int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;
		this.panelWidth = panelWidth(width);
		this.panelHeight = panelHeight(height);
		component.sizing(Sizing.fixed(panelWidth), Sizing.fixed(panelHeight));
		spring.setTarget(targetX());
		int maxX = Math.max(4, width - panelWidth - 4);
		if (spring.value() < 4 || spring.value() > maxX) {
			spring.setValue(clamp((int) Math.round(spring.value()), 4, maxX));
			spring.setTarget(targetX());
		}
		for (NumericScrubberComponent scrubber : scrubbers.values()) {
			scrubber.resizeViewport(width, height);
		}
	}

	public @Nullable NumericScrubberComponent scrubberAt(double mouseX, double mouseY) {
		for (NumericScrubberComponent scrubber : scrubbers.values()) {
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

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static io.wispforest.owo.ui.component.LabelComponent sectionLabel(String key) {
		return Components.label(Text.translatable(key).formatted(Formatting.WHITE, Formatting.BOLD));
	}

	public FlowLayout component() {
		return component;
	}

	private enum Side {
		LEFT, RIGHT
	}
}
