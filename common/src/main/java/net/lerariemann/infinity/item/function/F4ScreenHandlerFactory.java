package net.lerariemann.infinity.item.function;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.lerariemann.infinity.item.F4Item;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class F4ScreenHandlerFactory implements ExtendedMenuProvider {
    ItemStack f4;
    int slot;
    String destination;
    public F4ScreenHandlerFactory(PlayerEntity player) {
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
