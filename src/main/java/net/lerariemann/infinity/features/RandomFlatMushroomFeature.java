package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;

public class RandomFlatMushroomFeature extends RandomMushroomFeature {
    public RandomFlatMushroomFeature(Codec<RandomMushroomFeatureConfig> codec) {
        super(codec);
    }

    @Override
    protected void generateCap(WorldAccess world, Random random, BlockPos start, int y, BlockPos.Mutable mutable, RandomMushroomFeatureConfig config) {
        int i = config.foliageRadius();
        for (int j = -i; j <= i; ++j) {
            for (int k = -i; k <= i; ++k) {
                boolean bl = j == -i;
                boolean bl2 = j == i;
                boolean bl3 = k == -i;
                boolean bl4 = k == i;
                boolean bl5 = bl || bl2;
                boolean bl6 = bl3 || bl4;
                if (bl5 && bl6) continue;
                mutable.set(start, j, y, k);
                if (world.getBlockState(mutable).isOpaqueFullCube(world, mutable)) continue;
                BlockState blockState = config.capProvider().get(random, start);
                this.setBlockState(world, mutable, blockState);
            }
        }
    }

    @Override
    protected int getCapSize(int i, int j, int capSize, int y) {
        return y <= 3 ? 0 : capSize;
    }
}
