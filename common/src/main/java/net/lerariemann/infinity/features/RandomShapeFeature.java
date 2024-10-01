package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RandomShapeFeature extends Feature<RandomShapeFeatureConfig> {
    public RandomShapeFeature(Codec<RandomShapeFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<RandomShapeFeatureConfig> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        Random random = context.getRandom();
        BlockPos blockPos = context.getOrigin();
        Map<Integer, BlockState> blocks = new HashMap<>();
        int r = (int)context.getConfig().radius().get(random);
        boolean bl = context.getConfig().useBands();
        if (bl) for (int i = -3*r; i <= 3*r; i++) blocks.put(i, context.getConfig().blockProvider().get(random, blockPos.east(i)));
        double p = context.getConfig().pow();
        double ia = 0.0;
        double ra = Math.pow(r, p);
        for (int i = 0; ia < ra; i++) {
            double ja = 0.0;
            for (int j = 0; ia + ja < ra; j++) {
                double ka = 0.0;
                for (int k = 0; ia + ja + ka < ra; k++) {
                    for (Vec3i v : signs(i, j, k)) {
                        BlockPos blockPos1 = blockPos.add(v);
                        if (structureWorldAccess.isAir(blockPos1) || context.getConfig().replaceable().contains(structureWorldAccess.getBlockState(blockPos1)))
                            this.setBlockState(structureWorldAccess, blockPos1, bl ? blocks.get(v.getX() + v.getY() + v.getZ()) :
                                    context.getConfig().blockProvider().get(random, blockPos1));
                    }
                    ka = Math.pow(k+1, p);
                }
                ja = Math.pow(j+1, p);
            }
            ia = Math.pow(i+1, p);
        }
        return true;
    }

    static HashSet<Vec3i> signs(int i, int j, int k) {
        HashSet<Vec3i> res = new HashSet<>();
        res.add(new Vec3i(i, j, k));
        res.add(new Vec3i(i, j, -k));
        res.add(new Vec3i(i, -j, k));
        res.add(new Vec3i(i, -j, -k));
        res.add(new Vec3i(-i, j, k));
        res.add(new Vec3i(-i, j, -k));
        res.add(new Vec3i(-i, -j, k));
        res.add(new Vec3i(-i, -j, -k));
        return res;
    }
}
