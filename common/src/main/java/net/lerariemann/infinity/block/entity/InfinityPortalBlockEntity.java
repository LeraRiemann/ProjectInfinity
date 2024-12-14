package net.lerariemann.infinity.block.entity;

import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.WarpLogic;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class InfinityPortalBlockEntity extends BlockEntity {
    private final PropertyDelegate propertyDelegate;
    private Identifier dimension;
    private int portalColor;
    private boolean isOpen;
    @Nullable
    private BlockPos otherSidePos;

    public InfinityPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEITHER_PORTAL.get(), pos, state);
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                if (index == 0) {
                    return InfinityPortalBlockEntity.this.portalColor;
                }
                return 0;
            }

            public void set(int index, int value) {
                if (index == 0) {
                    InfinityPortalBlockEntity.this.portalColor = value;
                }

            }
            public int size() {
                return 1;
            }
        };
    }

    public InfinityPortalBlockEntity(BlockPos pos, BlockState state, int i) {
        this(pos, state, i, InfinityMethods.getDimId(i));
    }

    public InfinityPortalBlockEntity(BlockPos pos, BlockState state, int i, Identifier id) {
        this(pos, state);
        this.dimension = id;
        this.portalColor = i;
        this.isOpen = false;
        this.otherSidePos = null;
    }

    public Identifier getDimension() {
        return this.dimension;
    }
    public int getPortalColor() {
        return this.portalColor;
    }

    public boolean getOpen() {
        return this.isOpen;
    }
    @Nullable
    public BlockPos getOtherSidePos() { return this.otherSidePos; }

    public void setDimension(long c) {
        setColor((int)c);
        setDimension(InfinityMethods.getDimId(c));
    }
    public void setDimension(Identifier i) {
        this.dimension = i;
    }
    public void setColor(int c) {
        this.portalColor = c;
    }
    public void setOpen(boolean i) {
        this.isOpen = i;
    }
    public void setBlockPos(BlockPos pos) {
        this.otherSidePos = pos;
        markDirty();
    }

    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt("Color", this.portalColor);
        tag.putString("Dimension", this.dimension.toString());
        tag.putBoolean("Open", this.isOpen);
        if (otherSidePos != null) {
            NbtCompound pos = new NbtCompound();
            pos.putInt("x", otherSidePos.getX());
            pos.putInt("y", otherSidePos.getY());
            pos.putInt("z", otherSidePos.getZ());
            tag.put("other_side", pos);
        }
    }

    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        if (tag.contains("Dimension", NbtElement.NUMBER_TYPE)) { //conversion from legacy formats
            this.portalColor = tag.getInt("Dimension");
            if (tag.contains("DimensionName")) {
                this.dimension = new Identifier(tag.getString("DimensionName"));
            }
            else this.dimension = InfinityMethods.getDimId(this.portalColor);
        }
        else if (tag.contains("Dimension", NbtElement.STRING_TYPE)) { //new better format
            this.dimension = new Identifier(tag.getString("Dimension"));
            this.portalColor = tag.contains("Color", NbtElement.INT_TYPE) ?
                    tag.getInt("Color") :
                    (world != null ? WarpLogic.getPortalColorApplier(dimension, world.getServer()) :
                            WarpLogic.getPortalColorApplier(dimension, new NbtCompound())).apply(pos);
        }
        else {
            setDimension(InfinityMethods.getRandomSeed(new Random())); //random by default
        }
        this.isOpen = tag.getBoolean("Open");
        if (tag.contains("other_side")) {
            NbtCompound pos = tag.getCompound("other_side");
            otherSidePos = new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z"));
        }
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
    public Object getRenderData() {
        return propertyDelegate.get(0);
    }
}
