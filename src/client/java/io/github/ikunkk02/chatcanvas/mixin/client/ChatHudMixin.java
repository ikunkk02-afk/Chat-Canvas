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
import io.github.ikunkk02.chatcanvas.chat.layout.ChatTextLayoutEngine;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatVerticalMetrics;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageIngress;
import io.github.ikunkk02.chatcanvas.chat.render.ChatBackgroundDraw;
import io.github.ikunkk02.chatcanvas.chat.render.DualChatHudRenderer;
import io.github.ikunkk02.chatcanvas.chat.style.OrderedTextStyleOverlay;
import io.github.ikunkk02.chatcanvas.chat.style.StyledRangePipeline;
import io.github.ikunkk02.chatcanvas.chat.style.TextRange;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextHitTester;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextMetrics;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextRenderer;
import io.github.ikunkk02.chatcanvas.chat.text.ChatHeadsCompat;
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
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
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
	protected abstract int getLineHeight();

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

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$pushLayoutTransform(DrawContext context,
				TextRenderer textRenderer, int currentTick,
				int mouseX, int mouseY, boolean interactable, boolean insert,
														 CallbackInfo ci) {
		if (DualChatHudRenderer.instance().render(context, mouseX, mouseY, interactable)) {
			ci.cancel();
			return;
		}
		if (!ChatCanvasConfig.instance().enabled()) return;
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
		context.getMatrices().pushMatrix();
		context.getMatrices().translate((float) transform.offsetX(), (float) transform.offsetY());
		chat_canvas$matrixPushed = true;
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void chat_canvas$popLayoutTransform(DrawContext context,
				TextRenderer textRenderer, int currentTick,
				int mouseX, int mouseY, boolean interactable, boolean insert,
														CallbackInfo ci) {
		if (chat_canvas$matrixPushed) {
			context.getMatrices().popMatrix();
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

	@ModifyReturnValue(method = "getLineHeight", at = @At("RETURN"))
	private int chat_canvas$useConfiguredLineSpacing(int vanillaLineHeight) {
		ChatTextConfig config = ChatCanvasConfig.instance().text();
		return ChatTextLayout.internalLineHeight(
				vanillaLineHeight, config.fontScale(), config.lineSpacing());
	}

	@ModifyArg(
			method = "addVisibleMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/ChatHudLine;breakLines(Lnet/minecraft/client/font/TextRenderer;I)Ljava/util/List;"
			),
			index = 1
	)
	private int chat_canvas$reserveBackgroundPaddingForWrapping(int originalWidth) {
		ChatTextConfig text = ChatCanvasConfig.instance().text();
		return ChatTextLayout.glyphWrapWidth(
				getWidth(),
				ChatCanvasConfig.instance().background().horizontalPadding(),
				chat_canvas$glyphSafetyPixels(),
				getChatScale(),
				text.fontScale());
	}

	@WrapOperation(
			method = "addVisibleMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/hud/ChatHudLine;breakLines(Lnet/minecraft/client/font/TextRenderer;I)Ljava/util/List;"
			)
	)
	private List<OrderedText> chat_canvas$bindVisiblePlayerNameRanges(
				ChatHudLine self, TextRenderer renderer, int width,
				Operation<List<OrderedText>> original,
				@Local(argsOnly = true) ChatHudLine message) {
		ChatTextConfig config = ChatCanvasConfig.instance().text();
		List<OrderedText> lines = ChatTextLayoutEngine.instance().wrap(
				message, renderer, width, config.characterSpacing(), config.fontScale(),
				() -> original.call(self, renderer, Integer.MAX_VALUE / 4));
		ChatMessageMetadataRegistry.instance().registerVisibleLines(message, lines);
		return lines;
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
		ChatTextLayoutEngine.instance().clearWorld();
		ChatMessageMetadataRegistry.instance().clearAll();
		PlayerNameHitboxRegistry.clear();
	}

	@Inject(
			method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
			at = @At("HEAD")
	)
	private void chat_canvas$captureMessage(
			Text message, net.minecraft.network.message.MessageSignatureData signature,
			MessageIndicator indicator, CallbackInfo ci) {
		ChatCanvasMessageIngress.instance().acceptFromChatHud(message, signature);
	}

	@Inject(
			method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V",
			at = @At("RETURN")
	)
	private void chat_canvas$pruneMessageMetadata(ChatHudLine message, CallbackInfo ci) {
		ChatMessageMetadataRegistry.instance().retainMessages(messages);
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
					chat_canvas$visualWidth(text),
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
				chat_canvas$visualWidth(line.content())
						+ chat_canvas$scaled(ChatHeadsCompat.extraWidth(line)),
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
		return (chat_canvas$background().horizontalPadding()
				+ chat_canvas$glyphSafetyPixels()) / scale
				- chat_canvas$VANILLA_TEXT_ORIGIN_X;
	}

	@Unique
	private double chat_canvas$contentRight() {
		double scale = Math.max(0.0001, getChatScale());
		double internalMessageWidth = getWidth() / scale;
		double internalPadding = (chat_canvas$background().horizontalPadding()
				+ chat_canvas$glyphSafetyPixels()) / scale;
		return Math.max(chat_canvas$contentLeft(),
				internalMessageWidth - internalPadding - chat_canvas$VANILLA_TEXT_ORIGIN_X);
	}

	@Unique
	private ChatVerticalMetrics chat_canvas$verticalMetrics() {
		ChatTextConfig config = ChatCanvasConfig.instance().text();
		return ChatTextLayout.verticalMetrics(
				client.textRenderer.fontHeight,
				client.textRenderer.fontHeight,
				config.fontScale(),
				config.lineSpacing()
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
			ChatMessageMetadataRegistry.VisibleMetadata player, double drawX, int y) {
		TextRange nameRange = player.playerNameRange();
		double spacing = ChatCanvasConfig.instance().text().characterSpacing();
		int prefixWidth;
		int nameWidth;
		if (Math.abs(spacing) < 0.00001) {
			prefixWidth = renderer.getWidth(OrderedTextStyleOverlay.selectRange(
					text, new TextRange(0, nameRange.startCodePoint())));
			nameWidth = renderer.getWidth(OrderedTextStyleOverlay.selectRange(text, nameRange));
		} else {
			double startX = SpacedTextMetrics.xAtCodePoint(
					renderer, text, spacing, nameRange.startCodePoint());
			double endX = SpacedTextMetrics.xAtCodePoint(
					renderer, text, spacing, nameRange.endCodePoint());
			prefixWidth = (int) Math.round(startX);
			nameWidth = Math.max(0, (int) Math.round(endX - startX));
		}
		if (nameWidth <= 0) return;
		ChatHudLine.Visible line = chat_canvas$lineLookup.get(text);
		if (line != null) {
			prefixWidth += ChatHeadsCompat.widthBeforeCodePoint(
					line, nameRange.startCodePoint());
		}

		Matrix3x2f matrix = context.getMatrices();
		double fontScale = ChatCanvasConfig.instance().text().fontScale();
		Vector2f topLeft = matrix.transformPosition(
				new Vector2f((float) (drawX + prefixWidth * fontScale), y));
		Vector2f bottomRight = matrix.transformPosition(
				new Vector2f((float) (drawX + (prefixWidth + nameWidth) * fontScale),
				(float) (y + renderer.fontHeight * fontScale)));
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
			ChatBackgroundDraw.drawBorder(context,
					(int) Math.floor(drawX + prefixWidth * fontScale), y,
					(int) Math.ceil(nameWidth * fontScale),
					(int) Math.ceil(renderer.fontHeight * fontScale), 0xFFE66B6B);
		}
	}

	@Unique
	private int chat_canvas$visualWidth(OrderedText text) {
		return chat_canvas$scaled(ChatLineWidthCache.width(
				client.textRenderer, text,
				ChatCanvasConfig.instance().text().characterSpacing()));
	}

	@Unique
	private int chat_canvas$scaled(int baseWidth) {
		return (int) Math.ceil(Math.max(0, baseWidth)
				* ChatCanvasConfig.instance().text().fontScale());
	}

	@Unique
	private int chat_canvas$glyphSafetyPixels() {
		return ChatCanvasConfig.instance().text().shadow() ? 2 : 1;
	}
}
