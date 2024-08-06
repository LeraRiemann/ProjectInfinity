package net.lerariemann.infinity.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntities {
    public static BlockEntityType<NeitherPortalBlockEntity> NEITHER_PORTAL;
    public static BlockEntityType<TransfiniteAltarEntity> ALTAR;
    public static BlockEntityType<CosmicAltarEntity> ALTAR_COSMIC;

    public static void registerBlockEntities() {
        NEITHER_PORTAL = Registry.register(Registries.BLOCK_ENTITY_TYPE,
                InfinityMod.getId("neither_portal"),
                FabricBlockEntityTypeBuilder.create(NeitherPortalBlockEntity::new,
                        ModBlocks.NEITHER_PORTAL).build(null));
        ALTAR = Registry.register(Registries.BLOCK_ENTITY_TYPE,
                InfinityMod.getId("altar_block_entity"),
                FabricBlockEntityTypeBuilder.create(TransfiniteAltarEntity::new, ModBlocks.ALTAR_LIT).build());
        ALTAR_COSMIC = Registry.register(Registries.BLOCK_ENTITY_TYPE,
                InfinityMod.getId("cosmic_block_entity"),
                FabricBlockEntityTypeBuilder.create(CosmicAltarEntity::new, ModBlocks.ALTAR_COSMIC).build());
    }
}