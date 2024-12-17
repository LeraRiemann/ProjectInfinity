package net.lerariemann.infinity.fluids.neoforge;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModFluidsNeoforge {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(MOD_ID, RegistryKeys.FLUID);

    public static final RegistrySupplier<BaseFlowingFluid.Flowing> IRIDESCENCE_FLOWING =
            FLUIDS.register("flowing_iridescence", () -> new BaseFlowingFluid.Flowing(iridProp()));
    public static final RegistrySupplier<BaseFlowingFluid.Source> IRIDESCENCE_STILL =
            FLUIDS.register("iridescence", () -> new BaseFlowingFluid.Source(iridProp()));

    public static BaseFlowingFluid.Properties iridProp() {
        return (new BaseFlowingFluid.Properties(FluidTypes.IRIDESCENCE_TYPE,
                IRIDESCENCE_STILL, IRIDESCENCE_FLOWING))
                .bucket(ModItems.IRIDESCENCE_BUCKET)
                .block(ModBlocks.IRIDESCENCE);
    }

    public static void registerModFluids() {
        FLUIDS.register();
    }
}
