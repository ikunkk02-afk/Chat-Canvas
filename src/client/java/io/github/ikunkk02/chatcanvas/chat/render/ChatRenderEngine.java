package io.github.ikunkk02.chatcanvas.chat.render;

import io.github.ikunkk02.chatcanvas.animation.AnimatedFloat;
import io.github.ikunkk02.chatcanvas.animation.AnimationClock;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLineMetrics;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatBackgroundBounds;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatBackgroundMetrics;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatTextLayout;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatVerticalMetrics;
import io.github.ikunkk02.chatcanvas.chat.layout.PlayerChatLayoutStrategies;
import io.github.ikunkk02.chatcanvas.chat.layout.RuntimeChatBounds;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameColorProvider;
import io.github.ikunkk02.chatcanvas.chat.style.StyledRangePipeline;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextMetrics;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextRenderer;

import java.util.List;

public final class ChatRenderEngine {
	private static final int HORIZONTAL_PADDING = 3;
	private static final int INPUT_VERTICAL_PADDING = 3;
	private static final float CLOSED_OPACITY = 0.58f;

	private final ChatLayoutCalculator layoutCalculator = new ChatLayoutCalculator();
	private final ChatBackgroundRenderer backgroundRenderer = new ChatBackgroundRenderer();
	private final ChatLineRenderer lineRenderer = new ChatLineRenderer();
	private final AnimationClock animationClock = new AnimationClock();
	private final AnimatedFloat openProgress = new AnimatedFloat(1.0f, 20.0f);
	private final PlayerNameColorProvider playerColors = new PlayerNameColorProvider();
	private final StyledRangePipeline stylePipeline = new StyledRangePipeline();

	private List<PreviewChatMessage> messages = List.of();
	private PreviewChatState state = PreviewChatState.OPEN;

	public void messages(List<PreviewChatMessage> messages) {
		if (this.messages == messages) return;
		this.messages = List.copyOf(messages);
		layoutCalculator.invalidate();
	}

	public PreviewChatState state() {
		return state;
	}

	public void state(PreviewChatState state) {
		if (this.state == state) return;
		this.state = state;
		openProgress.setTarget(state == PreviewChatState.OPEN ? 1.0f : 0.0f);
		animationClock.reset();
	}

	public void clearCache() {
		layoutCalculator.invalidate();
	}

