package net.lerariemann.infinity.item.function;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class F4Screen extends Screen implements ScreenHandlerProvider<F4ScreenHandler> {
    protected final F4ScreenHandler handler;
    int offsetX = 8;
    boolean v = true;

    public static F4Screen of(PlayerEntity player) {
        F4ScreenHandler.Factory factory = new F4ScreenHandler.Factory(player);
        F4ScreenHandler handler = (F4ScreenHandler)factory.createMenu(0, player.getInventory(), player);
        return new F4Screen(handler, player.getInventory(), factory.getDisplayName());
    }

    public F4Screen(F4ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(title);
        this.handler = handler;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (v) offsetX++;
        else offsetX--;
        if (offsetX > width - 149) v = false;
        if (offsetX < 0) v = true;
        context.drawText(this.textRenderer, Text.literal("A concept of a config screen"), offsetX, 8, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.literal("Portal width is " + handler.width), offsetX, 16, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.literal("Portal height is " + handler.height), offsetX,  24, 0xFFFFFF, false);
    }

    @Override
    public F4ScreenHandler getScreenHandler() {
        return handler;
    }
}
