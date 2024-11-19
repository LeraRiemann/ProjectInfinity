package net.lerariemann.infinity.fluid;

import dev.architectury.core.fluid.ArchitecturyFlowingFluid;
import dev.architectury.core.fluid.ArchitecturyFluidAttributes;
import net.minecraft.fluid.FluidState;
import net.minecraft.world.World;

public class Iridescence {
    public static boolean isInfinite(World world) {
        return world.getRegistryKey().getValue().toString().equals("infinity:chaos");
    }
    public static boolean isIridescence(FluidState st) {
        return st.isOf(ModFluids.IRIDESCENCE_FLOWING.get()) || st.isOf(ModFluids.IRIDESCENCE_STILL.get());
    }

    public static class Still extends ArchitecturyFlowingFluid.Source {
        public Still(ArchitecturyFluidAttributes attributes) {
            super(attributes);
        }

        @Override
        protected boolean isInfinite(World world) {
            return Iridescence.isInfinite(world);
        }
    }

    public static class Flowing extends ArchitecturyFlowingFluid.Flowing {
        public Flowing(ArchitecturyFluidAttributes attributes) {
            super(attributes);
        }

        @Override
        protected boolean isInfinite(World world) {
            return Iridescence.isInfinite(world);
        }
    }
}
