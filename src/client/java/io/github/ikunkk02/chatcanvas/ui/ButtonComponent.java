package io.github.ikunkk02.chatcanvas.ui;

import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.core.AnimatableProperty;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.util.FocusHandler;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.ui.event.CharTyped;
import io.wispforest.owo.ui.event.KeyPress;
import io.wispforest.owo.ui.event.MouseDrag;
import io.wispforest.owo.ui.event.MouseDown;
import io.wispforest.owo.ui.event.MouseEnter;
import io.wispforest.owo.ui.event.MouseLeave;
import io.wispforest.owo.ui.event.MouseScroll;
import io.wispforest.owo.ui.event.MouseUp;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * An owo UI component backed by owo's vanilla ButtonComponent.
 *
 * Owo 0.13 made ButtonComponent a vanilla widget, while the editor still
 * needs buttons to participate in an owo layout. This adapter keeps the
 * vanilla button behavior and exposes the UIComponent contract to parents.
 */
public final class ButtonComponent extends io.wispforest.owo.ui.component.ButtonComponent
        implements UIComponent {
    private final VanillaWidgetComponent delegate;

    private ButtonComponent(Component text, Consumer<ButtonComponent> action) {
        super(text, clicked -> action.accept((ButtonComponent) clicked));
        this.delegate = UIComponents.wrapVanillaWidget(this);
        // The owo ButtonComponent constructor calls UIComponent#sizing before
        // this adapter's delegate can be initialized. Reapply its intended
        // content sizing once the wrapper exists.
        this.delegate.sizing(Sizing.content());
    }

    public static ButtonComponent create(Component text, Consumer<ButtonComponent> action) {
        return new ButtonComponent(text, action);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        delegate.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    @Override
    public ParentUIComponent parent() { return delegate.parent(); }

    @Override
    public FocusHandler focusHandler() { return delegate.focusHandler(); }

    @Override
    public UIComponent positioning(Positioning positioning) {
        delegate.positioning(positioning);
        return this;
    }

    @Override
    public AnimatableProperty<Positioning> positioning() { return delegate.positioning(); }

    @Override
    public UIComponent margins(Insets margins) {
        delegate.margins(margins);
        return this;
    }

    @Override
    public AnimatableProperty<Insets> margins() { return delegate.margins(); }

    @Override
    public UIComponent horizontalSizing(Sizing sizing) {
        if (delegate != null) delegate.horizontalSizing(sizing);
        return this;
    }

    @Override
    public AnimatableProperty<Sizing> horizontalSizing() { return delegate.horizontalSizing(); }

    @Override
    public UIComponent verticalSizing(Sizing sizing) {
        if (delegate != null) delegate.verticalSizing(sizing);
        return this;
    }

    @Override
    public AnimatableProperty<Sizing> verticalSizing() { return delegate.verticalSizing(); }

    @Override
    public UIComponent id(String id) {
        delegate.id(id);
        return this;
    }

    @Override
    public String id() { return delegate.id(); }

    @Override
    public UIComponent tooltip(List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> tooltip) {
        delegate.tooltip(tooltip);
        return this;
    }

    @Override
    public List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> tooltip() {
        return delegate.tooltip();
    }

    @Override
    public void inflate(Size space) { delegate.inflate(space); }

    @Override
    public void mount(ParentUIComponent parent, int x, int y) { delegate.mount(parent, x, y); }

    @Override
    public void dismount(UIComponent.DismountReason reason) { delegate.dismount(reason); }

    @Override
    public <C extends UIComponent> C configure(Consumer<C> action) { return delegate.configure(action); }

    @Override
    public boolean onMouseDown(MouseButtonEvent event, boolean doubled) {
        return delegate.onMouseDown(event, doubled);
    }

    @Override
    public EventSource<MouseDown> mouseDown() { return delegate.mouseDown(); }

    @Override
    public boolean onMouseUp(MouseButtonEvent event) { return delegate.onMouseUp(event); }

    @Override
    public EventSource<MouseUp> mouseUp() { return delegate.mouseUp(); }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        return delegate.onMouseScroll(mouseX, mouseY, amount);
    }

    @Override
    public EventSource<MouseScroll> mouseScroll() { return delegate.mouseScroll(); }

    @Override
    public boolean onMouseDrag(MouseButtonEvent event, double deltaX, double deltaY) {
        return delegate.onMouseDrag(event, deltaX, deltaY);
    }

    @Override
    public EventSource<MouseDrag> mouseDrag() { return delegate.mouseDrag(); }

    @Override
    public boolean onKeyPress(KeyEvent event) { return delegate.onKeyPress(event); }

    @Override
    public EventSource<KeyPress> keyPress() { return delegate.keyPress(); }

    @Override
    public boolean onCharTyped(CharacterEvent event) { return delegate.onCharTyped(event); }

    @Override
    public EventSource<CharTyped> charTyped() { return delegate.charTyped(); }

    @Override
    public boolean canFocus(UIComponent.FocusSource focusSource) { return delegate.canFocus(focusSource); }

    @Override
    public void onFocusGained(UIComponent.FocusSource focusSource) { delegate.onFocusGained(focusSource); }

    @Override
    public EventSource<io.wispforest.owo.ui.event.FocusGained> focusGained() {
        return delegate.focusGained();
    }

    @Override
    public void onFocusLost() { delegate.onFocusLost(); }

    @Override
    public EventSource<io.wispforest.owo.ui.event.FocusLost> focusLost() { return delegate.focusLost(); }

    @Override
    public EventSource<MouseEnter> mouseEnter() { return delegate.mouseEnter(); }

    @Override
    public EventSource<MouseLeave> mouseLeave() { return delegate.mouseLeave(); }

    @Override
    public CursorStyle cursorStyle() { return delegate.cursorStyle(); }

    @Override
    public UIComponent cursorStyle(CursorStyle cursorStyle) {
        delegate.cursorStyle(cursorStyle);
        return this;
    }

    @Override
    public int width() { return delegate.width(); }

    @Override
    public int height() { return delegate.height(); }

    @Override
    public int x() { return delegate.x(); }

    @Override
    public int y() { return delegate.y(); }

    @Override
    public void updateX(int x) { delegate.updateX(x); }

    @Override
    public void updateY(int y) { delegate.updateY(y); }

    @Override
    public void update(float delta, int mouseX, int mouseY) { delegate.update(delta, mouseX, mouseY); }

    @Override
    public boolean shouldDrawTooltip(double mouseX, double mouseY) {
        return delegate.shouldDrawTooltip(mouseX, mouseY);
    }
}
