package net.lerariemann.infinity.item;

import dev.architectury.core.item.ArchitecturyBucketItem;
import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.entity.ModEntities;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

import java.util.List;
import java.util.Optional;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.PlatformMethods.*;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);

    public static final RegistrySupplier<Item> ALTAR_ITEM =
            registerBlockItem(ModBlocks.ALTAR, ItemGroups.FUNCTIONAL, Items.LECTERN);
    public static final RegistrySupplier<Item> ANT_ITEM  =
            registerBlockItem(ModBlocks.ANT, ItemGroups.FUNCTIONAL, Items.LODESTONE);
    public static final RegistrySupplier<Item> BOOK_BOX_ITEM =
            registerBlockItem(ModBlocks.BOOK_BOX, ItemGroups.FUNCTIONAL, Items.CHISELED_BOOKSHELF);
    public static final RegistrySupplier<Item> CURSOR_ITEM  =
            registerBlockItem(ModBlocks.CURSOR, ItemGroups.COLORED_BLOCKS, Items.PINK_TERRACOTTA);
    public static final RegistrySupplier<Item> IRIDESCENT_WOOL  =
            registerBlockItem(ModBlocks.IRIDESCENT_WOOL, ItemGroups.COLORED_BLOCKS, Items.PINK_WOOL);
    public static final RegistrySupplier<Item> FOOTPRINT =
            registerItem("footprint", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5);
    public static final RegistrySupplier<Item> FINE_ITEM = registerHomeItem();
    public static final RegistrySupplier<Item> NETHERITE_SLAB_ITEM =
            registerBlockItem(ModBlocks.NETHERITE_SLAB, ItemGroups.BUILDING_BLOCKS, Items.NETHERITE_BLOCK);
    public static final RegistrySupplier<Item> NETHERITE_STAIRS_ITEM =
            registerBlockItem(ModBlocks.NETHERITE_STAIRS, ItemGroups.BUILDING_BLOCKS, Items.NETHERITE_BLOCK);
    public static final RegistrySupplier<Item> TIME_BOMB_ITEM =
            registerBlockItem(ModBlocks.TIME_BOMB, ItemGroups.OPERATOR, new Item.Settings());
    public static final RegistrySupplier<Item> TRANSFINITE_KEY = registerKeyItem();
    public static final RegistrySupplier<Item> CHAOS_PAWN_SPAWN_EGG = ITEMS.register("chaos_pawn_spawn_egg", () ->
            new ArchitecturySpawnEggItem(ModEntities.CHAOS_PAWN, 0, 0xFFFFFF,
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
    public static final RegistrySupplier<Item> IRIDESCENCE_BUCKET = ITEMS.register("iridescence_bucket", () ->
            new ArchitecturyBucketItem(PlatformMethods.getIridescenceStill(), new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1)));
    public static final RegistrySupplier<Item> WHITE_MATTER =
            registerItem("white_matter", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5);
    public static final RegistrySupplier<Item> BLACK_MATTER =
            registerItem("black_matter", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5);
    public static final RegistrySupplier<Item> BIOME_BOTTLE_ITEM = registerBottleItem();
    public static TagKey<Item> IRIDESCENT_TAG;


    public static RegistrySupplier<Item> registerBlockItem(RegistrySupplier<Block> block, Item.Settings settings) {
        return ITEMS.register(block.getId(), () -> new BlockItem(block.get(), settings));
    }

    public static RegistrySupplier<Item> register(String item, Item.Settings settings) {
        return ITEMS.register(item, () -> new Item(settings));
    }

    public static RegistrySupplier<Item> registerBlockItem(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group, Item.Settings settings) {
       return registerBlockItem(block, settings.arch$tab(group));
    }

    public static RegistrySupplier<Item> registerBlockItem(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group, Item item) {
        return registerBlockItem(block, group, item, new Item.Settings());
    }

    public static Item.Settings addFallbackTab(Item.Settings settings, RegistryKey<ItemGroup> group){
        if (!PlatformMethods.isFabricApiLoaded("fabric-item-group-api-v1"))
            return settings.arch$tab(group);
        return settings;
    }

    public static RegistrySupplier<Item> registerBlockItem(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group, Item item, Item.Settings settings) {
        RegistrySupplier<Item> registeredItem = registerBlockItem(block, addFallbackTab(settings, group));
        addAfter(registeredItem, group, item);
        return registeredItem;
    }

    public static RegistrySupplier<Item> registerItem(String id, RegistryKey<ItemGroup> group, Item item) {
        RegistrySupplier<Item> registeredItem = register(id, addFallbackTab(new Item.Settings(), group));
        addAfter(registeredItem, group, item);
        return registeredItem;
    }

    public static RegistrySupplier<Item> registerKeyItem() {
        final Item.Settings keySettings = addFallbackTab(new Item.Settings(), ItemGroups.INGREDIENTS);
        RegistrySupplier<Item> registeredItem = ITEMS.register("key", () -> new TransfiniteKeyItem(keySettings));
        addAfter(registeredItem, ItemGroups.INGREDIENTS, Items.OMINOUS_TRIAL_KEY);
        return registeredItem;
    }

    public static RegistrySupplier<Item> registerHomeItem() {
        final Item.Settings homeSettings = addFallbackTab(new Item.Settings(), ItemGroups.INGREDIENTS).component(DataComponentTypes.FOOD,
                new FoodComponent(0, 0, true, 3f, Optional.empty(), List.of()));
        RegistrySupplier<Item> registeredItem = ITEMS.register("fine_item", () -> new HomeItem(homeSettings));
        addAfter(registeredItem, ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5);
        return registeredItem;
    }

    public static RegistrySupplier<Item> registerBottleItem() {
        final Item.Settings bottlesettings = addFallbackTab(new Item.Settings(), ItemGroups.FUNCTIONAL);
        RegistrySupplier<Item> registeredItem = ITEMS.register("biome_bottle", () ->
                new BiomeBottleItem(ModBlocks.BIOME_BOTTLE.get(), bottlesettings));
        addAfter(registeredItem, ItemGroups.FUNCTIONAL, Items.CHISELED_BOOKSHELF);
        return registeredItem;
    }

    public static Item.Settings createSpawnEggSettings() {
        return new Item.Settings().arch$tab(ItemGroups.SPAWN_EGGS);
    }

    public static void registerModItems() {
        addAfter(IRIDESCENCE_BUCKET, ItemGroups.TOOLS, Items.MILK_BUCKET);
        ITEMS.register();
    }
}