	public void render(ChatRenderContext baseContext) {
		ChatTextConfig textConfig = baseContext.textConfig() == null
				? ChatTextConfig.DEFAULT
				: baseContext.textConfig().sanitized();
		ChatBackgroundConfig backgroundConfig = baseContext.backgroundConfig() == null
				? ChatBackgroundConfig.DEFAULT
				: baseContext.backgroundConfig().sanitized();
		playerARGB.updateConfig(baseContext.playerColorConfig());
		float progress = openProgress.update(animationClock.tick());
		float opacity = lerp(CLOSED_OPACITY, 1.0f, progress);
		int fullInputHeight = baseContext.textRenderer().lineHeight + INPUT_VERTICAL_PADDING;
		int inputHeight = Math.round(fullInputHeight * progress);
		ChatRenderContext context = new ChatRenderContext(
				baseContext.drawContext(),
				baseContext.textRenderer(),
				baseContext.x(),
				baseContext.y(),
				baseContext.width(),
				baseContext.height(),
				opacity,
				progress,
				baseContext.inputPlaceholder(),
				textConfig,
				backgroundConfig,
				baseContext.playerColorConfig(),
				baseContext.mentionConfig(),
				baseContext.localPlayerName(),
				baseContext.playerChatLayoutMode(),
				baseContext.splitMessageMaxWidthRatio(),
				baseContext.vanillaBackgroundOpacity()
		);

		PixelLayout totalLayout = new PixelLayout(context.x(), context.y(), context.width(), context.height());
		RuntimeChatBounds bounds = RuntimeChatBounds.calculate(
				totalLayout,
				inputHeight > 0,
				inputHeight,
				Math.round(RuntimeChatBounds.DEFAULT_INPUT_GAP * progress),
				(int) Math.ceil(ChatTextLayout.internalLineHeight(
						context.textRenderer().lineHeight, textConfig.lineSpacing())
						* textConfig.fontScale())
		);
		if (inputHeight > 0) {
			drawInput(context, bounds);
		}

		double fontScale = textConfig.fontScale();
		int fullInternalWidth = Math.max(1,
				(int) Math.floor(context.width() / fontScale));
		int wrapWidth = ChatBackgroundMetrics.wrapWidth(
				fullInternalWidth,
				backgroundConfig.horizontalPadding(),
				fontScale
		);
		List<ChatLayoutCalculator.ChatLine> lines =
				layoutCalculator.calculate(
						context.textRenderer(), messages, wrapWidth,
						context.localPlayerName(),
						context.mentionConfig() == null
								|| context.mentionConfig().requireAtSymbol(),
						textConfig.characterSpacing(),
						context.playerChatLayoutMode(),
						context.splitMessageMaxWidthRatio());
		ChatVerticalMetrics verticalMetrics = ChatTextLayout.verticalMetrics(
				context.textRenderer().lineHeight,
				context.textRenderer().lineHeight,
				1.0,
				textConfig.lineSpacing()
		);
		int internalLineHeight = (int) Math.round(verticalMetrics.lineAdvance());
		double screenLineHeight = verticalMetrics.lineAdvance() * fontScale;
		double lineY = bounds.messageBottom() - context.textRenderer().lineHeight * fontScale;
		int minimumY = bounds.messageTop();
		int depth = 0;
		context.drawContext().enableScissor(
				bounds.left(), bounds.messageTop(), bounds.right(), bounds.messageBottom());
		for (int index = lines.size() - 1; index >= 0 && lineY >= minimumY; index--) {
			ChatLayoutCalculator.ChatLine line = lines.get(index);
			float ageFade = state == PreviewChatState.CLOSED
					? Math.max(0.72f, 1.0f - depth * 0.055f)
					: 1.0f;
			float vanillaLineOpacity = opacity * ageFade;
			float lineOpacity = vanillaLineOpacity * (float) textConfig.textOpacity();
			int lineX = PlayerChatLayoutStrategies
					.forMode(context.playerChatLayoutMode())
					.textX(0, wrapWidth, line.width(), 0, line.selfMessage());
			context.drawContext().pose().pushMatrix();
			context.drawContext().pose().translate(
					context.x() + backgroundConfig.horizontalPadding(), (float) lineY, 0.0f);
			context.drawContext().pose().scale((float) fontScale, (float) fontScale, 1.0f);
			backgroundRenderer.drawMessageBackground(
					new ChatRenderContext(
							context.drawContext(),
							context.textRenderer(),
							0,
							0,
							wrapWidth,
							internalLineHeight,
							lineOpacity,
							context.inputProgress(),
							context.inputPlaceholder(),
							textConfig,
							backgroundConfig,
							context.playerColorConfig(),
							context.mentionConfig(),
							context.localPlayerName(),
							context.playerChatLayoutMode(),
							context.splitMessageMaxWidthRatio(),
							context.vanillaBackgroundOpacity()
					),
					ChatBackgroundMetrics.messageBounds(
							backgroundConfig.messageMode(),
							-backgroundConfig.horizontalPadding() / fontScale,
							wrapWidth + backgroundConfig.horizontalPadding() / fontScale,
							lineX,
							line.width(),
							0,
							context.textRenderer().lineHeight,
							verticalMetrics.lineAdvance(),
							backgroundConfig.horizontalPadding(),
							backgroundConfig.verticalPadding(),
							fontScale
					),
					vanillaLineOpacity * context.vanillaBackgroundOpacity()
			);
			FormattedCharSequence renderedLine = line.text();
			var color = line.sender() == null
					? java.util.OptionalInt.empty()
					: playerARGB.colorFor(line.sender());
			renderedLine = stylePipeline.apply(
					renderedLine,
					line.playerNameRange(),
					color,
					line.mentionRanges(),
					context.mentionConfig());
			lineRenderer.draw(context, renderedLine, lineX, 0, lineOpacity, textConfig.shadow());
			context.drawContext().pose().popMatrix();
			lineY -= screenLineHeight;
			depth++;
		}
		context.drawContext().disableScissor();
	}

	private void drawInput(ChatRenderContext context, RuntimeChatBounds bounds) {
		int inputHeight = bounds.inputHeight();
		int inputY = bounds.inputTop();
		backgroundRenderer.drawInputBackground(context, inputY, inputHeight);
		int textY = inputY + Math.max(1, (inputHeight - context.textRenderer().lineHeight) / 2);
		int color = (Math.round(190 * context.inputProgress()) << 24) | 0xC8CDD6;
		int textX = context.x() + HORIZONTAL_PADDING;
		SpacedTextRenderer.draw(
				context.drawContext(), context.textRenderer(),
				context.inputPlaceholder().getVisualOrderText(),
				textX, textY, color, true,
				context.textConfig().characterSpacing());
		int cursorX = Math.min(context.right() - 2,
				textX + SpacedTextMetrics.width(
						context.textRenderer(),
						context.inputPlaceholder().getVisualOrderText(),
						context.textConfig().characterSpacing()) + 2);
		context.drawContext().fill(cursorX, textY, cursorX + 1,
				Math.min(bounds.inputBottom() - 1, textY + context.textRenderer().lineHeight),
				(Math.round(220 * context.inputProgress()) << 24) | 0xFFFFFF);
	}

	private static float lerp(float start, float end, float progress) {
		return start + (end - start) * progress;
	}
}
