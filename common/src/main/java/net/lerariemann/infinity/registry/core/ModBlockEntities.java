package net.lerariemann.infinity.registry.core;

import com.mojang.datafixers.types.Type;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.entity.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Util;

import java.util.Set;

public class ModBlockEntities {
    public static Type<?> type(String id) {
        if (!Platform.isFabric())
            return Util.getChoiceType(TypeReferences.BLOCK_ENTITY, "infinity:"+ id);
        else return null;
    }
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(InfinityMod.MOD_ID, RegistryKeys.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<InfinityPortalBlockEntity>> NEITHER_PORTAL =
            BLOCK_ENTITY_TYPES.register("neither_portal", () ->
                    new BlockEntityType<>(InfinityPortalBlockEntity::new,
                            Set.of(ModBlocks.PORTAL.get())));
    public static final RegistrySupplier<BlockEntityType<CosmicAltarBlockEntity>> COSMIC_ALTAR =
            BLOCK_ENTITY_TYPES.register("cosmic_altar", () ->
                    new BlockEntityType<>(CosmicAltarBlockEntity::new,
                            Set.of(ModBlocks.COSMIC_ALTAR.get())));
    public static final RegistrySupplier<BlockEntityType<BiomeBottleBlockEntity>> BIOME_BOTTLE =
            BLOCK_ENTITY_TYPES.register("biome_bottle", () ->
                    new BlockEntityType<>(BiomeBottleBlockEntity::new,
                            Set.of(ModBlocks.BIOME_BOTTLE.get())));
    public static final RegistrySupplier<BlockEntityType<ChromaticBlockEntity>> CHROMATIC =
            BLOCK_ENTITY_TYPES.register("chromatic", () ->
                    new BlockEntityType<>(ChromaticBlockEntity::new, Set.of(ModBlocks.CHROMATIC_WOOL.get(),
                            ModBlocks.CHROMATIC_CARPET.get())));


    public static void registerBlockEntities() {
        BLOCK_ENTITY_TYPES.register();
    }
}