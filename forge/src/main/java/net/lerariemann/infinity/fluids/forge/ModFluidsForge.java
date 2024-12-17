package net.lerariemann.infinity.fluids.forge;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModFluidsForge {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(MOD_ID, RegistryKeys.FLUID);

    public static final RegistrySupplier<ForgeFlowingFluid.Flowing> IRIDESCENCE_FLOWING =
            FLUIDS.register("flowing_iridescence", () -> new ForgeFlowingFluid.Flowing(iridProp()));
    public static final RegistrySupplier<ForgeFlowingFluid.Source> IRIDESCENCE_STILL =
            FLUIDS.register("iridescence", () -> new ForgeFlowingFluid.Source(iridProp()));

    public static ForgeFlowingFluid.Properties iridProp() {
        return (new ForgeFlowingFluid.Properties(FluidTypes.IRIDESCENCE_TYPE,
                IRIDESCENCE_STILL, IRIDESCENCE_FLOWING))
                .bucket(ModItems.IRIDESCENCE_BUCKET)
                .block(ModBlocks.IRIDESCENCE);
    }

    public static void registerModFluids() {
        FLUIDS.register();
    }
}