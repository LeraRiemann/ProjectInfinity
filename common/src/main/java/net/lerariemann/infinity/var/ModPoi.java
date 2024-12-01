package net.lerariemann.infinity.var;

import com.google.common.collect.ImmutableSet;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.ModBlocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.poi.PointOfInterestType;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModPoi {
    public static RegistrySupplier<PointOfInterestType> NEITHER_PORTAL;
    public static RegistryKey<PointOfInterestType> NEITHER_PORTAL_KEY;

    public static void registerPoi() {
        InfinityMod.LOGGER.debug("Registering POI for " + InfinityMod.MOD_ID);
        if (Platform.isFabric()) registerPoiFabric();
        else registerPoiArchitectury();
        NEITHER_PORTAL_KEY = RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, InfinityMod.getId("neither_portal"));

    }

    // Deferred handling of Poi through Architectury API, works on NeoForge.
    public static void registerPoiArchitectury() {
        final DeferredRegister<PointOfInterestType> POI_TYPES = DeferredRegister.create(MOD_ID, RegistryKeys.POINT_OF_INTEREST_TYPE);
        NEITHER_PORTAL = POI_TYPES.register("neither_portal", () -> new PointOfInterestType(ImmutableSet.copyOf(ModBlocks.PORTAL.get().getStateManager().getStates()), 0, 1));
        POI_TYPES.register();
    }

    // Legacy handling of Poi on Fabric, as Architectury Poi are not correctly registered.
    @ExpectPlatform
    public static void registerPoiFabric() {
        throw new AssertionError();
    }
}