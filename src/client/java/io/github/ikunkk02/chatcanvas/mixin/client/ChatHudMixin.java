package io.github.ikunkk02.chatcanvas.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatBackgroundBounds;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatBackgroundMetrics;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLineMetrics;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLineWidthCache;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatHudTransform;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLayoutRuntime;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatTextLayout;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatVerticalMetrics;
import io.github.ikunkk02.chatcanvas.chat.render.ChatBackgroundDraw;
import io.github.ikunkk02.chatcanvas.chat.style.OrderedTextStyleOverlay;
import io.github.ikunkk02.chatcanvas.chat.style.StyledRangePipeline;
import io.github.ikunkk02.chatcanvas.chat.style.TextRange;
import io.github.ikunkk02.chatcanvas.chat.identity.ChatMessageMetadataRegistry;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerColorRuntime;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitbox;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitboxRegistry;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.text.StringVisitable;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
	@Unique
	private static final double chat_canvas$VANILLA_TEXT_ORIGIN_X = 4.0;
	@Shadow
	private MinecraftClient client;
	@Shadow
	private List<ChatHudLine.Visible> visibleMessages;
	@Shadow
	private List<ChatHudLine> messages;
	@Shadow
	public abstract int getWidth();
	@Shadow
	public abstract double getChatScale();
	@Shadow
	private double toChatLineX(double x) {
		throw new AssertionError();
	}
	@Shadow
	private double toChatLineY(double y) {
		throw new AssertionError();
	}
	@Shadow
	private int getMessageLineIndex(double chatLineX, double chatLineY) {
		throw new AssertionError();
	}

	@Unique
	private boolean chat_canvas$matrixPushed;
	@Unique
	private boolean chat_canvas$scissorEnabled;
	@Unique
	private ChatBackgroundConfig chat_canvas$frameBackground;
	@Unique
	private final Map<OrderedText, ChatHudLine.Visible> chat_canvas$lineLookup =
			new IdentityHashMap<>();
	@Unique
	private final StyledRangePipeline chat_canvas$stylePipeline = new StyledRangePipeline();

	@Inject(method = "render", at = @At("HEAD"))
	private void chat_canvas$pushLayoutTransform(DrawContext context, int currentTick,
												 int mouseX, int mouseY, boolean focused,
												 CallbackInfo ci) {
		ChatHudTransform transform = ChatLayoutRuntime.currentTransform();
		PlayerNameHitboxRegistry.beginFrame();
		chat_canvas$frameBackground = ChatCanvasConfig.instance().background();
		context.enableScissor(
				transform.bounds().left(),
				transform.bounds().messageTop(),
				transform.bounds().right(),
				transform.bounds().messageBottom()
		);
		chat_canvas$scissorEnabled = true;
		context.getMatrices().push();
		context.getMatrices().translate((float) transform.offsetX(), (float) transform.offsetY(), 0.0f);
		chat_canvas$matrixPushed = true;
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void chat_canvas$popLayoutTransform(DrawContext context, int currentTick,
												int mouseX, int mouseY, boolean focused,
												CallbackInfo ci) {
		if (chat_canvas$matrixPushed) {
			context.getMatrices().pop();
			chat_canvas$matrixPushed = false;
		}
		if (chat_canvas$scissorEnabled) {
			context.disableScissor();
			chat_canvas$scissorEnabled = false;
		}
		chat_canvas$frameBackground = null;
	}

	@ModifyReturnValue(method = "getWidth", at = @At("RETURN"))
	private int chat_canvas$useConfiguredWidth(int original) {
		return ChatLayoutRuntime.currentTransform().configuredWidth();
	}

	@ModifyReturnValue(method = "getHeight", at = @At("RETURN"))
	private int chat_canvas$useConfiguredHeight(int original) {
		return ChatLayoutRuntime.currentTransform().configuredInternalHeight();
	}

	@ModifyReturnValue(method = "getChatScale", at = @At("RETURN"))
	private double chat_canvas$useConfiguredFontScale(double vanillaScale) {
		return ChatTextLayout.effectiveScale(
				vanillaScale, ChatCanvasConfig.instance().text().fontScale());
	}

	@ModifyReturnValue(method = "getLineHeight", at = @At("RETURN"))
	private int chat_canvas$useConfiguredLineSpacing(int vanillaLineHeight) {
		return (int) Math.round(ChatTextLayout.verticalMetrics(
				client.textRenderer.fontHeight,
				vanillaLineHeight,
				1.0,
				ChatCanvasConfig.instance().text().lineSpacing()
		).lineAdvance());
	}

	@WrapOperation(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V",
					ordinal = 0
			)
	)
	private void chat_canvas$drawCompactMessageBackground(DrawContext context,
														 int x1, int y1, int x2, int y2, int color,
														 Operation<Void> original,
														 @Local ChatHudLine.Visible visible) {
		ChatBackgroundConfig background = chat_canvas$background();
		if (background.messageMode() == io.github.ikunkk02.chatcanvas.config.MessageBackgroundMode.HIDDEN) {
			return;
		}
		ChatVerticalMetrics verticalMetrics = chat_canvas$verticalMetrics();
		int textY = y2 + chat_canvas$vanillaTextOffset();
		ChatLineMetrics lineMetrics = chat_canvas$metrics(visible);
		double scale = Math.max(0.0001, getChatScale());
		double messageLeft = -chat_canvas$VANILLA_TEXT_ORIGIN_X;
		double messageRight = getWidth() / scale - chat_canvas$VANILLA_TEXT_ORIGIN_X;
		ChatBackgroundBounds bounds = ChatBackgroundMetrics.messageBounds(
				background.messageMode(),
				messageLeft,
				messageRight,
				lineMetrics.drawX(),
				lineMetrics.renderedWidth(),
				textY,
				textY + client.textRenderer.fontHeight,
				verticalMetrics.lineAdvance(),
				background.horizontalPadding(),
				background.verticalPadding(),
				scale
		);
		int configuredColor = ChatBackgroundMetrics.composeBackgroundColor(
				background.messageColor(),
				background.messageOpacity(),
				(color >>> 24) / 255.0
		);
		ChatBackgroundDraw.fill(context, bounds, configuredColor);
	}

	@WrapOperation(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V",
					ordinal = 1
			)
	)
	private void chat_canvas$drawCompactIndicatorBackground(DrawContext context,
														   int x1, int y1, int x2, int y2, int color,
														   Operation<Void> original) {
		ChatVerticalMetrics metrics = chat_canvas$verticalMetrics();
		int textY = y2 + chat_canvas$vanillaTextOffset();
		original.call(
				context,
				x1,
				(int) Math.floor(metrics.backgroundTop(textY)),
				x2,
				(int) Math.ceil(metrics.backgroundBottom(textY)),
				color
		);
	}

	@ModifyArg(
			method = "addVisibleMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/util/ChatMessages;breakRenderedChatMessageLines(Lnet/minecraft/text/StringVisitable;ILnet/minecraft/client/font/TextRenderer;)Ljava/util/List;"
			),
			index = 1
	)
	private int chat_canvas$reserveBackgroundPaddingForWrapping(int originalWidth) {
		return ChatBackgroundMetrics.wrapWidth(
				originalWidth,
				ChatCanvasConfig.instance().background().horizontalPadding(),
				getChatScale()
		);
	}

	@WrapOperation(
			method = "addVisibleMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/util/ChatMessages;breakRenderedChatMessageLines(Lnet/minecraft/text/StringVisitable;ILnet/minecraft/client/font/TextRenderer;)Ljava/util/List;"
			)
	)
	private List<OrderedText> chat_canvas$bindVisiblePlayerNameRanges(
			StringVisitable text, int width, TextRenderer renderer,
			Operation<List<OrderedText>> original,
			@Local(argsOnly = true) ChatHudLine message) {
		List<OrderedText> lines = original.call(text, width, renderer);
		ChatMessageMetadataRegistry.instance().registerVisibleLines(message, lines);
		return lines;
	}

	@WrapOperation(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"
			)
	)
	private int chat_canvas$drawConfiguredChatLine(DrawContext context, TextRenderer renderer,
												  OrderedText text, int x, int y, int color,
												  Operation<Integer> original) {
		ChatTextConfig config = ChatCanvasConfig.instance().text();
		ChatLineMetrics metrics = chat_canvas$metrics(text);
		int drawX = (int) Math.round(metrics.drawX());
		int configuredColor = ChatTextLayout.multiplyAlpha(color, config.textOpacity());
		OrderedText renderedText = text;
		ChatMessageMetadataRegistry.VisibleMetadata metadata =
				ChatMessageMetadataRegistry.instance().visibleMetadata(text);
		if (metadata != null) {
			var playerColor = metadata.sender() == null
					? java.util.OptionalInt.empty()
					: PlayerColorRuntime.provider().colorFor(metadata.sender());
			renderedText = chat_canvas$stylePipeline.apply(
					text,
					metadata.playerNameRange(),
					playerColor,
					metadata.mentionRanges(),
					ChatCanvasConfig.instance().mention());
		}
		int result;
		if (config.shadow()) {
			result = original.call(context, renderer, renderedText, drawX, y, configuredColor);
		} else {
			result = context.drawText(renderer, renderedText, drawX, y, configuredColor, false);
		}
		if (metadata != null && metadata.sender() != null && metadata.playerNameRange() != null) {
			chat_canvas$recordPlayerNameHitbox(context, renderer, text, metadata, drawX, y);
		}
		return result;
	}

	@WrapOperation(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"
			)
	)
	private int chat_canvas$drawConfiguredQueueText(DrawContext context, TextRenderer renderer,
													Text text, int x, int y, int color,
													Operation<Integer> original) {
		ChatTextConfig config = ChatCanvasConfig.instance().text();
		int configuredColor = ChatTextLayout.multiplyAlpha(color, config.textOpacity());
		if (config.shadow()) {
			return original.call(context, renderer, text, x, y, configuredColor);
		}
		return context.drawText(renderer, text, x, y, configuredColor, false);
	}

	@Inject(method = "getTextStyleAt", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$getAlignedTextStyle(double x, double y,
												 CallbackInfoReturnable<Style> cir) {
		double chatLineX = toChatLineX(x);
		double chatLineY = toChatLineY(y);
		int lineIndex = getMessageLineIndex(chatLineX, chatLineY);
		if (lineIndex < 0 || lineIndex >= visibleMessages.size()) {
			cir.setReturnValue(null);
			return;
		}

		ChatHudLine.Visible line = visibleMessages.get(lineIndex);
		ChatLineMetrics metrics = chat_canvas$metrics(line);
		int localX = MathHelper.floor(metrics.localX(chatLineX));
		if (localX < 0 || localX > metrics.renderedWidth()) {
			cir.setReturnValue(null);
			return;
		}
		cir.setReturnValue(client.textRenderer.getTextHandler().getStyleAt(line.content(), localX));
	}

	@Inject(method = "getIndicatorX", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$getAlignedIndicatorX(ChatHudLine.Visible line,
												  CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue((int) Math.round(chat_canvas$metrics(line).indicatorX()));
	}

	@Inject(method = "refresh", at = @At("HEAD"))
	private void chat_canvas$clearLineMetrics(CallbackInfo ci) {
		chat_canvas$lineLookup.clear();
		ChatLineWidthCache.clear();
		ChatMessageMetadataRegistry.instance().clearVisible();
	}

	@Inject(method = "clear", at = @At("HEAD"))
	private void chat_canvas$clearMessageMetadata(boolean clearHistory, CallbackInfo ci) {
		chat_canvas$lineLookup.clear();
		ChatLineWidthCache.clear();
		ChatMessageMetadataRegistry.instance().clearAll();
		PlayerNameHitboxRegistry.clear();
	}

	@Inject(
			method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V",
			at = @At("RETURN")
	)
	private void chat_canvas$pruneMessageMetadata(ChatHudLine message, CallbackInfo ci) {
		ChatMessageMetadataRegistry.instance().retainMessages(messages);
	}

	@ModifyVariable(method = "toChatLineX", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private double chat_canvas$screenToChatX(double screenX) {
		return ChatLayoutRuntime.currentTransform().screenToChatX(screenX);
	}

	@ModifyVariable(method = "toChatLineY", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private double chat_canvas$screenToChatY(double screenY) {
		return ChatLayoutRuntime.currentTransform().screenToChatY(screenY);
	}

	@ModifyVariable(method = "mouseClicked", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private double chat_canvas$queueClickX(double screenX) {
		return ChatLayoutRuntime.currentTransform().screenToChatX(screenX);
	}

	@ModifyVariable(method = "mouseClicked", at = @At("HEAD"), argsOnly = true, ordinal = 1)
	private double chat_canvas$queueClickY(double screenY) {
		return ChatLayoutRuntime.currentTransform().screenToChatY(screenY);
	}

	@Unique
	private ChatLineMetrics chat_canvas$metrics(OrderedText text) {
		if (chat_canvas$lineLookup.size() >= 256 && !chat_canvas$lineLookup.containsKey(text)) {
			chat_canvas$lineLookup.clear();
		}
		ChatHudLine.Visible line = chat_canvas$lineLookup.get(text);
		if (line == null) {
			for (ChatHudLine.Visible candidate : visibleMessages) {
				if (candidate.content() == text) {
					line = candidate;
					chat_canvas$lineLookup.put(text, candidate);
					break;
				}
			}
		}
		if (line == null) {
			return ChatTextLayout.metricsWithin(
					-1,
					ChatLineWidthCache.width(client.textRenderer, text),
					chat_canvas$contentLeft(),
					chat_canvas$contentRight(),
					0,
					ChatCanvasConfig.instance().text().alignment(),
					0.0,
					1.0
			);
		}
		return chat_canvas$metrics(line);
	}

	@Unique
	private ChatLineMetrics chat_canvas$metrics(ChatHudLine.Visible line) {
		int indicatorReservation = 0;
		MessageIndicator indicator = line.indicator();
		if (line.endOfEntry() && indicator != null && indicator.icon() != null) {
			indicatorReservation = indicator.icon().width + 6;
		}
		return ChatTextLayout.metricsWithin(
				-1,
				ChatLineWidthCache.width(client.textRenderer, line.content()),
				chat_canvas$contentLeft(),
				chat_canvas$contentRight(),
				indicatorReservation,
				ChatCanvasConfig.instance().text().alignment(),
				0.0,
				1.0
		);
	}

	@Unique
	private double chat_canvas$contentLeft() {
		double scale = Math.max(0.0001, getChatScale());
		return chat_canvas$background().horizontalPadding() / scale
				- chat_canvas$VANILLA_TEXT_ORIGIN_X;
	}

	@Unique
	private double chat_canvas$contentRight() {
		double scale = Math.max(0.0001, getChatScale());
		double internalMessageWidth = getWidth() / scale;
		double internalPadding = chat_canvas$background().horizontalPadding() / scale;
		return Math.max(chat_canvas$contentLeft(),
				internalMessageWidth - internalPadding - chat_canvas$VANILLA_TEXT_ORIGIN_X);
	}

	@Unique
	private ChatVerticalMetrics chat_canvas$verticalMetrics() {
		return ChatTextLayout.verticalMetrics(
				client.textRenderer.fontHeight,
				client.textRenderer.fontHeight,
				1.0,
				ChatCanvasConfig.instance().text().lineSpacing()
		);
	}

	@Unique
	private int chat_canvas$vanillaTextOffset() {
		double spacing = client.options.getChatLineSpacing().getValue();
		return (int) Math.round(-8.0 * (spacing + 1.0) + 4.0 * spacing);
	}

	@Unique
	private ChatBackgroundConfig chat_canvas$background() {
		return chat_canvas$frameBackground == null
				? ChatCanvasConfig.instance().background()
				: chat_canvas$frameBackground;
	}

	@Unique
	private void chat_canvas$recordPlayerNameHitbox(
			DrawContext context, TextRenderer renderer, OrderedText text,
			ChatMessageMetadataRegistry.VisibleMetadata player, int drawX, int y) {
		TextRange nameRange = player.playerNameRange();
		int prefixWidth = renderer.getWidth(OrderedTextStyleOverlay.selectRange(
				text, new TextRange(0, nameRange.startCodePoint())));
		int nameWidth = renderer.getWidth(OrderedTextStyleOverlay.selectRange(text, nameRange));
		if (nameWidth <= 0) return;

		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
		Vector4f topLeft = new Vector4f(drawX + prefixWidth, y, 0.0f, 1.0f).mul(matrix);
		Vector4f bottomRight = new Vector4f(
				drawX + prefixWidth + nameWidth,
				y + renderer.fontHeight,
				0.0f,
				1.0f
		).mul(matrix);
		ChatHudLine.Visible line = chat_canvas$lineLookup.get(text);
		int messageIndex = line == null ? -1 : visibleMessages.indexOf(line);
		PlayerNameHitboxRegistry.add(new PlayerNameHitbox(
				player.sender().uuid(),
				player.sender().playerName(),
				messageIndex,
				Math.min(topLeft.x, bottomRight.x),
				Math.min(topLeft.y, bottomRight.y),
				Math.max(topLeft.x, bottomRight.x),
				Math.max(topLeft.y, bottomRight.y)
		));
		if (ChatCanvasConfig.instance().playerColors().showNameHitboxes()) {
			context.drawBorder(drawX + prefixWidth, y, nameWidth,
					renderer.fontHeight, 0xFFE66B6B);
		}
	}
}
