package net.lerariemann.infinity.fluids;

import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.item.ModItems;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModFluidsForge {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(RegistryKeys.FLUID, MOD_ID);

    public static final RegistryObject<ForgeFlowingFluid.Flowing> IRIDESCENCE_FLOWING =
            FLUIDS.register("flowing_iridescence", () -> new ForgeFlowingFluid.Flowing(ModFluidsForge.iridProp));
    public static final RegistryObject<ForgeFlowingFluid.Source> IRIDESCENCE_STILL =
            FLUIDS.register("iridescence", () -> new ForgeFlowingFluid.Source(ModFluidsForge.iridProp));

    public static ForgeFlowingFluid.Properties iridProp = (new ForgeFlowingFluid.Properties(FluidTypes.IRIDESCENCE_TYPE,
            IRIDESCENCE_STILL, IRIDESCENCE_FLOWING))
            .bucket(ModItems.IRIDESCENCE_BUCKET)
            .block(ModBlocks.IRIDESCENCE);

    public static void registerModFluids(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}
