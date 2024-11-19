package net.lerariemann.infinity.fluid;

import dev.architectury.core.fluid.ArchitecturyFlowingFluid;
import dev.architectury.core.fluid.SimpleArchitecturyFluidAttributes;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.item.ModItems;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(MOD_ID, RegistryKeys.FLUID);
    public static final SimpleArchitecturyFluidAttributes iridescence_attributes =
            SimpleArchitecturyFluidAttributes.ofSupplier(() -> ModFluids.IRIDESCENCE_FLOWING, () -> ModFluids.IRIDESCENCE_STILL)
                    .blockSupplier(() -> ModBlocks.IRIDESCENCE)
                    .bucketItemSupplier(() -> ModItems.IRIDESCENCE_BUCKET)
                    .sourceTexture(InfinityMod.getId("block/iridescence"))
                    .flowingTexture(InfinityMod.getId("block/iridescence"))
                    .overlayTexture(InfinityMod.getId("block/iridescence"));
    public static final RegistrySupplier<ArchitecturyFlowingFluid> IRIDESCENCE_FLOWING =
            FLUIDS.register("flowing_iridescence", () -> new Iridescence.Flowing(iridescence_attributes));
    public static final RegistrySupplier<ArchitecturyFlowingFluid> IRIDESCENCE_STILL =
            FLUIDS.register("iridescence", () -> new Iridescence.Still(iridescence_attributes));

    public static void registerModFluids() {
        FLUIDS.register();
    }
}
