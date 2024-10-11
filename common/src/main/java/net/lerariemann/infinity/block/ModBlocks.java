package net.lerariemann.infinity.block;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModBlocks {
    public static final DeferredRegister<Block> infinityBlocks = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK);
    public static final DeferredRegister<Item> infinityItems = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);
    public static final RegistrySupplier<Block> NEITHER_PORTAL = infinityBlocks.register("neither_portal", () -> new NeitherPortalBlock(AbstractBlock.Settings.copy(Blocks.NETHER_PORTAL)));
    public static final RegistrySupplier<Block> BOOK_BOX = infinityBlocks.register("book_box", () -> new BookBoxBlock(AbstractBlock.Settings.copy(Blocks.BOOKSHELF)));
    public static final RegistrySupplier<Item> BOOK_BOX_ITEM = infinityItems.register("book_box", () -> new BlockItem(BOOK_BOX.get(), new Item.Settings().arch$tab(ItemGroups.FUNCTIONAL)));

    public static final RegistrySupplier<Block> ALTAR_COSMIC = infinityBlocks.register("altar_cosmic", () -> new CosmicAltar(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque()));
    public static final RegistrySupplier<Block> ALTAR_LIT = infinityBlocks.register("altar_lit", () -> new TransfiniteAltar(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque().luminance(state -> state.get(TransfiniteAltar.FLOWER) ? 15 : 0)));
    public static final RegistrySupplier<Block> ALTAR = infinityBlocks.register("altar", () -> new TransfiniteAltar(AbstractBlock.Settings.copy(Blocks.STONE).nonOpaque().luminance(state -> state.get(TransfiniteAltar.FLOWER) ? 15 : 0)));
    public static final RegistrySupplier<Item> ALTAR_ITEM = infinityItems.register("altar", () -> new BlockItem(ALTAR.get(), new Item.Settings().arch$tab(ItemGroups.FUNCTIONAL)));

    public static final RegistrySupplier<Block> TIME_BOMB = infinityBlocks.register("timebomb", () -> new TimeBombBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque().luminance(state -> 15)));
    public static final RegistrySupplier<Item> TIME_BOMB_ITEM = infinityItems.register("timebomb", () -> new BlockItem(TIME_BOMB.get(), new Item.Settings().arch$tab(ItemGroups.OPERATOR)));



    public static void registerModBlocks() {
        infinityBlocks.register();
        infinityItems.register();
    }

    @ExpectPlatform
    public static void addAfter(RegistryKey<ItemGroup> functional, Item addBefore, Item item) {
    }

    @ExpectPlatform
    public static void add(RegistryKey<ItemGroup> functional, Item item) {
    }


}