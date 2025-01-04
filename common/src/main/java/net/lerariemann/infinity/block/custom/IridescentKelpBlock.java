package net.lerariemann.infinity.block.custom;

import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.util.PlatformMethods;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;

public class IridescentKelpBlock extends KelpBlock {
    public IridescentKelpBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean chooseStemState(BlockState state) {
        return state.isOf(ModBlocks.IRIDESCENCE.get());
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return PlatformMethods.getIridescenceStill().get().getStill(false);
    }

    @Override
    protected Block getPlant() {
        return ModBlocks.IRIDESCENT_KELP_PLANT.get();
    }

    public static class Plant extends KelpPlantBlock {
        public Plant(Settings settings) {
            super(settings);
        }

        @Override
        public FluidState getFluidState(BlockState state) {
            return PlatformMethods.getIridescenceStill().get().getStill(false);
        }

        @Override
        protected AbstractPlantStemBlock getStem() {
            return ModBlocks.IRIDESCENT_KELP.get();
        }
    }
}
