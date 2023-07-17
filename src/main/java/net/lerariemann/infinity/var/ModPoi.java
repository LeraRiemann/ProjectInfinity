package net.lerariemann.infinity.var;

import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

public class ModPoi {
    public static PointOfInterestType NEITHER_PORTAL;
    public static RegistryKey<PointOfInterestType> NEITHER_PORTAL_KEY;

    public static PointOfInterestType register(String name, Block block) {
        return PointOfInterestHelper.register(new Identifier(InfinityMod.MOD_ID, name), 0, 1, ImmutableSet.copyOf(block.getStateManager().getStates()));
    }
    public static void registerPoi() {
        InfinityMod.LOGGER.debug("Registering POI for " + InfinityMod.MOD_ID);
        NEITHER_PORTAL = register("neither_portal", ModBlocks.NEITHER_PORTAL);
        NEITHER_PORTAL_KEY = RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, new Identifier(InfinityMod.MOD_ID, "neither_portal"));
    }
}
