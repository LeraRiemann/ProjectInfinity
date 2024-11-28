package net.lerariemann.infinity.item;

import dev.architectury.core.item.ArchitecturyBucketItem;
import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.entity.ModEntities;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

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
    public static final RegistrySupplier<Item> FOOTPRINT =
            registerItem("footprint", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5);
    public static final RegistrySupplier<Item> FINE_ITEM = registerHomeItem();
    public static final RegistrySupplier<Item> NETHERITE_SLAB_ITEM =
            registerBlockItem(ModBlocks.NETHERITE_SLAB, ItemGroups.BUILDING_BLOCKS, Items.NETHERITE_BLOCK);
    public static final RegistrySupplier<Item> NETHERITE_STAIRS_ITEM =
            registerBlockItem(ModBlocks.NETHERITE_STAIRS, ItemGroups.BUILDING_BLOCKS, Items.NETHERITE_BLOCK);
    public static final RegistrySupplier<Item> TIME_BOMB_ITEM =
            registerBlockItem(ModBlocks.TIME_BOMB, ItemGroups.OPERATOR);
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



    public static RegistrySupplier<Item> registerBlockItem(RegistrySupplier<Block> block, Item.Settings settings) {
        return ITEMS.register(block.getId(), () -> new BlockItem(block.get(), settings));
    }

    public static RegistrySupplier<Item> register(String item, Item.Settings settings) {
        return ITEMS.register(item, () -> new Item(settings));
    }

    public static RegistrySupplier<Item> registerBlockItem(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group) {
       return registerBlockItem(block, new Item.Settings().arch$tab(group));
    }

    public static RegistrySupplier<Item> registerBlockItem(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group, Item item) {
        Item.Settings settings = new Item.Settings();
        if (!PlatformMethods.isFabricApiLoaded("fabric-item-group-api-v1"))
            settings = settings.arch$tab(group);
        RegistrySupplier<Item> registeredItem = registerBlockItem(block, settings);
        addAfter(registeredItem, group, item);
        return registeredItem;
    }

    public static RegistrySupplier<Item> registerItem(String id, RegistryKey<ItemGroup> group, Item item) {
        Item.Settings settings = new Item.Settings();
        if (!PlatformMethods.isFabricApiLoaded("fabric-item-group-api-v1"))
            settings = settings.arch$tab(group);
        RegistrySupplier<Item> registeredItem = register(id, settings);
        addAfter(registeredItem, group, item);
        return registeredItem;
    }

    public static RegistrySupplier<Item> registerKeyItem() {
        RegistrySupplier<Item> registeredKey = ITEMS.register("key", () ->
                new TransfiniteKeyItem(new Item.Settings()));
        addAfter(registeredKey, ItemGroups.INGREDIENTS, Items.AMETHYST_SHARD);
        return registeredKey;
    }

    public static RegistrySupplier<Item> registerHomeItem() {
        Item.Settings settings = new Item.Settings();
        if (!PlatformMethods.isFabricApiLoaded("fabric-item-group-api-v1"))
            settings = settings.arch$tab(ItemGroups.INGREDIENTS);
        final Item.Settings homeSettings = settings.food(new FoodComponent.Builder().build());
        RegistrySupplier<Item> registeredItem = ITEMS.register("fine_item", () -> new HomeItem(homeSettings));
        addAfter(registeredItem, ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5);
        return registeredItem;
    }

    public static Item.Settings createSpawnEggSettings() {
        return new Item.Settings().arch$tab(ItemGroups.SPAWN_EGGS);
    }

    public static void registerModItems() {
        addAfter(IRIDESCENCE_BUCKET, ItemGroups.TOOLS, Items.MILK_BUCKET);
        ITEMS.register();
    }

    @Environment(EnvType.CLIENT)
    public static void registerModelPredicates() {
        ItemPropertiesRegistry.register(TRANSFINITE_KEY.get(), InfinityMod.getId("key"), (stack, world, entity, seed) -> {
            String id;
            if (stack.getNbt() != null) {
                id = stack.getNbt().getString("key_destination");
            }
            else id = "minecraft:random";
            if (id == null) return 0;
            if (id.contains("infinity:generated_")) return 0.01f;
            return switch(id) {
                case "minecraft:random" -> 0.02f;
                case "minecraft:the_end" -> 0.03f;
                case "infinity:pride" -> 0.04f;
                default -> 0;
            };
        });
    }
}
