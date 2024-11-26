package net.lerariemann.infinity.fabric;

import dev.architectury.platform.Platform;
import net.fabricmc.api.ModInitializer;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.compat.fabric.CreateFabricCompat;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.fluids.ModFluidsFabric;
import net.lerariemann.infinity.var.ModStats;

public final class  InfinityModFabric implements ModInitializer {
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
            CreateFabricCompat.register();
        ModStats.load();
    }
}
