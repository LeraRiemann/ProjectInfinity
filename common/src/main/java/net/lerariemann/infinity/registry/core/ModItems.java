package net.lerariemann.infinity.registry.core;

import dev.architectury.core.item.ArchitecturyBucketItem;
import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.item.*;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Rarity;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.util.PlatformMethods.*;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);
    //block items
    public static final RegistrySupplier<Item> PORTAL_ITEM =
            ITEMS.register(ModBlocks.PORTAL.getId(), () -> new BlockItem(ModBlocks.PORTAL.get(), new Item.Settings()));
    public static final RegistrySupplier<Item> ALTAR_ITEM =
            registerBlockItemAfter(ModBlocks.ALTAR, ItemGroups.FUNCTIONAL, Items.LECTERN, BlockItem::new);
    public static final RegistrySupplier<Item> COSMIC_ALTAR_ITEM =
            registerBlockItemAfter(ModBlocks.COSMIC_ALTAR, ItemGroups.OPERATOR, Items.DEBUG_STICK, BlockItem::new);
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
    public static final RegistrySupplier<Item> TIME_BOMB_ITEM =
            registerBlockItemAfter(ModBlocks.TIME_BOMB, ItemGroups.FUNCTIONAL, Items.VAULT, BlockItem::new);
    public static final RegistrySupplier<Item> IRIDESCENT_WOOL  =
            registerBlockItemAfter(ModBlocks.IRIDESCENT_WOOL, ItemGroups.COLORED_BLOCKS, Items.PINK_WOOL, BlockItem::new);
    public static final RegistrySupplier<Item> IRIDESCENT_CARPET  =
            registerBlockItemAfter(ModBlocks.IRIDESCENT_CARPET, ItemGroups.COLORED_BLOCKS, Items.PINK_CARPET, BlockItem::new);
    public static final RegistrySupplier<Item> BIOME_BOTTLE_ITEM =
            registerBlockItemAfter(ModBlocks.BIOME_BOTTLE, ItemGroups.INGREDIENTS, Items.EXPERIENCE_BOTTLE, BiomeBottleItem::new);
    //spawn eggs
    public static final RegistrySupplier<Item> CHAOS_PAWN_SPAWN_EGG = ITEMS.register("chaos_pawn_spawn_egg", () ->
            new ArchitecturySpawnEggItem(ModEntities.CHAOS_PAWN, 0xFF66FF, 0xAA77DD,
                    createSpawnEggSettings()));
    public static final RegistrySupplier<Item> CHAOS_CREEPER_SPAWN_EGG = ITEMS.register("chaos_creeper_spawn_egg", () ->
            new ArchitecturySpawnEggItem(ModEntities.CHAOS_CREEPER, 0x91BD59, 0x78A7FF,
                    createSpawnEggSettings()));
    public static final RegistrySupplier<Item> CHAOS_SKELETON_SPAWN_EGG = ITEMS.register("chaos_skeleton_spawn_egg", () ->
            new ArchitecturySpawnEggItem(ModEntities.CHAOS_SKELETON, 0xF3CFB9, 0x87A363,
                    createSpawnEggSettings()));
    public static final RegistrySupplier<Item> CHAOS_SLIME_SPAWN_EGG = ITEMS.register("chaos_slime_spawn_egg",  () ->
            new ArchitecturySpawnEggItem(ModEntities.CHAOS_SLIME, 0xAA77DD, 0xFF66FF,
                    createSpawnEggSettings()));
    public static final RegistrySupplier<Item> ANT_SPAWN_EGG = ITEMS.register("ant_spawn_egg",  () ->
            new ArchitecturySpawnEggItem(ModEntities.ANT, 0, 0xFFFFFF,
                    createSpawnEggSettings()));
    //bucket
    public static final RegistrySupplier<Item> IRIDESCENCE_BUCKET = ITEMS.register("iridescence_bucket", () ->
            new ArchitecturyBucketItem(PlatformMethods.getIridescenceStill(), new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1)));
    //misc
    public static final RegistrySupplier<Item> FOOTPRINT =
            registerItemAfter("footprint", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5, Item::new);
    public static final RegistrySupplier<TransfiniteKeyItem> TRANSFINITE_KEY =
            registerItemAfter("key", ItemGroups.INGREDIENTS, Items.OMINOUS_TRIAL_KEY, TransfiniteKeyItem::new);
    public static final RegistrySupplier<HomeItem> HOME_ITEM =
            registerItemAfter("fine_item", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5, HomeItem::new,
                    new Item.Settings().component(DataComponentTypes.FOOD,
                            new FoodComponent(0, 0, true, 3f, Optional.empty(), List.of())));
    public static final RegistrySupplier<Item> WHITE_MATTER =
            registerItemAfter("white_matter", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5, Item::new);
    public static final RegistrySupplier<Item> BLACK_MATTER =
            registerItemAfter("black_matter", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5, Item::new);
    public static final RegistrySupplier<F4Item> F4 =
            registerItemAfter("f4", ItemGroups.OPERATOR, Items.DEBUG_STICK, F4Item::new,
                    new Item.Settings().rarity(Rarity.UNCOMMON));
    public static TagKey<Item> IRIDESCENT_TAG = createItemTag("iridescent");

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

    public static RegistrySupplier<Item> registerBlockItem(RegistrySupplier<Block> block, Item.Settings settings,
                                                           BiFunction<Block, Item.Settings, Item> constructor) {
        return ITEMS.register(block.getId(), () -> constructor.apply(block.get(), settings));
    }
    /**
     * Registers a Block Item via Architectury API.
     */
    public static RegistrySupplier<Item> registerBlockItemAfter(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group, Item item,
                                                                Item.Settings settings,
                                                                BiFunction<Block, Item.Settings, Item> constructor) {
        RegistrySupplier<Item> registeredItem = registerBlockItem(block, addFallbackTab(settings, group), constructor);
        addAfter(registeredItem, group, item);
        return registeredItem;
    }
    /**
     * Registers a Block Item through Architectury API.
     */
    public static RegistrySupplier<Item> registerBlockItemAfter(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group, Item item,
                                                                BiFunction<Block, Item.Settings, Item> constructor) {
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

    public static void registerModItems() {
        addAfter(IRIDESCENCE_BUCKET, ItemGroups.TOOLS, Items.MILK_BUCKET);
        ITEMS.register();
    }
}
