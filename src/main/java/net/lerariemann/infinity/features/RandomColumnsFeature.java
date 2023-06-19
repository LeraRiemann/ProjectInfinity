package net.lerariemann.infinity.features;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.jetbrains.annotations.Nullable;

public class RandomColumnsFeature
        extends Feature<RandomColumnsFeatureConfig> {
    private static final ImmutableList<Block> CANNOT_REPLACE_BLOCKS = ImmutableList.of(Blocks.LAVA, Blocks.BEDROCK, Blocks.WATER, Blocks.END_GATEWAY, Blocks.CHEST, Blocks.SPAWNER);

    public RandomColumnsFeature(Codec<RandomColumnsFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<RandomColumnsFeatureConfig> context) {
        int i = context.getGenerator().getSeaLevel();
        BlockPos blockPos = context.getOrigin();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        Random random = context.getRandom();
        RandomColumnsFeatureConfig randomColumnsFeatureConfig = context.getConfig();
        if (!RandomColumnsFeature.canPlaceAt(structureWorldAccess, i, blockPos.mutableCopy())) {
            return false;
        }
        int j = randomColumnsFeatureConfig.height().get(random);
        boolean bl = random.nextFloat() < 0.9f;
        int k = Math.min(j, bl ? 5 : 8);
        int l = bl ? 50 : 15;
        boolean bl2 = false;
        for (BlockPos blockPos2 : BlockPos.iterateRandomly(random, l, blockPos.getX() - k, blockPos.getY(), blockPos.getZ() - k, blockPos.getX() + k, blockPos.getY(), blockPos.getZ() + k)) {
            int m = j - blockPos2.getManhattanDistance(blockPos);
            if (m < 0) continue;
            BlockState state = context.getConfig().block().get(random, blockPos2);
            bl2 |= this.placeBasaltColumn(structureWorldAccess, i, blockPos2, m, randomColumnsFeatureConfig.reach().get(random), state);
        }
        return bl2;
    }

    private boolean placeBasaltColumn(WorldAccess world, int seaLevel, BlockPos pos, int height, int reach, BlockState state) {
        boolean bl = false;
        block0: for (BlockPos blockPos : BlockPos.iterate(pos.getX() - reach, pos.getY(), pos.getZ() - reach,
                pos.getX() + reach, pos.getY(), pos.getZ() + reach)) {
            int i = blockPos.getManhattanDistance(pos);
            BlockPos blockPos2 = RandomColumnsFeature.isAirOrOcean(world, seaLevel, blockPos) ?
                    RandomColumnsFeature.moveDownToGround(world, seaLevel, blockPos.mutableCopy(), i) :
                    RandomColumnsFeature.moveUpToAir(world, blockPos.mutableCopy(), i);
            if (blockPos2 == null) continue;
            BlockPos.Mutable mutable = blockPos2.mutableCopy();
            for (int j = height - i / 2; j >= 0; --j) {
                if (RandomColumnsFeature.isAirOrOcean(world, seaLevel, mutable)) {
                    this.setBlockState(world, mutable, state);
                    mutable.move(Direction.UP);
                    bl = true;
                    continue;
                }
                if (!world.getBlockState(mutable).isOf(state.getBlock())) continue block0;
                mutable.move(Direction.UP);
            }
        }
        return bl;
    }

    @Nullable
    private static BlockPos moveDownToGround(WorldAccess world, int seaLevel, BlockPos.Mutable mutablePos, int distance) {
        while (mutablePos.getY() > world.getBottomY() + 1 && distance > 0) {
            --distance;
            if (RandomColumnsFeature.canPlaceAt(world, seaLevel, mutablePos)) {
                return mutablePos;
            }
            mutablePos.move(Direction.DOWN);
        }
        return null;
    }

    private static boolean canPlaceAt(WorldAccess world, int seaLevel, BlockPos.Mutable mutablePos) {
        if (RandomColumnsFeature.isAirOrOcean(world, seaLevel, mutablePos)) {
            BlockState blockState = world.getBlockState(mutablePos.move(Direction.DOWN));
            mutablePos.move(Direction.UP);
            return !blockState.isAir() && !CANNOT_REPLACE_BLOCKS.contains(blockState.getBlock());
        }
        return false;
    }

    @Nullable
    private static BlockPos moveUpToAir(WorldAccess world, BlockPos.Mutable mutablePos, int distance) {
        while (mutablePos.getY() < world.getTopY() && distance > 0) {
            --distance;
            BlockState blockState = world.getBlockState(mutablePos);
            if (CANNOT_REPLACE_BLOCKS.contains(blockState.getBlock())) {
                return null;
            }
            if (blockState.isAir()) {
                return mutablePos;
            }
            mutablePos.move(Direction.UP);
        }
        return null;
    }

    private static boolean isAirOrOcean(WorldAccess world, int seaLevel, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.isAir() || (blockState.isOf(Blocks.LAVA) || blockState.isOf(Blocks.WATER)) && pos.getY() <= seaLevel;
    }
}

