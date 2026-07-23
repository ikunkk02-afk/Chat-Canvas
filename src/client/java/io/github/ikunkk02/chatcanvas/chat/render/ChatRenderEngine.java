package io.github.ikunkk02.chatcanvas.chat.render;

import io.github.ikunkk02.chatcanvas.animation.AnimatedFloat;
import io.github.ikunkk02.chatcanvas.animation.AnimationClock;
import io.github.ikunkk02.chatcanvas.chat.layout.RuntimeChatBounds;
import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import net.minecraft.text.Text;

import java.util.List;

public final class ChatRenderEngine {
	private static final int HORIZONTAL_PADDING = 3;
	private static final int INPUT_VERTICAL_PADDING = 3;
	private static final int LINE_GAP = 1;
	private static final float CLOSED_OPACITY = 0.58f;

	private final ChatLayoutCalculator layoutCalculator = new ChatLayoutCalculator();
	private final ChatBackgroundRenderer backgroundRenderer = new ChatBackgroundRenderer();
	private final ChatLineRenderer lineRenderer = new ChatLineRenderer();
	private final AnimationClock animationClock = new AnimationClock();
	private final AnimatedFloat openProgress = new AnimatedFloat(1.0f, 20.0f);

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

	public void render(ChatRenderContext baseContext) {
		float progress = openProgress.update(animationClock.tick());
		float opacity = lerp(CLOSED_OPACITY, 1.0f, progress);
		int fullInputHeight = baseContext.textRenderer().fontHeight + INPUT_VERTICAL_PADDING;
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
				baseContext.inputPlaceholder()
		);

		PixelLayout totalLayout = new PixelLayout(context.x(), context.y(), context.width(), context.height());
		RuntimeChatBounds bounds = RuntimeChatBounds.calculate(
				totalLayout,
				inputHeight > 0,
				inputHeight,
				Math.round(RuntimeChatBounds.DEFAULT_INPUT_GAP * progress),
				context.textRenderer().fontHeight + LINE_GAP
		);
		if (inputHeight > 0) {
			drawInput(context, bounds);
		}

		int wrapWidth = Math.max(1, context.width() - HORIZONTAL_PADDING * 2);
		List<ChatLayoutCalculator.ChatLine> lines =
				layoutCalculator.calculate(context.textRenderer(), messages, wrapWidth);
		int lineY = bounds.messageBottom() - context.textRenderer().fontHeight;
		int minimumY = bounds.messageTop();
		int depth = 0;
		context.drawContext().enableScissor(
				bounds.left(), bounds.messageTop(), bounds.right(), bounds.messageBottom());
		for (int index = lines.size() - 1; index >= 0 && lineY >= minimumY; index--) {
			ChatLayoutCalculator.ChatLine line = lines.get(index);
			float ageFade = state == PreviewChatState.CLOSED
					? Math.max(0.72f, 1.0f - depth * 0.055f)
					: 1.0f;
			float lineOpacity = opacity * ageFade;
			int lineX = context.x() + HORIZONTAL_PADDING;
			backgroundRenderer.drawMessageBackground(context, lineX, lineY, line.width(), lineOpacity);
			lineRenderer.draw(context, line.text(), lineX, lineY, lineOpacity);
			lineY -= context.textRenderer().fontHeight + LINE_GAP;
			depth++;
		}
		context.drawContext().disableScissor();
	}

	private void drawInput(ChatRenderContext context, RuntimeChatBounds bounds) {
		int inputHeight = bounds.inputHeight();
		int inputY = bounds.inputTop();
		backgroundRenderer.drawInputBackground(context, inputY, inputHeight);
		int textY = inputY + Math.max(1, (inputHeight - context.textRenderer().fontHeight) / 2);
		int color = (Math.round(190 * context.inputProgress()) << 24) | 0xC8CDD6;
		int textX = context.x() + HORIZONTAL_PADDING;
		context.drawContext().drawText(context.textRenderer(), context.inputPlaceholder(),
				textX, textY, color, true);
		int cursorX = Math.min(context.right() - 2,
				textX + context.textRenderer().getWidth(context.inputPlaceholder()) + 2);
		context.drawContext().fill(cursorX, textY, cursorX + 1,
				Math.min(bounds.inputBottom() - 1, textY + context.textRenderer().fontHeight),
				(Math.round(220 * context.inputProgress()) << 24) | 0xFFFFFF);
	}

	private static float lerp(float start, float end, float progress) {
		return start + (end - start) * progress;
	}
}
