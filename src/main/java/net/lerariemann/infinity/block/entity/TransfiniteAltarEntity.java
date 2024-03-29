package net.lerariemann.infinity.block.entity;

import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.block.custom.TransfiniteAltar;
import net.lerariemann.infinity.util.ConfigGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class TransfiniteAltarEntity extends BlockEntity {
    private int time;
    public static int[] offsets = new int[]{-1, 0, 1};
    public static int[] offsets_y = new int[]{1, 2, 3};
    Map<String, BlockState> map;
    public static Random r = new Random();

    public TransfiniteAltarEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR, pos, state);
        time = 0;
        map = new HashMap<>();
    }

    public static void remove(World world, BlockPos pos, TransfiniteAltarEntity be) {
        be.markRemoved();
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
        LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(world);
        if (lightningEntity != null) {
            lightningEntity.setPosition(pos.toCenterPos());
            world.spawnEntity(lightningEntity);
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, TransfiniteAltarEntity be) {
        MinecraftServerAccess a = ((MinecraftServerAccess)(Objects.requireNonNull(world.getServer())));
        int stage = state.get(TransfiniteAltar.COLOR);
        if (be.time >= 20) {
            if (stage == 6) {
                for (int i : offsets) for (int k : offsets) {
                    BlockState s = be.map.get(i + "," + k);
                    if (s==null) s = Blocks.STONE.getDefaultState();
                    world.setBlockState(pos.add(i, -1, k), s);
                }
                remove(world, pos, be);
                return;
            }
            be.time = 0;
            TransfiniteAltar.bumpAge(world, pos, state);
        }
        if (stage == 0) {
            if(be.time == 0) {
                for (int i : offsets) for (int j : offsets_y) for (int k : offsets)
                    world.setBlockState(pos.add(i, j, k), Blocks.AIR.getDefaultState());
            }
            if(be.time == 10) {
                ConfigGenerator.generateAll(world, pos.up(2), pos.up());
                a.setDimensionProvider();
            }
        }
        if(stage == 0 && be.time == 19) for (int i : offsets) for (int k : offsets) be.map.put(i + "," + k, world.getBlockState(pos.add(i, -1, k)));
        if (stage > 0 && be.time % 3 == 0) {
            world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1f, 1f);
            for (int i : offsets) for (int k : offsets) {
                if (i == 0 && k == 0) world.setBlockState(pos.add(i, -1, k), Blocks.STONE.getDefaultState());
                else world.setBlockState(pos.add(i, -1, k), Registries.BLOCK.get(new Identifier(a.getDimensionProvider().randomName(r, "full_blocks"))).getDefaultState());
            }
        }
        be.time+=1;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("time", NbtElement.INT_TYPE)) {
            time = nbt.getInt("time");
        }
        map = new HashMap<>();
        if (nbt.contains("map", NbtElement.COMPOUND_TYPE)) {
            RegistryWrapper<Block> registryEntryLookup = this.world != null ? this.world.createCommandRegistryWrapper(RegistryKeys.BLOCK) : Registries.BLOCK.getReadOnlyWrapper();
            NbtCompound mapnbt = nbt.getCompound("map");
            for (String s : mapnbt.getKeys()) {
                map.put(s, NbtHelper.toBlockState(registryEntryLookup, nbt.getCompound(s)));
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("time", this.time);
        NbtCompound mapnbt = new NbtCompound();
        for (String s : map.keySet()) {
            mapnbt.put(s, NbtHelper.fromBlockState(map.get(s)));
        }
    }
}
