package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.SingleStateFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class RandomEndIslandFeature extends Feature<SingleStateFeatureConfig> {
    public RandomEndIslandFeature(Codec<SingleStateFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<SingleStateFeatureConfig> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        Random random = context.getRandom();
        BlockPos blockPos = context.getOrigin();
        float f = (float)random.nextInt(3) + 4.0f;
        int i = 0;
        while (f > 0.5f) {
            for (int j = MathHelper.floor(-f); j <= MathHelper.ceil(f); ++j) {
                for (int k = MathHelper.floor(-f); k <= MathHelper.ceil(f); ++k) {
                    if (!((float)(j * j + k * k) <= (f + 1.0f) * (f + 1.0f))) continue;
                    this.setBlockState(structureWorldAccess, blockPos.add(j, i, k), context.getConfig().state);
                }
            }
            f -= (float)random.nextInt(2) + 0.5f;
            --i;
        }
        return true;
    }
}

