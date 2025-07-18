package net.lerariemann.infinity.block.entity;

import net.lerariemann.infinity.options.PortalColorApplier;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.registry.core.ModBlockEntities;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.lerariemann.infinity.util.teleport.InfinityPortal;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class InfinityPortalBlockEntity extends TintableBlockEntity {
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
                    InfinityPortalBlockEntity.this.portalColor = value & 0xFFFFFF;
                }

            }
            public int size() {
                return 1;
            }
        };
    }

    public InfinityPortalBlockEntity(BlockPos pos, BlockState state, Identifier id) {
        this(pos, state, (int)InfinityMethods.getNumericFromId(id), id);
    }

    public InfinityPortalBlockEntity(BlockPos pos, BlockState state, int i, Identifier id) {
        this(pos, state);
        this.dimension = id;
        this.portalColor = i & 0xFFFFFF;
        this.isOpen = false;
        this.otherSidePos = null;
    }

    public Identifier getDimension() {
        return this.dimension;
    }
    public int getPortalColor() {
        return this.portalColor;
    }
    public boolean isOpen() {
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
        this.portalColor = c & 0xFFFFFF;
        if (world != null) {
            BlockState bs = world.getBlockState(pos);
            world.updateListeners(pos, bs, bs, 0);
        }
    }
    public void setOpen(boolean i) {
        this.isOpen = i;
    }
    public void setBlockPos(BlockPos pos) {
        this.otherSidePos = pos;
        markDirty();
    }

    public void setData(MinecraftServer server, Identifier i) {
        if (server == null) {
            setData(i);
            return;
        }
        setDimension(i);
        setColor(PortalColorApplier.of(i, server).apply(getPos()));
        setOpen(server.getWorldRegistryKeys().contains(RegistryKey.of(RegistryKeys.WORLD, i)));
    }
    public void setData(Identifier i) {
        setDimension(i);
        setColor((int)InfinityMethods.getNumericFromId(i));
        setOpen(false);
    }

    public ServerWorld getDimensionAsWorld() {
        if (getWorld() instanceof ServerWorld serverWorld)
            return serverWorld.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, getDimension()));
        return null;
    }
    public boolean isConnected() {
        if (!isOpen()) return false;
        if (getWorld() instanceof ServerWorld worldFrom && !InfinityMethods.isTimebombed(worldFrom)) {
            return InfinityPortal.isValidDestination(worldFrom, getDimensionAsWorld(), getOtherSidePos());
        }
        return false;
    }
    public boolean isConnectedBothSides() {
        if (!isOpen()) return false;
        if (getWorld() instanceof ServerWorld worldFrom && !InfinityMethods.isTimebombed(worldFrom)) {
            ServerWorld worldTo = getDimensionAsWorld();
            BlockPos posTo = getOtherSidePos();
            if (posTo == null || !InfinityMethods.dimExists(worldTo)) return false;
            return (worldTo.getBlockEntity(posTo) instanceof InfinityPortalBlockEntity ipbe
                    && ipbe.getDimension().toString().equals(worldFrom.getRegistryKey().getValue().toString())
                    && ipbe.isConnected());
        }
        return false;
    }


    public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(tag, registryLookup);
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

    public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(tag, registryLookup);
        if (tag.getType("Dimension") == NbtElement.NUMBER_TYPE) { //conversion from legacy formats
            this.portalColor = NbtUtils.getInt(tag, "Dimension") & 0xFFFFFF;
            if (tag.contains("DimensionName")) {
                this.dimension = Identifier.of(NbtUtils.getString(tag, "DimensionName"));
            }
            else this.dimension = InfinityMethods.getDimId(this.portalColor);
        }
        else if (tag.getType("Dimension") == NbtElement.STRING_TYPE) { //new better format
            this.dimension = Identifier.of(NbtUtils.getString(tag, "Dimension"));
            this.portalColor = NbtUtils.getInt(tag, "Color", (world != null ? PortalColorApplier.of(dimension, world.getServer()) :
                    PortalColorApplier.of(dimension, new NbtCompound())).apply(pos) & 0xFFFFFF);
        }
        else {
            setDimension(InfinityMethods.getRandomSeed(new Random())); //random by default
        }
        this.isOpen = NbtUtils.getBoolean(tag, "Open", false);
        if (tag.contains("other_side")) {
            NbtCompound pos = NbtUtils.getCompound(tag,"other_side");
            otherSidePos = new BlockPos(NbtUtils.getInt(pos, "x"), NbtUtils.getInt(pos, "y"), NbtUtils.getInt(pos, "z"));
        }
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
    public int getTint() {
        return propertyDelegate.get(0);
    }
}
