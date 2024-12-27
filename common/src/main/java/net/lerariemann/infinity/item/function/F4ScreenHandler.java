package net.lerariemann.infinity.item.function;

import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

public class F4ScreenHandler extends ScreenHandler {
    public final PlayerInventory playerInventory;
    public final ItemStack stack;
    public final int slot;
    public int width;
    public int height;

    public F4ScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf packet) {
        this(syncId, playerInventory, packet.readString(), packet.readVarInt());
    }
    public F4ScreenHandler(int syncId, PlayerInventory playerInventory, String destination, int slot) {
        super(ModItemFunctions.F4_SCREEN_HANDLER.get(), syncId);
        this.playerInventory = playerInventory;
        this.slot = slot;
        stack = playerInventory.getStack(slot);
        width = stack.getOrDefault(ModComponentTypes.SIZE_X.get(), 3);
        height = stack.getOrDefault(ModComponentTypes.SIZE_Y.get(), 3);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return false;
    }
}
