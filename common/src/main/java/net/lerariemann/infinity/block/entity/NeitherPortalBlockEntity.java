package net.lerariemann.infinity.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class NeitherPortalBlockEntity extends BlockEntity {
    private final PropertyDelegate propertyDelegate;
    private long dimension;
    private boolean isOpen;

    public NeitherPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEITHER_PORTAL.get(), pos, state);
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                if (index == 0) {
                    return (int)(NeitherPortalBlockEntity.this.dimension);
                }
                return 0;
            }

            public void set(int index, int value) {
                if (index == 0) {
                    NeitherPortalBlockEntity.this.dimension = value;
                }

            }
            public int size() {
                return 1;
            }
        };
    }

    public NeitherPortalBlockEntity(BlockPos pos, BlockState state, long i) {
        this(pos, state);
        this.dimension = i;
        this.isOpen = false;
    }


    public long getDimension() {
        return this.dimension;
    }
    public boolean getOpen() {
        return this.isOpen;
    }

    public void setDimension(long i) {
        this.dimension = i;
    }

    public void setOpen(boolean i) {
        this.isOpen = i;
    }
    public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(tag, registryLookup);
        tag.putLong("Dimension", this.dimension);
        tag.putBoolean("Open", this.isOpen);
    }

    public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(tag, registryLookup);
        this.dimension = tag.getLong("Dimension");
        this.isOpen = tag.getBoolean("Open");
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

//    @Override
    public Object getRenderData() {
        return propertyDelegate.get(0);
    }
}
