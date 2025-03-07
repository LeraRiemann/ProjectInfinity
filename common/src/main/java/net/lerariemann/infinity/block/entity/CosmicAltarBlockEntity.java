package net.lerariemann.infinity.block.entity;

import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.util.config.ConfigGenerator;
import net.lerariemann.infinity.registry.core.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CosmicAltarBlockEntity extends BlockEntity {
    protected int time;
    protected Map<String, BlockState> map;
    public static int[] offsets = new int[]{-1, 0, 1};
    public static int[] offsets_y = new int[]{1, 2, 3};
    public CosmicAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COSMIC_ALTAR.get(), pos, state);
        time = -1; //not ticking when set improperly
        map = new HashMap<>();
    }

    @Override
    public boolean supports(BlockState state) {
        return true;
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, CosmicAltarBlockEntity be) {
        if (world instanceof ServerWorld serverWorld && be.time == 0) {
            for (int i : offsets) for (int j : offsets_y) for (int k : offsets) {
                be.map.put(i + "," + j + "," + k, world.getBlockState(pos.add(i, j, k)));
                world.setBlockState(pos.add(i, j, k), Blocks.AIR.getDefaultState());
            }
            invoke(serverWorld, pos);
            for (int i : offsets) for (int j : offsets_y) for (int k : offsets) {
                world.setBlockState(pos.add(i, j, k), be.fromMap(i, j, k));
            }
            ((MinecraftServerAccess)world.getServer()).infinity$onInvocation();
            be.markRemoved();
            world.setBlockState(pos, be.fromMap(0, 0, 0));
        }
    }
    static void invoke(ServerWorld serverWorld, BlockPos altarPos) {
        ConfigGenerator.generateAll(serverWorld.getServer());
        Set<String> fluidBlockNames = ConfigGenerator.generateFluids();
        ConfigGenerator.generateBlocks(serverWorld, altarPos.up(2), altarPos.up(), fluidBlockNames);
    }

    public void addNull(BlockState s) {
        map.put("0,0,0", s);
    }

    public void startTime() {
        time = 0;
    }

    BlockState fromMap(int i, int j, int k) {
        BlockState s = map.get(i + "," + j + "," + k);
        if (s==null) s = Blocks.AIR.getDefaultState();
        return s;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains("time", NbtElement.INT_TYPE)) {
            time = nbt.getInt("time");
        }
        map = new HashMap<>();
        if (nbt.contains("map", NbtElement.COMPOUND_TYPE)) {
            RegistryWrapper<Block> registryEntryLookup =
                    registryLookup.getOptionalWrapper(RegistryKeys.BLOCK).orElse(Registries.BLOCK.getReadOnlyWrapper());
            NbtCompound mapnbt = nbt.getCompound("map");
            for (String s : mapnbt.getKeys()) {
                map.put(s, NbtHelper.toBlockState(registryEntryLookup, nbt.getCompound(s)));
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("time", this.time);
        if (!map.isEmpty()) {
            NbtCompound mapnbt = new NbtCompound();
            for (String s : map.keySet()) {
                mapnbt.put(s, NbtHelper.fromBlockState(map.get(s)));
            }
            nbt.put("map", mapnbt);
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
}
