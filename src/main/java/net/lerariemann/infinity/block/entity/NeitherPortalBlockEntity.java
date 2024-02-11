package net.lerariemann.infinity.block.entity;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class NeitherPortalBlockEntity extends BlockEntity implements RenderAttachmentBlockEntity {
    private final PropertyDelegate propertyDelegate;
    private long dimension;

    public NeitherPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEITHER_PORTAL, pos, state);
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
    }

    public long getDimension() {
        return this.dimension;
    }

    public void setDimension(long i) {
        this.dimension = i;
    }
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putLong("Dimension", this.dimension);
    }

    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        this.dimension = tag.getLong("Dimension");
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public Object getRenderAttachmentData() {
        return (int)dimension;
    }
}
