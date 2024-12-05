package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomCubeFeature extends Feature<RandomCubeFeature.Config> {
    public RandomCubeFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<Config> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        Random random = context.getRandom();
        BlockPos blockPos = context.getOrigin();
        Map<Integer, BlockState> blocks = new HashMap<>();
        int r = (int)context.getConfig().radius().get(random);
        boolean bl = context.getConfig().useBands();
        if (bl) for (int i = -3*r; i <= 3*r; i++) blocks.put(i, context.getConfig().blockProvider().get(random, blockPos.east(i)));
        for (int i = -r; i <= r; i++) for (int j = -r; j <= r; j++) for (int k = -r; k <= r; k++) {
            BlockPos blockPos1 = blockPos.add(i, j, k);
            if (structureWorldAccess.isAir(blockPos1) || context.getConfig().replaceable().contains(structureWorldAccess.getBlockState(blockPos1)))
                this.setBlockState(structureWorldAccess, blockPos1, bl ? blocks.get(i+j+k) : context.getConfig().blockProvider().get(random, blockPos1));
        }
        return true;
    }

    public record Config(BlockStateProvider blockProvider, List<BlockState> replaceable, FloatProvider radius, boolean useBands) implements FeatureConfig {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                (BlockStateProvider.TYPE_CODEC.fieldOf("block_provider")).forGetter(a -> a.blockProvider),
                (Codec.list(BlockState.CODEC).fieldOf("replaceable")).forGetter(a -> a.replaceable),
                (FloatProvider.createValidatedCodec(2.0f, 20.0f).fieldOf("radius")).forGetter(a -> a.radius),
                (Codec.BOOL.fieldOf("use_bands")).orElse(false).forGetter(a -> a.useBands)).apply(
                instance, Config::new));
    }
}
