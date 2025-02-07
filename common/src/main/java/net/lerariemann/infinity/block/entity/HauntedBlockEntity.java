package net.lerariemann.infinity.block.entity;

import net.lerariemann.infinity.registry.core.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HauntedBlockEntity extends BlockEntity {
    public BlockState original;

    public HauntedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HAUNTED.get(), pos, state);
        original = Blocks.AIR.getDefaultState();
    }

    public static int getExpiryTicks(World w) {
        return w.random.nextBetween(20, 200);
    }

    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        BlockState.CODEC.encodeStart(NbtOps.INSTANCE, original).ifSuccess(e -> tag.put("InnerBlockState", e));
    }

    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        if (tag.contains("InnerBlockState"))
            BlockState.CODEC.decode(NbtOps.INSTANCE, tag.get("InnerBlockState")).ifSuccess(e -> original = e.get(0));
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    public void updateFrom(BlockState bs) {
        original = bs;
        markDirty();
    }
}
