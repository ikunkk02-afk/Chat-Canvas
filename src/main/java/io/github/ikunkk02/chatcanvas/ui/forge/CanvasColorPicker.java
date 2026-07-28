package io.github.ikunkk02.chatcanvas.ui.forge;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Simple RGB color picker with recent colors.
 */
public class CanvasColorPicker extends CanvasWidget {
    private final Supplier<Integer> colorGetter;
    private final Consumer<Integer> colorSetter;
    private final List<Integer> recentColors = new ArrayList<>(8);
    private final float[] hsb = new float[3];
    private boolean pickingHue;
    private int defaultColor;

    public CanvasColorPicker(int x, int y, int width, int height,
                              Supplier<Integer> colorGetter, Consumer<Integer> colorSetter) {
        super(x, y, width, height);
        this.colorGetter = colorGetter;
        this.colorSetter = colorSetter;
        this.defaultColor = colorGetter.get();
        syncHSB(colorGetter.get());
    }

    private void syncHSB(int argb) {
        int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
        Color.RGBtoHSB(r, g, b, hsb);
    }

    public void addRecentColor(int color) {
        recentColors.remove(Integer.valueOf(color));
        recentColors.add(0, color);
        while (recentColors.size() > 8) recentColors.remove(recentColors.size() - 1);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        int color = colorGetter.get();
        // Preview swatch
        context.fill(x, y, x + 20, y + height, color | 0xFF000000);
        // Hue bar
        for (int i = 0; i < width - 24; i++) {
            float hue = (float) i / (width - 24);
            context.fill(x + 22 + i, y, x + 23 + i, y + height / 2, Color.HSBtoRGB(hue, 1f, 1f) | 0xFF000000);
        }
        // Saturation/value area
        for (int sx = 0; sx < width - 24; sx++) {
            for (int sy = 0; sy < height / 2; sy++) {
                float sat = (float) sx / (width - 24);
                float bri = 1f - (float) sy / (height / 2);
                context.fill(x + 22 + sx, y + height / 2 + sy, x + 23 + sx, y + height / 2 + sy + 1,
                        Color.HSBtoRGB(hsb[0], sat, bri) | 0xFF000000);
            }
        }
        // Recent colors
        for (int i = 0; i < Math.min(recentColors.size(), 4); i++) {
            context.fill(x + i * 6, y + height - 6, x + i * 6 + 5, y + height - 1, recentColors.get(i) | 0xFF000000);
        }
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (!isActive() || !visible) return false;
        if (button == 1) { colorSetter.accept(defaultColor); syncHSB(defaultColor); return true; }
        if (isMouseOver(mx, my)) {
            pickingHue = my < y + height / 2.0;
            handlePick(mx, my);
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (isMouseOver(mx, my)) { handlePick(mx, my); return true; }
        return false;
    }

    private void handlePick(double mx, double my) {
        int relX = (int) Math.round(mx - x - 22);
        int relY = (int) Math.round(my - y);
        int range = width - 24;
        if (pickingHue) {
            float hue = Mth.clamp((float) relX / range, 0f, 1f);
            hsb[0] = hue;
        } else {
            float sat = Mth.clamp((float) relX / (range - 1), 0f, 1f);
            float bri = Mth.clamp(1f - (float) relY / (height / 2), 0f, 1f);
            hsb[1] = sat; hsb[2] = bri;
        }
        colorSetter.accept(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]) & 0x00FFFFFF);
    }
}
