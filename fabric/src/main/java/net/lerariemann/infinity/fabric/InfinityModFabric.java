package net.lerariemann.infinity.fabric;

import net.fabricmc.api.ModInitializer;
import net.lerariemann.infinity.InfinityMod;

import static net.lerariemann.infinity.entity.ModEntities.registerOtherSpawnRestrictions;
import static net.lerariemann.infinity.entity.ModEntities.registerSpawnRestrictions;

public final class InfinityModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        InfinityMod.init();
        registerSpawnRestrictions();
        registerOtherSpawnRestrictions();
    }
}
