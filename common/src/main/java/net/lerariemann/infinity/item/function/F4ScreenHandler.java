package net.lerariemann.infinity.item.function;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.lerariemann.infinity.item.F4Item;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

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

    public static class Factory implements ExtendedMenuProvider {
        ItemStack f4;
        int slot;
        String destination;

        public Factory(PlayerEntity player) {
            f4 = player.getStackInHand(Hand.MAIN_HAND);
            slot = player.getInventory().selectedSlot;
            Identifier id = f4.get(ModComponentTypes.DESTINATION.get());
            destination = id == null ? "" : id.toString();
        }

        @Override
        public void saveExtraData(PacketByteBuf packetByteBuf) {
            packetByteBuf.writeString(destination);
            packetByteBuf.writeVarInt(slot);
        }

        @Override
        public Text getDisplayName() {
            return destination.isEmpty() ? F4Item.defaultDimensionTooltip() : F4Item.getDimensionTooltip(Identifier.of(destination));
        }

        @Override
        public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            return new F4ScreenHandler(syncId, playerInventory, destination, slot);
        }
    }
}
