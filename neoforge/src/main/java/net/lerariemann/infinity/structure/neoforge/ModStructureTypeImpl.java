package net.lerariemann.infinity.structure.neoforge;

import net.fabricmc.fabric.mixin.registry.sync.BaseMappedRegistryAccessor;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.structure.PyramidStructure;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.world.gen.structure.StructureType;

public class ModStructureTypeImpl {
    public static StructurePieceType register(StructurePieceType type, String id) {
        ((BaseMappedRegistryAccessor) Registries.STRUCTURE_TYPE).invokeUnfreeze();

        return Registry.register(Registries.STRUCTURE_PIECE, id, type);
    }
    public static StructureType<PyramidStructure> registerPyramid(String id) {
        ((BaseMappedRegistryAccessor) Registries.STRUCTURE_TYPE).invokeUnfreeze();
        return Registry.register(Registries.STRUCTURE_TYPE, InfinityMod.getId(id), () -> PyramidStructure.CODEC);
    }
}
