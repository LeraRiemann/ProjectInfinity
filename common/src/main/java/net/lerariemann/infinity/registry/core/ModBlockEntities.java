package net.lerariemann.infinity.registry.core;

import com.mojang.datafixers.types.Type;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.RailHelper;
import net.lerariemann.infinity.block.entity.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Util;

public class ModBlockEntities {
    public static Type<?> type(String id) {
        if (!Platform.isFabric())
            return Util.getChoiceType(TypeReferences.BLOCK_ENTITY, "infinity:"+ id);
        else return null;
    }
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(InfinityMod.MOD_ID, RegistryKeys.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<InfinityPortalBlockEntity>> NEITHER_PORTAL =
            BLOCK_ENTITY_TYPES.register("neither_portal", () ->
                    BlockEntityType.Builder.create(InfinityPortalBlockEntity::new,
                            ModBlocks.PORTAL.get()).build(type("neither_portal")));
    public static final RegistrySupplier<BlockEntityType<CosmicAltarBlockEntity>> COSMIC_ALTAR =
            BLOCK_ENTITY_TYPES.register("cosmic_altar", () ->
                    BlockEntityType.Builder.create(CosmicAltarBlockEntity::new,
                            ModBlocks.COSMIC_ALTAR.get()).build(type("cosmic_altar")));
    public static final RegistrySupplier<BlockEntityType<BiomeBottleBlockEntity>> BIOME_BOTTLE =
            BLOCK_ENTITY_TYPES.register("biome_bottle", () ->
                    BlockEntityType.Builder.create(BiomeBottleBlockEntity::new,
                            ModBlocks.BIOME_BOTTLE.get()).build(type("biome_bottle")));
    public static final RegistrySupplier<BlockEntityType<ChromaticBlockEntity>> CHROMATIC =
            BLOCK_ENTITY_TYPES.register("chromatic", () ->
                    BlockEntityType.Builder.create(ChromaticBlockEntity::new,
                            ModBlocks.CHROMATIC_WOOL.get(),
                            ModBlocks.CHROMATIC_CARPET.get()).build(type("chromatic")));
    public static final RegistrySupplier<BlockEntityType<RailHelper.RHBEntity>> RAIL_HELPER =
            BLOCK_ENTITY_TYPES.register("rail_helper_block_entity", () ->
                    BlockEntityType.Builder.create(RailHelper.RHBEntity::new,
                            ModBlocks.RAIL_HELPER.get()).build(type("rail_helper_block_entity")));


    public static void registerBlockEntities() {
        BLOCK_ENTITY_TYPES.register();
    }
}