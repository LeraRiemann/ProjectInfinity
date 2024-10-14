package net.lerariemann.infinity.structure.fabric;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.structure.PyramidStructure;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.world.gen.structure.StructureType;
/**
 * See {@link net.lerariemann.infinity.structure.ModStructureType} for usages.
 */
@SuppressWarnings("unused")
public class ModStructureTypeImpl {
    private static StructurePieceType register(StructurePieceType.Simple type, String id) {
        return register((StructurePieceType)type, id);
    }

    public static StructurePieceType register(StructurePieceType type, String id) {
        return Registry.register(Registries.STRUCTURE_PIECE, id, type);
    }
    public static StructureType<PyramidStructure> registerPyramid(String id) {
        return Registry.register(Registries.STRUCTURE_TYPE, InfinityMod.getId(id), () -> PyramidStructure.CODEC);
    }
}
