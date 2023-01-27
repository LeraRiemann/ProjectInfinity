package net.lerariemann.infinity.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;

public class ModBlockEntities {
    public static BlockEntityType<NeitherPortalBlockEntity> NEITHER_PORTAL;

    public static void registerBlockEntities() {
        NEITHER_PORTAL = Registry.register(Registries.BLOCK_ENTITY_TYPE,
                new Identifier(InfinityMod.MOD_ID, "neither_portal"),
                FabricBlockEntityTypeBuilder.create(NeitherPortalBlockEntity::new,
                        ModBlocks.NEITHER_PORTAL).build(null));
    }
}