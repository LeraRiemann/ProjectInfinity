package net.lerariemann.infinity.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.BookBoxBlock;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;

public class ModBlocks {
    public static final Block NEITHER_PORTAL = new NeitherPortalBlock(FabricBlockSettings.copy(Blocks.NETHER_PORTAL));
    public static final Block BOOK_BOX = new BookBoxBlock(FabricBlockSettings.copy(Blocks.BOOKSHELF));
    private static void registerBlockWithoutItem(String name, Block block) {
        Registry.register(Registries.BLOCK, new Identifier(InfinityMod.MOD_ID, name), block);
    }
    private static void registerBlockWithItem(String name, Block block) {
        Registry.register(Registries.ITEM, new Identifier(InfinityMod.MOD_ID, name), new BlockItem(block, new FabricItemSettings()));
        registerBlockWithoutItem(name, block);
    }
    public static void registerModBlocks() {
        registerBlockWithoutItem("neither_portal", NEITHER_PORTAL);
        registerBlockWithItem("book_box", BOOK_BOX);
        InfinityMod.LOGGER.debug("Registering ModBlocks for " + InfinityMod.MOD_ID);
    }
}