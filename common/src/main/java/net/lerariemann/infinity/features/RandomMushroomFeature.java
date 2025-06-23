package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public abstract class RandomMushroomFeature
        extends Feature<RandomMushroomFeature.Config> {
    public RandomMushroomFeature(Codec<Config> codec) {
        super(codec);
    }

    protected void generateStem(WorldAccess world, Random random, BlockPos pos, Config config, int height, BlockPos.Mutable mutablePos) {
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

    protected boolean canGenerate(WorldAccess world, BlockPos pos, int height, BlockPos.Mutable mutablePos, Config config) {
        int i = pos.getY();
        if (i < world.getBottomY() + 1 || i + height + 1 >= world.getHeight()   ) {
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
    public boolean generate(FeatureContext<Config> context) {
        BlockPos.Mutable mutable;
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        Random random = context.getRandom();
        Config hugeMushroomFeatureConfig = context.getConfig();
        int i = this.getHeight(random, context.getConfig().height());
        if (!this.canGenerate(structureWorldAccess, blockPos, i, mutable = new BlockPos.Mutable(), hugeMushroomFeatureConfig)) {
            return false;
        }
        this.generateCap(structureWorldAccess, random, blockPos, i, mutable, hugeMushroomFeatureConfig);
        this.generateStem(structureWorldAccess, random, blockPos, hugeMushroomFeatureConfig, i, mutable);
        return true;
    }

    protected abstract int getCapSize(int var1, int var2, int var3, int var4);

    protected abstract void generateCap(WorldAccess var1, Random var2, BlockPos var3, int var4, BlockPos.Mutable var5, Config var6);

    public record Config(BlockStateProvider capProvider, BlockStateProvider stemProvider,
                                              BlockState validBaseBlock, int foliageRadius,
                                              int height) implements FeatureConfig {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                (BlockStateProvider.TYPE_CODEC.fieldOf("cap_provider")).forGetter(a -> a.capProvider),
                (BlockStateProvider.TYPE_CODEC.fieldOf("stem_provider")).forGetter(a -> a.stemProvider),
                (BlockState.CODEC.fieldOf("valid_base_block")).forGetter(a -> a.validBaseBlock),
                (Codec.INT.fieldOf("foliage_radius")).orElse(2).forGetter(a -> a.foliageRadius),
                (Codec.INT.fieldOf("height")).orElse(5).forGetter(a -> a.height)).apply(
                instance, Config::new));
    }
}

