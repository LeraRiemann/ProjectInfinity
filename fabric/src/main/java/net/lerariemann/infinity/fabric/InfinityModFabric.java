package net.lerariemann.infinity.fabric;

import dev.architectury.platform.Platform;
import net.fabricmc.api.ModInitializer;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.compat.CreateCompat;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModEntities;
import net.lerariemann.infinity.fluids.fabric.ModFluidsFabric;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.registry.var.ModStats;

import static net.lerariemann.infinity.InfinityMod.LOGGER;

public final class InfinityModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        ModFluidsFabric.registerModFluids();
        InfinityMod.init();
        // Run any remaining tasks that require waiting for the registry to freeze on NeoForge.
        ModEntities.registerSpawnRestrictions();
        if (Platform.isModLoaded("create"))
            CreateCompat.register();
        ModStats.load();
        ModBlocks.registerFlammableBlocks();
        ModItemFunctions.registerDispenserBehaviour();
        if (Platform.isModLoaded("edenring"))
            LOGGER.info("Eden Ring is installed, dimensions will not have custom gravity!");
    }
}
