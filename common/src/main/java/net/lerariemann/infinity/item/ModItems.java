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
import net.minecraft.util.Identifier;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.PlatformMethods.*;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);

    public static final RegistrySupplier<Item> ALTAR_ITEM = registerBlockItem(ModBlocks.ALTAR, ItemGroups.FUNCTIONAL, Items.LECTERN);
    public static final RegistrySupplier<Item> ANT_ITEM  = registerBlockItem(ModBlocks.ANT, ItemGroups.FUNCTIONAL, Items.LODESTONE);
    public static final RegistrySupplier<Item> BOOK_BOX_ITEM = registerBlockItem(ModBlocks.BOOK_BOX, ItemGroups.FUNCTIONAL, Items.CHISELED_BOOKSHELF);
    public static final RegistrySupplier<Item> CURSOR_ITEM  = registerBlockItem(ModBlocks.CURSOR, ItemGroups.COLORED_BLOCKS, Items.PINK_TERRACOTTA);
    public static final RegistrySupplier<Item> FOOTPRINT = registerItem("footprint", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5);
    public static final RegistrySupplier<Item> FINE_ITEM = registerItem("fine_item", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5);
    public static final RegistrySupplier<Item> NETHERITE_SLAB_ITEM  = registerBlockItem(ModBlocks.NETHERITE_SLAB, ItemGroups.BUILDING_BLOCKS, Items.NETHERITE_BLOCK);
    public static final RegistrySupplier<Item> NETHERITE_STAIRS_ITEM  = registerBlockItem(ModBlocks.NETHERITE_STAIRS, ItemGroups.BUILDING_BLOCKS, Items.NETHERITE_BLOCK);
    public static final RegistrySupplier<Item> TIME_BOMB_ITEM = registerBlockItem(ModBlocks.TIME_BOMB, ItemGroups.OPERATOR);
    public static final RegistrySupplier<Item> TRANSFINITE_KEY = registerKeyItem();
    public static final RegistrySupplier<Item> CHAOS_PAWN_SPAWN_EGG = ITEMS.register("chaos_pawn_spawn_egg", () ->
            new ArchitecturySpawnEggItem(ModEntities.CHAOS_PAWN, 0, 0xFFFFFF, createSpawnEggSettings("chaos_pawn_spawn_egg")));
    public static final RegistrySupplier<Item> CHAOS_CREEPER_SPAWN_EGG = ITEMS.register("chaos_creeper_spawn_egg", () ->
            new ArchitecturySpawnEggItem(ModEntities.CHAOS_CREEPER, 0x91BD59, 0x78A7FF, createSpawnEggSettings("chaos_creeper_spawn_egg")));
    public static final RegistrySupplier<Item> CHAOS_SKELETON_SPAWN_EGG = ITEMS.register("chaos_skeleton_spawn_egg", () ->
            new ArchitecturySpawnEggItem(ModEntities.CHAOS_SKELETON, 0xF3CFB9, 0x87A363, createSpawnEggSettings("chaos_skeleton_spawn_egg")));
    public static final RegistrySupplier<Item> CHAOS_SLIME_SPAWN_EGG = ITEMS.register("chaos_slime_spawn_egg",  () ->
            new ArchitecturySpawnEggItem(ModEntities.CHAOS_SLIME, 0xAA77DD, 0xFF66FF, createSpawnEggSettings("chaos_slime_spawn_egg")));
    public static final RegistrySupplier<Item> IRIDESCENCE_BUCKET = ITEMS.register("iridescence_bucket", () ->
            new ArchitecturyBucketItem(PlatformMethods.getIridescenceStill(), new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1).registryKey(registryKey("iridescence_bucket"))));


    public static RegistrySupplier<Item> registerBlockItem(RegistrySupplier<Block> block, Item.Settings settings) {
        return ITEMS.register(block.getId(), () -> new BlockItem(block.get(), settings));
    }

    public static RegistrySupplier<Item> register(String item, Item.Settings settings) {
        return ITEMS.register(item, () -> new Item(settings));
    }

    public static RegistrySupplier<Item> registerBlockItem(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group) {
       return registerBlockItem(block, settings(block.getId().getPath()).useBlockPrefixedTranslationKey().arch$tab(group));
    }

    public static Item.Settings settings(String id) {
        return new Item.Settings().registryKey(registryKey(id));
    }

    public static RegistryKey<Item> registryKey(String id) {
        return RegistryKey.of(RegistryKeys.ITEM, InfinityMod.getId(id));
    }

    public static RegistrySupplier<Item> registerBlockItem(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group, Item item) {
        Item.Settings settings = settings(block.getId().getPath()).useBlockPrefixedTranslationKey();
        if (!PlatformMethods.isFabricApiLoaded("fabric-item-group-api-v1"))
            settings = settings.arch$tab(group);
        RegistrySupplier<Item> registeredItem = registerBlockItem(block, settings);
        addAfter(registeredItem, group, item);
        return registeredItem;
    }

    public static RegistrySupplier<Item> registerItem(String id, RegistryKey<ItemGroup> group, Item item) {
        Item.Settings settings = settings(id);
        if (!PlatformMethods.isFabricApiLoaded("fabric-item-group-api-v1"))
            settings = settings.arch$tab(group);
        RegistrySupplier<Item> registeredItem = register(id, settings);
        addAfter(registeredItem, group, item);
        return registeredItem;
    }

    public static RegistrySupplier<Item> registerKeyItem() {
        Item.Settings settings = settings("key");
        if (!PlatformMethods.isFabricApiLoaded("fabric-item-group-api-v1"))
            settings = settings.arch$tab(ItemGroups.INGREDIENTS);
        final Item.Settings keySettings = settings;
        RegistrySupplier<Item> registeredItem = ITEMS.register("key", () -> new TransfiniteKeyItem(keySettings));
        addAfter(registeredItem, ItemGroups.INGREDIENTS, Items.OMINOUS_TRIAL_KEY);
        return registeredItem;
    }

    public static Item.Settings createSpawnEggSettings(String id) {
        return settings(id).arch$tab(ItemGroups.SPAWN_EGGS);
    }

    public static void registerModItems() {
        addAfter(IRIDESCENCE_BUCKET, ItemGroups.TOOLS, Items.MILK_BUCKET);
        ITEMS.register();
    }

    @Environment(EnvType.CLIENT)
    public static void registerModelPredicates() {
        ItemPropertiesRegistry.register(TRANSFINITE_KEY.get(), InfinityMod.getId("key"), (stack, world, entity, seed) -> {
            Identifier id = stack.getComponents().get(ModComponentTypes.KEY_DESTINATION.get());
            if (id == null) return 0.02f;
            String s = id.toString();
            if (s.contains("infinity:generated_")) return 0.01f;
            return switch(s) {
                case "minecraft:random" -> 0.02f;
                case "minecraft:the_end" -> 0.03f;
                case "infinity:pride" -> 0.04f;
                default -> 0;
            };
        });
    }
}
