package net.lerariemann.infinity.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.*;
import net.lerariemann.infinity.entity.ModEntities;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlocks {
    //portal
    public static final Block NEITHER_PORTAL = new NeitherPortalBlock(AbstractBlock.Settings.copy(Blocks.NETHER_PORTAL));
    //book box
    public static final Block BOOK_BOX = new BookBoxBlock(AbstractBlock.Settings.copy(Blocks.BOOKSHELF));
    public static final Item BOOK_BOX_ITEM = new BlockItem(BOOK_BOX, new Item.Settings());
    //altar
    public static final Block ALTAR_COSMIC = new CosmicAltar(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque());
    public static final Block ALTAR_LIT = new TransfiniteAltar(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque().
            luminance(state -> state.get(TransfiniteAltar.FLOWER) ? 15 : 0));
    public static final Block ALTAR = new TransfiniteAltarBase(AbstractBlock.Settings.copy(Blocks.STONE).nonOpaque().
            luminance(state -> state.get(TransfiniteAltarBase.FLOWER) ? 15 : 0));
    public static final Item ALTAR_ITEM = new BlockItem(ALTAR, new Item.Settings());
    //reset charge
    public static final Block TIME_BOMB = new TimeBombBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque().
            luminance(state -> 15));
    public static final Item TIME_BOMB_ITEM = new BlockItem(TIME_BOMB, new Item.Settings());
    //spawn eggs
    public static final Item CHAOS_SKELETON_SPAWN_EGG = new SpawnEggItem(ModEntities.CHAOS_SKELETON, 4012102, 9519532, new Item.Settings());
    public static final Item CHAOS_SLIME_SPAWN_EGG = new SpawnEggItem(ModEntities.CHAOS_SLIME, 16753145, 12607947, new Item.Settings());
    public static final Item CHAOS_CREEPER_SPAWN_EGG = new SpawnEggItem(ModEntities.CHAOS_CREEPER, 4259648, 16753480, new Item.Settings());
    public static final Item CHAOS_PAWN_SPAWN_EGG = new SpawnEggItem(ModEntities.CHAOS_PAWN, 0, 16777215, new Item.Settings());

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

        registerItem("chaos_skeleton_spawn_egg", CHAOS_SKELETON_SPAWN_EGG);
        registerItem("chaos_creeper_spawn_egg", CHAOS_CREEPER_SPAWN_EGG);
        registerItem("chaos_pawn_spawn_egg", CHAOS_PAWN_SPAWN_EGG);
        registerItem("chaos_slime_spawn_egg", CHAOS_SLIME_SPAWN_EGG);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> content.addAfter(Items.CHISELED_BOOKSHELF, ALTAR_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> content.addAfter(Items.CHISELED_BOOKSHELF, BOOK_BOX_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(content -> content.add(TIME_BOMB_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
            content.add(CHAOS_SKELETON_SPAWN_EGG);
            content.add(CHAOS_CREEPER_SPAWN_EGG);
            content.add(CHAOS_SLIME_SPAWN_EGG);
            content.add(CHAOS_PAWN_SPAWN_EGG);
        });
    }
}