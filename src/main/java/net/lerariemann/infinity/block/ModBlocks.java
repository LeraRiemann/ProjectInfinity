package net.lerariemann.infinity.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlocks {
    public static final Block NEITHER_PORTAL = new NeitherPortalBlock(FabricBlockSettings.copy(Blocks.NETHER_PORTAL));
    public static final Block BOOK_BOX = new BookBoxBlock(FabricBlockSettings.copy(Blocks.BOOKSHELF));
    public static final Item BOOK_BOX_ITEM = new BlockItem(BOOK_BOX, new FabricItemSettings());
    public static final Block ALTAR_LIT = new TransfiniteAltar(FabricBlockSettings.copy(Blocks.BEDROCK).nonOpaque().
            luminance(state -> state.get(TransfiniteAltar.FLOWER) ? 15 : 0));
    public static final Block ALTAR = new TransfiniteAltarBase(FabricBlockSettings.copy(Blocks.STONE).nonOpaque().
            luminance(state -> state.get(TransfiniteAltarBase.FLOWER) ? 15 : 0));
    public static final Item ALTAR_ITEM = new BlockItem(ALTAR, new FabricItemSettings());
    public static final Block TIME_BOMB = new TimeBombBlock(FabricBlockSettings.copy(Blocks.BEDROCK).nonOpaque().
            luminance(state -> state.get(TimeBombBlock.ACTIVE) ? 15 : 0));
    public static final Item TIME_BOMB_ITEM = new BlockItem(TIME_BOMB, new FabricItemSettings());
    private static void registerBlock(String name, Block block) {
        Registry.register(Registries.BLOCK, InfinityMod.getId(name), block);
    }
    private static void registerItem(String name, Item item) {
        Registry.register(Registries.ITEM, InfinityMod.getId(name), item);
    }
    public static void registerModBlocks() {
        registerBlock("neither_portal", NEITHER_PORTAL);
        registerBlock("book_box", BOOK_BOX);
        registerItem("book_box", BOOK_BOX_ITEM);
        registerBlock("altar_lit", ALTAR_LIT);
        registerBlock("altar", ALTAR);
        registerItem("altar", ALTAR_ITEM);
        registerBlock("timebomb", TIME_BOMB);
        registerItem("timebomb", TIME_BOMB_ITEM);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> content.addAfter(Items.CHISELED_BOOKSHELF, ALTAR_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> content.addAfter(Items.CHISELED_BOOKSHELF, BOOK_BOX_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(content -> content.add(TIME_BOMB_ITEM));
    }
}