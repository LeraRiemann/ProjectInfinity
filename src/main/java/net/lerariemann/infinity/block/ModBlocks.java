package net.lerariemann.infinity.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.BookBoxBlock;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;

public class ModBlocks {
    public static final Block NEITHER_PORTAL = new NeitherPortalBlock(FabricBlockSettings.copy(Blocks.NETHER_PORTAL));
    public static final Block BOOK_BOX = new BookBoxBlock(FabricBlockSettings.copy(Blocks.BOOKSHELF));
    public static final Item BOOK_BOX_ITEM = new BlockItem(BOOK_BOX, new FabricItemSettings());
    private static void registerBlock(String name, Block block) {
        Registry.register(Registries.BLOCK, new Identifier(InfinityMod.MOD_ID, name), block);
    }
    private static void registerItem(String name, Item item) {
        Registry.register(Registries.ITEM, new Identifier(InfinityMod.MOD_ID, name), item);
    }
    public static void registerModBlocks() {
        registerBlock("neither_portal", NEITHER_PORTAL);
        registerBlock("book_box", BOOK_BOX);
        registerItem("book_box", BOOK_BOX_ITEM);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> content.addAfter(Items.CHISELED_BOOKSHELF, BOOK_BOX_ITEM));
    }
}