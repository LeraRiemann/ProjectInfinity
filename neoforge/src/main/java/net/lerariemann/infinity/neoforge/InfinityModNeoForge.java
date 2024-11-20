package net.lerariemann.infinity.neoforge;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.fluids.FluidTypes;
import net.lerariemann.infinity.fluids.ModEffectsNeoforge;
import net.lerariemann.infinity.fluids.ModFluidsNeoforge;
import net.lerariemann.infinity.neoforge.client.InfinityModNeoForgeClient;
import net.lerariemann.infinity.var.ModStats;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@Mod(InfinityMod.MOD_ID)
public final class InfinityModNeoForge {
    public InfinityModNeoForge(IEventBus eventBus, ModContainer container) {
        // Run our common setup.
        InfinityMod.init();
        // Run our client setup.
        if (FMLEnvironment.dist == Dist.CLIENT) InfinityModNeoForgeClient.initializeClient(eventBus);
        // Run any remaining tasks that require waiting for the registry to freeze on NeoForge.
        eventBus.addListener(InfinityModNeoForge::registerSpawns);
        eventBus.addListener(InfinityModNeoForge::loadStats);
        eventBus.addListener(FluidTypes::registerFluidInteractions);
        eventBus.addListener(ModEffectsNeoforge::register);

        FluidTypes.registerFluidTypes(eventBus);
        ModFluidsNeoforge.registerModFluids();
    }

    @SubscribeEvent
    public static void registerSpawns(RegisterSpawnPlacementsEvent event) {
        ModEntities.registerSpawnRestrictions();
    }

    @SubscribeEvent
    public static void loadStats(FMLCommonSetupEvent event) {
        ModStats.load();
    }

}
