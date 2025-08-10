package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.Optional;

public class RandomEndGatewayFeature extends Feature<RandomEndGatewayFeature.Config> {
    public RandomEndGatewayFeature(Codec<RandomEndGatewayFeature.Config> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<RandomEndGatewayFeature.Config> context) {
        BlockPos origin = context.getOrigin();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        RandomEndGatewayFeature.Config config = context.getConfig();
        BlockState bedrock = config.block.orElse(Blocks.BEDROCK.getDefaultState());
        for (BlockPos bp : BlockPos.iterate(origin.add(-1, -2, -1), origin.add(1, 2, 1))) {
            boolean bl = bp.getX() == origin.getX();
            boolean bl2 = bp.getY() == origin.getY();
            boolean bl3 = bp.getZ() == origin.getZ();
            boolean bl4 = Math.abs(bp.getY() - origin.getY()) == 2;
            BlockState localFluid = structureWorldAccess.getFluidState(bp).getBlockState().getBlock().getDefaultState();
            if (bl && bl2 && bl3) {
                BlockPos destination = new BlockPos(bp.getX() + context.getRandom().nextBetween(-config.spread, config.spread),
                        bp.getY(), bp.getZ() + context.getRandom().nextBetween(-config.spread, config.spread));
                destination = structureWorldAccess.getWorldBorder().clamp(destination);
                this.setBlockState(structureWorldAccess, bp, Blocks.END_GATEWAY.getDefaultState());
                if (structureWorldAccess.getBlockEntity(bp) instanceof EndGatewayBlockEntity egbe) {
                    egbe.setExitPortalPos(destination, false);
                }
            } else if (bl2) {
                this.setBlockState(structureWorldAccess, bp, localFluid);
            } else if (bl4 && bl && bl3) {
                this.setBlockState(structureWorldAccess, bp, bedrock);
            } else if ((bl || bl3) && !bl4) {
                this.setBlockState(structureWorldAccess, bp, bedrock);
            } else {
                this.setBlockState(structureWorldAccess, bp, localFluid);
            }
        }

        return true;
    }

    public record Config(int spread, Optional<BlockState> block) implements FeatureConfig {
        public static final Codec<RandomEndGatewayFeature.Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                (Codecs.rangedInt(1, 30000000).fieldOf("spread")).forGetter(a -> a.spread),
                (BlockState.CODEC.optionalFieldOf("block")).forGetter(a -> a.block)).apply(
                instance, RandomEndGatewayFeature.Config::new));
    }
}

