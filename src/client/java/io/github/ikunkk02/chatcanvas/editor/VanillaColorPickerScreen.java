package io.github.ikunkk02.chatcanvas.editor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.IntConsumer;

/**
 * A vanilla-style RGB color picker with three sliders and a hex input.
 * Returns the chosen color to the caller via {@code onPicked} when "Done"
 * is pressed.  Pressing "Cancel" or Escape returns to the parent screen
 * without changing the color.
 */
public final class VanillaColorPickerScreen extends Screen {

    private final Screen parent;
    private final int initialColor;
    private final IntConsumer onPicked;

    private int currentColor;
    private TextFieldWidget hexField;

    public VanillaColorPickerScreen(Screen parent, int initialColor, IntConsumer onPicked) {
        super(Text.translatable("chat_canvas.color_picker.title"));
        this.parent = parent;
        this.initialColor = initialColor;
        this.onPicked = onPicked;
        this.currentColor = initialColor & 0xFFFFFF;
    }

    @Override
    protected void init() {
        super.init();
        int cx = width / 2;
        int y = 40;

        // --- R/G/B sliders ---
        int r = (currentColor >> 16) & 0xFF;
        int g = (currentColor >> 8) & 0xFF;
        int b = currentColor & 0xFF;

        addDrawableChild(colorSlider(cx - 100, y, 200, r, 0xFF0000,
                v -> setRGB(v, g, b)));
        addDrawableChild(colorSlider(cx - 100, y + 28, 200, g, 0x00FF00,
                v -> setRGB(r, v, b)));
        addDrawableChild(colorSlider(cx - 100, y + 56, 200, b, 0x0000FF,
                v -> setRGB(r, g, v)));

        // --- hex input ---
        hexField = new TextFieldWidget(textRenderer, cx - 60, y + 90, 120, 20, Text.empty());
        hexField.setMaxLength(7);
        updateHexField();
        hexField.setChangedListener(val -> {
            if (val.startsWith("#")) val = val.substring(1);
            try {
                if (val.length() == 6) {
                    int parsed = Integer.parseInt(val, 16);
                    setRGB((parsed >> 16) & 0xFF, (parsed >> 8) & 0xFF, parsed & 0xFF);
                }
            } catch (NumberFormatException ignored) { }
        });
        addDrawableChild(hexField);

        // --- footer ---
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("chat_canvas.color_picker.restore_default"),
                btn -> { currentColor = initialColor & 0xFFFFFF; updateAll(); })
                .dimensions(cx - 120, y + 120, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("chat_canvas.action.cancel"), btn -> onCancel())
                .dimensions(cx - 10, y + 120, 60, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("chat_canvas.action.confirm"), btn -> onDone())
                .dimensions(cx + 58, y + 120, 60, 20).build());
    }

    private SliderWidget colorSlider(int x, int y, int w, int value, int tint,
                                     java.util.function.IntConsumer onChange) {
        return new SliderWidget(x, y, w, 20,
                Text.literal(formatComponent(value, tint)),
                value / 255.0) {
            @Override
            protected void updateMessage() {
                int v = (int)(this.value * 255);
                this.setMessage(Text.literal(formatComponent(v, tint)));
            }
            @Override
            protected void applyValue() {
                onChange.accept((int)(this.value * 255));
            }
        };
    }

    private static String formatComponent(int value, int tint) {
        String label = switch (tint) {
            case 0xFF0000 -> "R";
            case 0x00FF00 -> "G";
            case 0x0000FF -> "B";
            default -> "?";
        };
        return label + ": " + value;
    }

    private void setRGB(int r, int g, int b) {
        currentColor = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        updateAll();
    }

    private void updateAll() {
        updateHexField();
        // Rebuild to refresh sliders
        clearChildren();
        init();
    }

    private void updateHexField() {
        if (hexField != null) {
            hexField.setText(String.format("#%06X", currentColor));
        }
    }

    private void onDone() {
        onPicked.accept(currentColor);
        returnToParent();
    }

    private void onCancel() {
        returnToParent();
    }

    @Override
    public void close() {
        onCancel();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int cx = width / 2;
        context.drawCenteredTextWithShadow(textRenderer, title, cx, 15, 0xFFFFFF);

        // Color preview swatch
        int swatchX = cx - 80;
        int swatchY = height - 60;
        context.fill(swatchX, swatchY, swatchX + 160, swatchY + 24, 0xFF000000 | currentColor);
        context.drawBorder(swatchX, swatchY, 160, 24, 0xFFFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (client != null && client.world == null) {
            renderPanoramaBackground(context, delta);
        }
        context.fill(0, 0, width, height, 0x88070A10);
    }

    private void returnToParent() {
        if (client != null) client.setScreen(parent);
    }
}
