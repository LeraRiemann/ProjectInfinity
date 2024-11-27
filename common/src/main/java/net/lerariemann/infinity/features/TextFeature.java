package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.var.ModMaterialConditions;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.List;

public class TextFeature extends Feature<TextFeature.Config> {
    public TextFeature(Codec<Config> codec) {
        super(codec);
    }

    BlockPos mutate(BlockPos blockPos, int ori, int a, int b) {
        if (((ori/6)%2) == 1) a*=-1;
        if (((ori/12)%2) == 1) b*=-1;
        List<Integer> lst = switch (ori % 6) {
            case 0 -> List.of(0, a, b);
            case 1 -> List.of(b, 0, a);
            case 2 -> List.of(a, b, 0);
            case 3 -> List.of(0, b, a);
            case 4 -> List.of(a, 0, b);
            default -> List.of(b, a, 0);
        };
        return blockPos.add(lst.get(0), lst.get(1), lst.get(2));
    }

    @Override
    public boolean generate(FeatureContext<Config> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        Random random = context.getRandom();
        BlockPos blockPos = context.getOrigin();
        blockPos = mutate(blockPos, context.getConfig().orientation(), 0, -16);
        String text = context.getConfig().text();
        int i2 = 8 + random.nextInt(16);
        if (text.length() > i2) {
            int i1 = random.nextInt(text.length() - i2);
            text = text.substring(i1, i1 + i2);
        }
        int i, j, k;
        int len = 0;
        for (i = 0; i < text.length(); i++) {
            List<Integer> lst = ModMaterialConditions.TextCondition.storage.get(text.charAt(i));
            if (lst != null) {
                if (!structureWorldAccess.isChunkLoaded(mutate(blockPos, context.getConfig().orientation(), 0, len + lst.size() - 1))) break;
                for (j = 0; j < lst.size(); j++) {
                    for (k = 0; k < 8; k++) {
                        if ((lst.get(j) >> k)%2 == 1) {
                            BlockPos blockPos1 = mutate(blockPos, context.getConfig().orientation(), -k, len + j);
                            if ((structureWorldAccess.isAir(blockPos1) || context.getConfig().replaceable().contains(structureWorldAccess.getBlockState(blockPos1))))
                                this.setBlockState(structureWorldAccess, blockPos1, context.getConfig().blockProvider().get(random, blockPos1));
                        }
                    }
                }
                len += lst.size() + context.getConfig().spacing();
            }
        }
        return true;
    }

    public record Config(BlockStateProvider blockProvider, List<BlockState> replaceable, int orientation, int spacing, String text) implements FeatureConfig {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                (BlockStateProvider.TYPE_CODEC.fieldOf("block_provider")).forGetter(a -> a.blockProvider),
                (Codec.list(BlockState.CODEC).fieldOf("replaceable")).forGetter(a -> a.replaceable),
                (Codec.INT.fieldOf("orientation")).orElse(2).forGetter(a -> a.orientation),
                (Codec.INT.fieldOf("spacing")).orElse(1).forGetter(a -> a.spacing),
                (Codec.STRING.fieldOf("text")).forGetter(a -> a.text)).apply(instance, Config::new));
    }
}
