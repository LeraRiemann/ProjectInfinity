package net.lerariemann.infinity.block.entity;

import com.mojang.datafixers.types.Type;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Util;

import static net.lerariemann.infinity.PlatformMethods.unfreeze;

public class ModBlockEntities {

    public static Type<?> type(String id) {
        return Util.getChoiceType(TypeReferences.BLOCK_ENTITY, id);
    }

    public static BlockEntityType<NeitherPortalBlockEntity> NEITHER_PORTAL = BlockEntityType.Builder.create(NeitherPortalBlockEntity::new, ModBlocks.NEITHER_PORTAL).build(type("neither_portal"));
    public static BlockEntityType<TransfiniteAltarEntity> ALTAR = BlockEntityType.Builder.create(TransfiniteAltarEntity::new, ModBlocks.ALTAR_LIT).build(type("altar_block_entity"));
    public static BlockEntityType<CosmicAltarEntity> ALTAR_COSMIC = BlockEntityType.Builder.create(CosmicAltarEntity::new, ModBlocks.ALTAR_COSMIC).build(type("cosmic_block_entity"));

    public static void registerBlockEntities() {
        unfreeze(Registries.BLOCK_ENTITY_TYPE);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, InfinityMod.getId("neither_portal"),NEITHER_PORTAL);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, InfinityMod.getId("altar_block_entity"), ALTAR);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, InfinityMod.getId("cosmic_block_entity"), ALTAR_COSMIC);
    }
}