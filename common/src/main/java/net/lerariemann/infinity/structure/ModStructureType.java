package net.lerariemann.infinity.structure;

import dev.architectury.injectables.annotations.ExpectPlatform;
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

    @ExpectPlatform
    public static StructurePieceType register(StructurePieceType type, String id) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static StructureType<PyramidStructure> registerPyramid(String id) {
        throw new AssertionError();
    }

    public static void registerStructures() {
        InfinityMod.LOGGER.debug("Registering processors for " + InfinityMod.MOD_ID);
        PYRAMID = registerPyramid("pyramid");
        PYRAMID_PIECE = register(PyramidGenerator::new, "infinity:pypiece");
        Registries.STRUCTURE_TYPE.freeze();
        Registries.STRUCTURE_PIECE.freeze();

    }
}
