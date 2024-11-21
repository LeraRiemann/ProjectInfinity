package net.lerariemann.infinity.forge;

import dev.architectury.platform.forge.EventBuses;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.fluids.FluidTypes;
import net.lerariemann.infinity.fluids.ModEffectsForge;
import net.lerariemann.infinity.fluids.ModFluidsForge;
import net.lerariemann.infinity.var.ModStats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(InfinityMod.MOD_ID)
public final class InfinityModForge {
    public InfinityModForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(InfinityMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Run our common setup.
        InfinityMod.init();
        // Run our client setup.
        if (FMLEnvironment.dist == Dist.CLIENT) net.lerariemann.infinity.forge.client.InfinityModForgeClient.initializeClient(eventBus);
        // Run any remaining NeoForge specific tasks.
        eventBus.addListener(InfinityModForge::registerSpawns);
        eventBus.addListener(InfinityModForge::loadStats);
        eventBus.addListener(FluidTypes::registerFluidInteractions);
        FluidTypes.registerFluidTypes(eventBus);
        ModFluidsForge.registerModFluids();
        ModEffectsForge.register(eventBus);


    }

    @SubscribeEvent
    public static void registerSpawns(FMLDedicatedServerSetupEvent event) {
        ModEntities.registerSpawnRestrictions();
    }

    @SubscribeEvent
    public static void loadStats(FMLCommonSetupEvent event) {
        ModStats.load();
    }
}
