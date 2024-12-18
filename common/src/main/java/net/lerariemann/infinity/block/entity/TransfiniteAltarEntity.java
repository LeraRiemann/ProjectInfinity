package net.lerariemann.infinity.block.entity;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.TransfiniteAltar;
import net.lerariemann.infinity.util.ConfigGenerator;
import net.lerariemann.infinity.registry.core.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

public class TransfiniteAltarEntity extends CosmicAltarEntity {
    public TransfiniteAltarEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR.get(), pos, state);
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
                InfinityMod.updateProvider(Objects.requireNonNull(world.getServer()));
            }
        }
        if(stage == 0 && be.time == 19) for (int i : offsets) for (int k : offsets)
            be.map.put(i + "," + k, world.getBlockState(pos.add(i, -1, k)));
        if (stage > 0 && be.time % 3 == 0) {
            world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1f, 1f);
            for (int i : offsets) for (int k : offsets) {
                if (i == 0 && k == 0) world.setBlockState(pos.add(i, -1, k), Blocks.STONE.getDefaultState());
                else world.setBlockState(pos.add(i, -1, k), Registries.BLOCK.get(Identifier.of(
                        InfinityMod.provider.randomName(InfinityMod.random, "full_blocks"))).getDefaultState());
            }
        }
        if (be.time >= 0) be.time+=1;
    }
}
