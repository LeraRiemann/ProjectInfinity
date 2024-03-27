package net.lerariemann.infinity.structure;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.structure.StructureType;

public class ModStructureType {
    public static StructureType<RandomPortalStructure> PORTAL;

    public static void registerStructures() {
        InfinityMod.LOGGER.debug("Registering processors for " + InfinityMod.MOD_ID);
        PORTAL = Registry.register(Registries.STRUCTURE_TYPE, InfinityMod.getId("portal"), () -> RandomPortalStructure.CODEC);
    }
}
