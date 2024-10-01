package net.lerariemann.infinity.var;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.poi.PointOfInterestType;

public class ModPoi {
    public static PointOfInterestType NEITHER_PORTAL;
    public static RegistryKey<PointOfInterestType> NEITHER_PORTAL_KEY;

    @ExpectPlatform
    public static PointOfInterestType register(String name, Block block) {
        throw new AssertionError();
    }
    public static void registerPoi() {
        InfinityMod.LOGGER.debug("Registering POI for " + InfinityMod.MOD_ID);
        NEITHER_PORTAL = register("neither_portal", ModBlocks.NEITHER_PORTAL);
        NEITHER_PORTAL_KEY = RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, InfinityMod.getId("neither_portal"));
    }
}
