package net.lerariemann.infinity.structure;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.world.gen.structure.StructureType;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModStructureType {
    public static RegistrySupplier<StructurePieceType> PYRAMID_PIECE;
    public static RegistrySupplier<StructureType<PyramidStructure>> PYRAMID;
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(MOD_ID, RegistryKeys.STRUCTURE_TYPE);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(MOD_ID, RegistryKeys.STRUCTURE_PIECE);

    private static RegistrySupplier<StructurePieceType> register(StructurePieceType.Simple type, String id) {
        return register((StructurePieceType)type, id);
    }
    private static RegistrySupplier<StructurePieceType> register(StructurePieceType type, String id) {
        return STRUCTURE_PIECES.register(id, () -> type);
    }

    public static void registerStructures() {
        InfinityMod.LOGGER.debug("Registering processors for " + InfinityMod.MOD_ID);

        PYRAMID = STRUCTURE_TYPES.register("pyramid", () -> () -> PyramidStructure.CODEC);
        PYRAMID_PIECE = register(PyramidGenerator::new, "pypiece");

        STRUCTURE_PIECES.register();
        STRUCTURE_TYPES.register();
    }









}