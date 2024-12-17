package net.lerariemann.infinity.registry.core;

import com.mojang.datafixers.types.Type;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.entity.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Util;

public class ModBlockEntities {

    public static Type<?> type(String id) {
        return Util.getChoiceType(TypeReferences.BLOCK_ENTITY, id);
    }
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(InfinityMod.MOD_ID, RegistryKeys.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<InfinityPortalBlockEntity>> NEITHER_PORTAL =
            BLOCK_ENTITY_TYPES.register("neither_portal", () ->
                    BlockEntityType.Builder.create(InfinityPortalBlockEntity::new,
                            ModBlocks.PORTAL.get()).build(type("neither_portal")));
    public static final RegistrySupplier<BlockEntityType<TransfiniteAltarEntity>> ALTAR =
            BLOCK_ENTITY_TYPES.register("altar_block_entity", () ->
                    BlockEntityType.Builder.create(TransfiniteAltarEntity::new,
                            ModBlocks.ALTAR.get()).build(type("altar_block_entity")));
    public static final RegistrySupplier<BlockEntityType<CosmicAltarEntity>> ALTAR_COSMIC =
            BLOCK_ENTITY_TYPES.register("cosmic_block_entity", () ->
                    BlockEntityType.Builder.create(CosmicAltarEntity::new,
                            ModBlocks.ALTAR_COSMIC.get()).build(type("cosmic_block_entity")));
    public static final RegistrySupplier<BlockEntityType<BiomeBottleBlockEntity>> BIOME_BOTTLE =
            BLOCK_ENTITY_TYPES.register("biome_bottle_block_entity", () ->
                    BlockEntityType.Builder.create(BiomeBottleBlockEntity::new,
                            ModBlocks.BIOME_BOTTLE.get()).build(type("biome_bottle_block_entity")));


    public static void registerBlockEntities() {
        BLOCK_ENTITY_TYPES.register();
    }
}