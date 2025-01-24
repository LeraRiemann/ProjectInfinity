package net.lerariemann.infinity.registry.core;

import dev.architectury.core.item.ArchitecturyBucketItem;
import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.item.*;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.var.ColorLogic;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;

import java.util.function.BiFunction;
import java.util.function.Function;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.util.PlatformMethods.*;

@SuppressWarnings("unused")
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);
    //block items
    public static final RegistrySupplier<Item> PORTAL_ITEM =
            ITEMS.register(ModBlocks.PORTAL.getId(), () -> new BlockItem(ModBlocks.PORTAL.get(), new Item.Settings()));
    public static final RegistrySupplier<Item> COSMIC_ALTAR_ITEM =
            registerBlockItemAfter(ModBlocks.COSMIC_ALTAR, ItemGroups.FUNCTIONAL, Items.LECTERN, BlockItem::new);
    public static final RegistrySupplier<Item> ALTAR_ITEM =
            registerBlockItemAfter(ModBlocks.ALTAR, ItemGroups.FUNCTIONAL, Items.LECTERN, BlockItem::new);
    public static final RegistrySupplier<Item> ANT_ITEM  =
            registerBlockItemAfter(ModBlocks.ANT, ItemGroups.FUNCTIONAL, Items.LODESTONE, BlockItem::new);
    public static final RegistrySupplier<Item> BOOK_BOX_ITEM =
            registerBlockItemAfter(ModBlocks.BOOK_BOX, ItemGroups.FUNCTIONAL, Items.CHISELED_BOOKSHELF, BlockItem::new);
    public static final RegistrySupplier<Item> CURSOR_ITEM  =
            registerBlockItemAfter(ModBlocks.CURSOR, ItemGroups.COLORED_BLOCKS, Items.PINK_TERRACOTTA, BlockItem::new);
    public static final RegistrySupplier<Item> NETHERITE_SLAB_ITEM =
            registerBlockItemAfter(ModBlocks.NETHERITE_SLAB, ItemGroups.BUILDING_BLOCKS, Items.NETHERITE_BLOCK, BlockItem::new);
    public static final RegistrySupplier<Item> NETHERITE_STAIRS_ITEM =
            registerBlockItemAfter(ModBlocks.NETHERITE_STAIRS, ItemGroups.BUILDING_BLOCKS, Items.NETHERITE_BLOCK, BlockItem::new);
    public static final RegistrySupplier<Item> NOTES_BLOCK_ITEM =
            registerBlockItemAfter(ModBlocks.NOTES_BLOCK, ItemGroups.FUNCTIONAL, Items.NOTE_BLOCK, BlockItem::new);
    public static final RegistrySupplier<Item> TIME_BOMB_ITEM =
            registerBlockItemAfter(ModBlocks.TIME_BOMB, ItemGroups.FUNCTIONAL, Items.BEACON, BlockItem::new);
    public static final RegistrySupplier<Item> IRIDESCENT_WOOL  =
            registerBlockItemAfter(ModBlocks.IRIDESCENT_WOOL, ItemGroups.COLORED_BLOCKS, Items.PINK_WOOL, BlockItem::new);
    public static final RegistrySupplier<Item> IRIDESCENT_CARPET  =
            registerBlockItemAfter(ModBlocks.IRIDESCENT_CARPET, ItemGroups.COLORED_BLOCKS, Items.PINK_CARPET, BlockItem::new);
    public static final RegistrySupplier<ChromaticBlockItem> CHROMATIC_WOOL  =
            registerBlockItemAfter(ModBlocks.CHROMATIC_WOOL, ItemGroups.COLORED_BLOCKS, Items.PINK_WOOL,
                    new Item.Settings(),
                    ChromaticBlockItem::new);
    public static final RegistrySupplier<ChromaticBlockItem> CHROMATIC_CARPET  =
            registerBlockItemAfter(ModBlocks.CHROMATIC_CARPET, ItemGroups.COLORED_BLOCKS, Items.PINK_CARPET,
                    new Item.Settings(),
                    ChromaticBlockItem::new);
    public static final RegistrySupplier<Item> BIOME_BOTTLE_ITEM =
            registerBlockItemAfter(ModBlocks.BIOME_BOTTLE, ItemGroups.INGREDIENTS, Items.EXPERIENCE_BOTTLE, BiomeBottleItem::new);
    //spawn eggs
    public static final RegistrySupplier<Item> CHAOS_CREEPER_SPAWN_EGG = ITEMS.register("chaos_creeper_spawn_egg", () ->
            new ArchitecturySpawnEggItem(ModEntities.CHAOS_CREEPER, 0x91BD59, 0x78A7FF,
                    createSpawnEggSettings()));
    public static final RegistrySupplier<Item> CHAOS_SKELETON_SPAWN_EGG = ITEMS.register("chaos_skeleton_spawn_egg", () ->
            new ArchitecturySpawnEggItem(ModEntities.CHAOS_SKELETON, 0xF3CFB9, 0x87A363,
                    createSpawnEggSettings()));
    public static final RegistrySupplier<Item> CHAOS_SLIME_SPAWN_EGG = ITEMS.register("chaos_slime_spawn_egg",  () ->
            new ArchitecturySpawnEggItem(ModEntities.CHAOS_SLIME, 0xFF66FF, 0xAA77DD,
                    createSpawnEggSettings()));
    public static final RegistrySupplier<Item> CHAOS_PAWN_SPAWN_EGG = ITEMS.register("chaos_pawn_spawn_egg", () ->
            new ArchitecturySpawnEggItem(ModEntities.CHAOS_PAWN, 0x222222, 0xFFFFFF,
                    createSpawnEggSettings()));
    public static final RegistrySupplier<Item> ANT_SPAWN_EGG = ITEMS.register("ant_spawn_egg",  () ->
            new ArchitecturySpawnEggItem(ModEntities.ANT, 0x444444, 0xFFFFFF,
                    createSpawnEggSettings()));
    public static final RegistrySupplier<Item> BISHOP_SPAWN_EGG = ITEMS.register("bishop_spawn_egg",  () ->
            new ArchitecturySpawnEggItem(ModEntities.BISHOP, 0, 0xFFFFFF,
                    createSpawnEggSettings()));
    //bucket
    public static final RegistrySupplier<Item> IRIDESCENCE_BUCKET = ITEMS.register("iridescence_bucket", () ->
            new ArchitecturyBucketItem(PlatformMethods.getIridescenceStill(), new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1)));
    //misc
    public static final RegistrySupplier<Item> FOOTPRINT =
            registerItemAfter("footprint", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5, Item::new);
    public static final RegistrySupplier<TransfiniteKeyItem> TRANSFINITE_KEY =
            registerItemAfter("key", ItemGroups.INGREDIENTS, Items.AMETHYST_SHARD, TransfiniteKeyItem::new);
    public static final RegistrySupplier<HomeItem> HOME_ITEM =
            registerItemAfter("fine_item", ItemGroups.INGREDIENTS, Items.MILK_BUCKET, HomeItem::new,
                    new Item.Settings().food(
                            new FoodComponent.Builder().alwaysEdible().build()));
    public static final RegistrySupplier<ChromaticItem> CHROMATIC_MATTER =
            registerItemAfter("chromatic_matter", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5, ChromaticItem::new,
                    new Item.Settings());
    public static final RegistrySupplier<Item> WHITE_MATTER =
            registerItemAfter("white_matter", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5, Item::new);
    public static final RegistrySupplier<Item> BLACK_MATTER =
            registerItemAfter("black_matter", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5, Item::new);
    public static final RegistrySupplier<Item> IRIDESCENT_STAR =
            registerItemAfter("iridescent_star", ItemGroups.INGREDIENTS, Items.NETHER_STAR, GlintItem::new,
                    new Item.Settings().rarity(Rarity.UNCOMMON));
    public static final RegistrySupplier<? extends StarOfLangItem> STAR_OF_LANG =
            registerItemAfter("star_of_lang", ItemGroups.INGREDIENTS, Items.NETHER_STAR, PlatformMethods.getStarOfLangConstructor(),
                    new Item.Settings());
    public static final RegistrySupplier<F4Item> F4 =
            registerItemAfter("f4", ItemGroups.TOOLS, Items.WRITABLE_BOOK, F4Item::new,
                    new Item.Settings().rarity(Rarity.UNCOMMON));
    public static final RegistrySupplier<Item> DISC =
            registerItemAfter("disc", ItemGroups.TOOLS, Items.MUSIC_DISC_PIGSTEP, Item::new,
                    new Item.Settings().rarity(Rarity.RARE));
    public static final RegistrySupplier<Item> IRIDESCENT_POTION =
            registerItemAfter("iridescent_potion", ItemGroups.FOOD_AND_DRINK, Items.HONEY_BOTTLE, IridescentPotionItem::new,
                    new Item.Settings());
    public static final RegistrySupplier<Item> CHROMATIC_POTION =
            registerItemAfter("chromatic_potion", ItemGroups.FOOD_AND_DRINK, Items.HONEY_BOTTLE, IridescentPotionItem::new,
                    new Item.Settings());

    public static <T extends Item> RegistrySupplier<T> register(String item, Item.Settings settings, Function<Item.Settings, T> constructor) {
        return ITEMS.register(item, () -> constructor.apply(settings));
    }
    /**
     * Registers an item via Architectury API.
     */
    public static <T extends Item> RegistrySupplier<T> registerItemAfter(String id, RegistryKey<ItemGroup> group, Item item,
                                                           Function<Item.Settings, T> constructor, Item.Settings settings) {
        RegistrySupplier<T> registeredItem = register(id, addFallbackTab(settings, group), constructor);
        addAfter(registeredItem, group, item);
        return registeredItem;
    }
    public static <T extends Item> RegistrySupplier<T> registerItemAfter(String id, RegistryKey<ItemGroup> group, Item item,
                                                           Function<Item.Settings, T> constructor) {
        return registerItemAfter(id, group, item, constructor, new Item.Settings());
    }

    public static <T extends Item> RegistrySupplier<T> registerBlockItem(RegistrySupplier<Block> block, Item.Settings settings,
                                                           BiFunction<Block, Item.Settings, T> constructor) {
        return ITEMS.register(block.getId(), () -> constructor.apply(block.get(), settings));
    }
    /**
     * Registers a BlockItem via Architectury API.
     */
    public static <T extends Item> RegistrySupplier<T> registerBlockItemAfter(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group, Item item,
                                                                Item.Settings settings,
                                                                BiFunction<Block, Item.Settings, T> constructor) {
        RegistrySupplier<T> registeredItem = registerBlockItem(block, addFallbackTab(settings, group), constructor);
        addAfter(registeredItem, group, item);
        return registeredItem;
    }
    /**
     * Registers a BlockItem through Architectury API.
     */
    public static <T extends Item> RegistrySupplier<T> registerBlockItemAfter(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group, Item item,
                                                                BiFunction<Block, Item.Settings, T> constructor) {
        return registerBlockItemAfter(block, group, item, new Item.Settings(), constructor);
    }

    /**
     * Adds an item to an Item Group through Architectury API if Fabric API is not installed.
     */
    public static Item.Settings addFallbackTab(Item.Settings settings, RegistryKey<ItemGroup> group){
        if (!InfinityMethods.isFabricApiLoaded("fabric-item-group-api-v1"))
            return settings.arch$tab(group);
        return settings;
    }

    /**
     * Creates item settings for Spawn Egg items.
     */
    public static Item.Settings createSpawnEggSettings() {
        return new Item.Settings().arch$tab(ItemGroups.SPAWN_EGGS);
    }

    public static Item.Settings createChromaticSettings() {
        return deferredIntComponent(ModComponentTypes.COLOR, ColorLogic.defaultChromatic);
    }

    public static void registerModItems() {
        addAfter(IRIDESCENCE_BUCKET, ItemGroups.TOOLS, Items.MILK_BUCKET);
        InfinityMod.LOGGER.debug("Registering items for " + MOD_ID);
        ITEMS.register();
    }
}
