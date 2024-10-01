package net.lerariemann.infinity.structure;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.world.gen.structure.StructureType;

public class ModStructureType {
    public static StructurePieceType PYRAMID_PIECE;
    public static StructureType<PyramidStructure> PYRAMID;


    private static StructurePieceType register(StructurePieceType.Simple type, String id) {
        return register((StructurePieceType)type, id);
    }
    private static StructurePieceType register(StructurePieceType type, String id) {
        return Registry.register(Registries.STRUCTURE_PIECE, id, type);
    }

    public static void registerStructures() {
        InfinityMod.LOGGER.debug("Registering processors for " + InfinityMod.MOD_ID);
        PYRAMID = Registry.register(Registries.STRUCTURE_TYPE, InfinityMod.getId("pyramid"), () -> PyramidStructure.CODEC);
        PYRAMID_PIECE = register(PyramidGenerator::new, "infinity:pypiece");
    }
}
