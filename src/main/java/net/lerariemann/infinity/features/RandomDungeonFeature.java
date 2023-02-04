package net.lerariemann.infinity.features;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.slf4j.Logger;

public class RandomDungeonFeature extends Feature<RandomDungeonFeatureConfig> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final BlockState AIR = Blocks.CAVE_AIR.getDefaultState();

    public RandomDungeonFeature(Codec<RandomDungeonFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<RandomDungeonFeatureConfig> context) {
        BlockPos blockPos2;
        int u;
        int t;
        int s;
        Predicate<BlockState> predicate = Feature.notInBlockTagPredicate(BlockTags.FEATURES_CANNOT_REPLACE);
        BlockPos blockPos = context.getOrigin();
        Random random = context.getRandom();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        int j = random.nextInt(2) + context.getConfig().size;
        int k = -j - 1;
        int l = j + 1;
        int o = random.nextInt(2) + context.getConfig().size;
        int p = -o - 1;
        int q = o + 1;
        int r = 0;
        for (s = k; s <= l; ++s) {
            for (t = -1; t <= 4; ++t) {
                for (u = p; u <= q; ++u) {
                    blockPos2 = blockPos.add(s, t, u);
                    Material material = structureWorldAccess.getBlockState(blockPos2).getMaterial();
                    boolean bl = material.isSolid();
                    if (t == -1 && !bl) {
                        return false;
                    }
                    if (t == 4 && !bl) {
                        return false;
                    }
                    if (s != k && s != l && u != p && u != q || t != 0 || !structureWorldAccess.isAir(blockPos2) || !structureWorldAccess.isAir(blockPos2.up())) continue;
                    ++r;
                }
            }
        }
        if (r < 1 || r > 5) {
            return false;
        }
        for (s = k; s <= l; ++s) {
            for (t = 3; t >= -1; --t) {
                for (u = p; u <= q; ++u) {
                    blockPos2 = blockPos.add(s, t, u);
                    BlockState blockState = structureWorldAccess.getBlockState(blockPos2);
                    if (s == k || t == -1 || u == p || s == l || t == 4 || u == q) {
                        if (blockPos2.getY() >= structureWorldAccess.getBottomY() && !structureWorldAccess.getBlockState(blockPos2.down()).getMaterial().isSolid()) {
                            structureWorldAccess.setBlockState(blockPos2, AIR, Block.NOTIFY_LISTENERS);
                            continue;
                        }
                        if (!blockState.getMaterial().isSolid() || blockState.isOf(Blocks.CHEST)) continue;
                        if (t == -1 && random.nextInt(4) != 0) {
                            this.setBlockStateIf(structureWorldAccess, blockPos2, context.getConfig().decorationProvider, predicate);
                            continue;
                        }
                        this.setBlockStateIf(structureWorldAccess, blockPos2, context.getConfig().mainProvider, predicate);
                        continue;
                    }
                    if (blockState.isOf(Blocks.CHEST) || blockState.isOf(Blocks.SPAWNER)) continue;
                    this.setBlockStateIf(structureWorldAccess, blockPos2, AIR, predicate);
                }
            }
        }
        block6: for (s = 0; s < 2; ++s) {
            for (t = 0; t < 3; ++t) {
                u = blockPos.getX() + random.nextInt(j * 2 + 1) - j;
                BlockPos blockPos3 = new BlockPos(u, blockPos.getY(), blockPos.getZ() + random.nextInt(o * 2 + 1) - o);
                if (!structureWorldAccess.isAir(blockPos3)) continue;
                int x = 0;
                for (Direction direction : Direction.Type.HORIZONTAL) {
                    if (!structureWorldAccess.getBlockState(blockPos3.offset(direction)).getMaterial().isSolid()) continue;
                    ++x;
                }
                if (x != 1) continue;
                this.setBlockStateIf(structureWorldAccess, blockPos3, StructurePiece.orientateChest(structureWorldAccess, blockPos3, Blocks.CHEST.getDefaultState()), predicate);
                LootableContainerBlockEntity.setLootTable(structureWorldAccess, random, blockPos3, LootTables.SIMPLE_DUNGEON_CHEST);
                continue block6;
            }
        }
        this.setBlockStateIf(structureWorldAccess, blockPos, Blocks.SPAWNER.getDefaultState(), predicate);
        BlockEntity blockEntity = structureWorldAccess.getBlockEntity(blockPos);
        if (blockEntity instanceof MobSpawnerBlockEntity mobSpawnerBlockEntity) {
            mobSpawnerBlockEntity.setEntityType(EntityType.get(context.getConfig().mob).orElse(EntityType.PIG), random);
        } else {
            LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
        return true;
    }
}


