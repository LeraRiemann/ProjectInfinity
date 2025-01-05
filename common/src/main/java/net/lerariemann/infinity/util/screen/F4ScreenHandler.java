package net.lerariemann.infinity.util.screen;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lerariemann.infinity.item.F4Item;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.lerariemann.infinity.registry.var.ModScreenHandlers;
import net.lerariemann.infinity.util.BackportMethods;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
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
        width = new AtomicInteger(BackportMethods.getOrDefaultInt(stack, ModComponentTypes.SIZE_X, 3));
        height = new AtomicInteger(BackportMethods.getOrDefaultInt(stack, ModComponentTypes.SIZE_Y, 3));

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
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putInt(ModComponentTypes.SIZE_X, MathHelper.clamp(width.get(), 1, 21));
        nbtCompound.putInt(ModComponentTypes.SIZE_Y, MathHelper.clamp(height.get(), 1, 21));
        stack.setNbt(nbtCompound);
        playerInventory.setStack(slot, st);
        super.onClosed(player);
        if (player instanceof ClientPlayerEntity) {
            ClientPlayNetworking.send(new ModPayloads.F4Payload(slot, width.get(), height.get()));
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
            Identifier id = BackportMethods.getDimensionIdentifier(f4);
            destination = id == null ? "" : id.toString();
        }

        @Override
        public void saveExtraData(PacketByteBuf packetByteBuf) {
            packetByteBuf.writeString(destination);
            packetByteBuf.writeVarInt(slot);
        }

        @Override
        public Text getDisplayName() {
            return F4Item.getDimensionTooltip(destination.isEmpty() ? null : new Identifier(destination));
        }

        @Override
        public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            return new F4ScreenHandler(syncId, playerInventory, destination, slot);
        }
    }
}
