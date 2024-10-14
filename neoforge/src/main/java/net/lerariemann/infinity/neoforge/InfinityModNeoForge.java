package net.lerariemann.infinity.neoforge;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.InfinityModClient;
import net.lerariemann.infinity.neoforge.client.InfinityModNeoForgeClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

import static net.lerariemann.infinity.entity.ModEntities.*;

@Mod(InfinityMod.MOD_ID)
public final class InfinityModNeoForge {
    public InfinityModNeoForge(IEventBus eventBus, ModContainer container) {
        // Run our common setup.
        InfinityMod.init();
        // Run our client setup.
        if (FMLEnvironment.dist == Dist.CLIENT) InfinityModNeoForgeClient.initializeClient(eventBus);
        // Run any remaining NeoForge specific tasks.
        eventBus.addListener(InfinityModNeoForge::registerSpawns);
    }

    @SubscribeEvent
    public static void registerSpawns(RegisterSpawnPlacementsEvent event) {
        registerSpawnRestrictions();
        registerOtherSpawnRestrictions();
    }
}
