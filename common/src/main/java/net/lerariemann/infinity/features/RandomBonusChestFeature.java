package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.stream.IntStream;

public class RandomBonusChestFeature extends Feature<RandomBonusChestFeature.Config> {
    public RandomBonusChestFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<Config> context) {
        Random random = context.getRandom();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        ChunkPos chunkPos = new ChunkPos(context.getOrigin());
        IntArrayList intArrayList = Util.shuffle(IntStream.rangeClosed(chunkPos.getStartX(), chunkPos.getEndX()), random);
        IntArrayList intArrayList2 = Util.shuffle(IntStream.rangeClosed(chunkPos.getStartZ(), chunkPos.getEndZ()), random);
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (Integer integer : intArrayList) {
            for (Integer integer2 : intArrayList2) {
                mutable.set(integer, 0, integer2);
                BlockPos blockPos = structureWorldAccess.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, mutable);
                if (structureWorldAccess.isAir(blockPos) || structureWorldAccess.getBlockState(blockPos).getCollisionShape(structureWorldAccess, blockPos).isEmpty()) {
                    structureWorldAccess.setBlockState(blockPos, Blocks.CHEST.getDefaultState(), 2);
                    LootableInventory.setLootTable(structureWorldAccess, random, blockPos, RegistryKey.of(RegistryKeys.LOOT_TABLE, context.getConfig().loot));
                    BlockState blockState = Blocks.TORCH.getDefaultState();

                    for (Direction direction : Direction.Type.HORIZONTAL) {
                        BlockPos blockPos2 = blockPos.offset(direction);
                        if (blockState.canPlaceAt(structureWorldAccess, blockPos2)) {
                            structureWorldAccess.setBlockState(blockPos2, blockState, 2);
                        }
                    }

                    return true;
                }
            }
        }
        return false;
    }

    public record Config(Identifier loot) implements FeatureConfig {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                (Identifier.CODEC.fieldOf("loot")).forGetter(a -> a.loot)).apply(
                instance, Config::new));
    }
}
