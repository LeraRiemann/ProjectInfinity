package net.lerariemann.infinity.fluids;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.item.ModItems;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModFluidsForge {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(MOD_ID, RegistryKeys.FLUID);

    public static final RegistrySupplier<ForgeFlowingFluid.Flowing> IRIDESCENCE_FLOWING =
            FLUIDS.register("flowing_iridescence", () -> new ForgeFlowingFluid.Flowing(ModFluidsForge.iridProp));
    public static final RegistrySupplier<ForgeFlowingFluid.Source> IRIDESCENCE_STILL =
            FLUIDS.register("iridescence", () -> new ForgeFlowingFluid.Source(ModFluidsForge.iridProp));

    public static ForgeFlowingFluid.Properties iridProp = (new ForgeFlowingFluid.Properties(FluidTypes.IRIDESCENCE_TYPE,
            IRIDESCENCE_STILL, IRIDESCENCE_FLOWING))
            .bucket(ModItems.IRIDESCENCE_BUCKET)
            .block(ModBlocks.IRIDESCENCE);

    public static void registerModFluids() {
        FLUIDS.register();
    }
}
