package net.lerariemann.infinity.util.screen;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.lerariemann.infinity.item.F4Item;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.lerariemann.infinity.registry.var.ModScreenHandlers;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class F4ScreenHandler extends ScreenHandler {
    public final PlayerInventory playerInventory;
    public final ItemStack stack;
    public final int slot;
    public AtomicInteger width;
    public AtomicInteger height;

    public F4ScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf packet) {
        this(syncId, playerInventory, packet.readString(), packet.readVarInt());
    }
    public F4ScreenHandler(int syncId, PlayerInventory playerInventory, String destination, int slot) {
        super(ModScreenHandlers.F4.get(), syncId);
        this.playerInventory = playerInventory;
        this.slot = slot;
        stack = playerInventory.getStack(slot);
        width = new AtomicInteger(stack.getOrDefault(ModComponentTypes.SIZE_X.get(), 3));
        height = new AtomicInteger(stack.getOrDefault(ModComponentTypes.SIZE_Y.get(), 3));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        ItemStack st = stack.copy();
        st.applyComponentsFrom(ComponentMap.builder()
                .add(ModComponentTypes.SIZE_X.get(), Math.clamp(width.get(), 1, 21))
                .add(ModComponentTypes.SIZE_Y.get(), Math.clamp(height.get(), 1, 21))
                .build());
        playerInventory.setStack(slot, st);
        super.onClosed(player);
        if (player instanceof ClientPlayerEntity) {
            ModPayloads.sendF4UpdatePayload(slot, width.get(), height.get());
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
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
            return F4Item.getDimensionTooltip(destination.isEmpty() ? null : Identifier.of(destination));
        }

        @Override
        public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            return new F4ScreenHandler(syncId, playerInventory, destination, slot);
        }
    }
}
