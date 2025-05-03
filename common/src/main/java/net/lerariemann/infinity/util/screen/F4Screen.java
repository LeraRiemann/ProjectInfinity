package net.lerariemann.infinity.util.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.Text;

import java.util.concurrent.atomic.AtomicInteger;

@Environment(EnvType.CLIENT)
public class F4Screen extends HandledScreen<F4ScreenHandler> {
    TextFieldWidget widthValue;
    TextFieldWidget heightValue;

    public static F4Screen of(PlayerEntity player) {
        F4ScreenHandler.Factory factory = new F4ScreenHandler.Factory(player);
        F4ScreenHandler handler = (F4ScreenHandler)factory.createMenu(0, player.getInventory(), player);
        return new F4Screen(handler, player.getInventory(), factory.getDisplayName());
    }

    public F4Screen(F4ScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
        handler.addListener(new ScreenHandlerListener() {
                    public void onSlotUpdate(ScreenHandler handlerx, int slotId, ItemStack stack) {
                    }

                    public void onPropertyUpdate(ScreenHandler handlerx, int property, int value) {
                        if (property == 0) handler.width.set(value);
                        else handler.height.set(value);
                    }
                });
    }

    @Override
    protected void init() {
        super.init();
        widthValue = new TextFieldWidget(this.textRenderer, x + 82, y + 30, 12, 8, Text.literal("width"));
        heightValue = new TextFieldWidget(this.textRenderer, x + 82, y + 57, 12, 8, Text.literal("height"));
        setupValue(widthValue, handler.width);
        setupValue(heightValue, handler.height);
    }

    public void setupValue(TextFieldWidget value, AtomicInteger valueReference) {
        value.setEditableColor(-1);
        value.setUneditableColor(-1);
        value.setDrawsBackground(false);
        value.setMaxLength(2);
        value.setText(String.valueOf(valueReference.get()));
        value.setTextPredicate(string -> (string.isEmpty() ||
                (string.matches("[0-9]*")
                        && Integer.parseInt(string) > 0
                        && Integer.parseInt(string) < 22)));
        value.setX(x + (valueReference.get() < 10 ? 85 : 82));
        value.setChangedListener(string -> {
            int v = string.isEmpty() ? 3 : Integer.parseInt(string);
            valueReference.set(v);
            value.setX(x + ((string.length() < 2) ? 85 : 82));
        });
        addSelectableChild(value);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, Text.literal("Portal width"), x + 59, y + 18, 4210752, false);
        context.drawText(this.textRenderer, Text.literal("Portal height"), x + 56, y + 45, 4210752, false);
        this.widthValue.render(context, mouseX, mouseY, delta);
        this.heightValue.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, InfinityMethods.getId("textures/gui/f4.png"),
                x, y, this.backgroundWidth, this.backgroundHeight);
    }
}
