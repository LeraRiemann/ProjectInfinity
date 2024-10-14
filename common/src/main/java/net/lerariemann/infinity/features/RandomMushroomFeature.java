package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public abstract class RandomMushroomFeature
        extends Feature<RandomMushroomFeatureConfig> {
    public RandomMushroomFeature(Codec<RandomMushroomFeatureConfig> codec) {
        super(codec);
    }

    protected void generateStem(WorldAccess world, Random random, BlockPos pos, RandomMushroomFeatureConfig config, int height, BlockPos.Mutable mutablePos) {
        for (int i = 0; i < height; ++i) {
            mutablePos.set(pos).move(Direction.UP, i);
            if (world.getBlockState(mutablePos).isOpaqueFullCube()) continue;
            this.setBlockState(world, mutablePos, config.stemProvider().get(random, pos));
        }
    }

    protected int getHeight(Random random, int h) {
        int i = random.nextInt(3) + h - 1;
        if (random.nextInt(12) == 0) {
            i *= 2;
        }
        return i;
    }

    protected boolean canGenerate(WorldAccess world, BlockPos pos, int height, BlockPos.Mutable mutablePos, RandomMushroomFeatureConfig config) {
        int i = pos.getY();
        if (i < world.getBottomY() + 1 || i + height + 1 >= world.getTopYInclusive()) {
            return false;
        }
        BlockState blockState = world.getBlockState(pos.down());
        if (!blockState.getBlock().equals(config.validBaseBlock().getBlock())) {
            return false;
        }
        for (int j = 0; j <= height; ++j) {
            int k = this.getCapSize(-1, -1, config.foliageRadius(), j);
            for (int l = -k; l <= k; ++l) {
                for (int m = -k; m <= k; ++m) {
                    BlockState blockState2 = world.getBlockState(mutablePos.set(pos, l, j, m));
                    if (blockState2.isAir() || blockState2.isIn(BlockTags.LEAVES)) continue;
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean generate(FeatureContext<RandomMushroomFeatureConfig> context) {
        BlockPos.Mutable mutable;
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        Random random = context.getRandom();
        RandomMushroomFeatureConfig hugeMushroomFeatureConfig = context.getConfig();
        int i = this.getHeight(random, context.getConfig().height());
        if (!this.canGenerate(structureWorldAccess, blockPos, i, mutable = new BlockPos.Mutable(), hugeMushroomFeatureConfig)) {
            return false;
        }
        this.generateCap(structureWorldAccess, random, blockPos, i, mutable, hugeMushroomFeatureConfig);
        this.generateStem(structureWorldAccess, random, blockPos, hugeMushroomFeatureConfig, i, mutable);
        return true;
    }

    protected abstract int getCapSize(int var1, int var2, int var3, int var4);

    protected abstract void generateCap(WorldAccess var1, Random var2, BlockPos var3, int var4, BlockPos.Mutable var5, RandomMushroomFeatureConfig var6);
}

