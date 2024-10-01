package net.lerariemann.infinity.block;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class ModBlocks {
    public static final Block NEITHER_PORTAL = new NeitherPortalBlock(AbstractBlock.Settings.copy(Blocks.NETHER_PORTAL));
    public static final Block BOOK_BOX = new BookBoxBlock(AbstractBlock.Settings.copy(Blocks.BOOKSHELF));
    public static final Item BOOK_BOX_ITEM = new BlockItem(BOOK_BOX, new Item.Settings());
    public static final Block ALTAR_COSMIC = new CosmicAltar(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque());
    public static final Block ALTAR_LIT = new TransfiniteAltar(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque().
            luminance(state -> state.get(TransfiniteAltar.FLOWER) ? 15 : 0));
    public static final Block ALTAR = new TransfiniteAltarBase(AbstractBlock.Settings.copy(Blocks.STONE).nonOpaque().
            luminance(state -> state.get(TransfiniteAltarBase.FLOWER) ? 15 : 0));
    public static final Item ALTAR_ITEM = new BlockItem(ALTAR, new Item.Settings());
    public static final Block TIME_BOMB = new TimeBombBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque().
            luminance(state -> 15));
    public static final Item TIME_BOMB_ITEM = new BlockItem(TIME_BOMB, new Item.Settings());
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
        registerBlock("altar_cosmic", ALTAR_COSMIC);
        registerItem("altar", ALTAR_ITEM);
        registerBlock("timebomb", TIME_BOMB);
        registerItem("timebomb", TIME_BOMB_ITEM);
        addAfter(ItemGroups.FUNCTIONAL, Items.CHISELED_BOOKSHELF, ALTAR_ITEM);
        addAfter(ItemGroups.FUNCTIONAL, Items.CHISELED_BOOKSHELF, BOOK_BOX_ITEM);
        add(ItemGroups.OPERATOR, TIME_BOMB_ITEM);
    }

    @ExpectPlatform
    public static void addAfter(RegistryKey<ItemGroup> functional, Item addBefore, Item item) {
    }

    @ExpectPlatform
    public static void add(RegistryKey<ItemGroup> functional, Item item) {
    }


}