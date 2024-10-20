package net.lerariemann.infinity.var;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.ModBlocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Collections;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModPoi {
    public static RegistrySupplier<PointOfInterestType> NEITHER_PORTAL;
    public static RegistryKey<PointOfInterestType> NEITHER_PORTAL_KEY;

    public static final DeferredRegister<PointOfInterestType> POI_TYPES = DeferredRegister.create(MOD_ID, RegistryKeys.POINT_OF_INTEREST_TYPE);


    public static void registerPoi() {
        InfinityMod.LOGGER.debug("Registering POI for " + InfinityMod.MOD_ID);
        NEITHER_PORTAL = POI_TYPES.register("neither_portal", () -> new PointOfInterestType(Collections.singleton(ModBlocks.NEITHER_PORTAL.get().getDefaultState()), 0, 1));
        NEITHER_PORTAL_KEY = RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, InfinityMod.getId("neither_portal"));
        POI_TYPES.register();
    }
}