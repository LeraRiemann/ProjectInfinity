package net.lerariemann.infinity.neoforge;

import net.lerariemann.infinity.InfinityMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

import static net.lerariemann.infinity.entity.ModEntities.*;

@Mod(InfinityMod.MOD_ID)
public final class InfinityModNeoForge {
    public InfinityModNeoForge() {
        // Run our common setup.
        InfinityMod.init();
    }

    @SubscribeEvent
    public static void registerSpawns(RegisterSpawnPlacementsEvent event) {
        registerSpawnRestrictions();
        registerOtherSpawnRestrictions();
    }
}
