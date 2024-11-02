package net.lerariemann.infinity.item;

import dev.architectury.registry.item.ItemPropertiesRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.PlatformMethods.*;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);

    public static final RegistrySupplier<Item> ALTAR_ITEM = registerSimpleBlockItem(ModBlocks.ALTAR, ItemGroups.FUNCTIONAL, Items.LECTERN);
    public static final RegistrySupplier<Item> ANT_ITEM  = registerSimpleBlockItem(ModBlocks.ANT, ItemGroups.FUNCTIONAL, Items.LODESTONE);
    public static final RegistrySupplier<Item> BOOK_BOX_ITEM = registerSimpleBlockItem(ModBlocks.BOOK_BOX, ItemGroups.FUNCTIONAL, Items.CHISELED_BOOKSHELF);
    public static final RegistrySupplier<Item> CURSOR_ITEM  = registerSimpleBlockItem(ModBlocks.CURSOR, ItemGroups.COLORED_BLOCKS, Items.PINK_TERRACOTTA);
    public static final RegistrySupplier<Item> FOOTPRINT = registerSimpleItem("footprint", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5);
    public static final RegistrySupplier<Item> FINE_ITEM = registerSimpleItem("fine_item", ItemGroups.INGREDIENTS, Items.DISC_FRAGMENT_5);
    public static final RegistrySupplier<Item> NETHERITE_SLAB_ITEM  = registerSimpleBlockItem(ModBlocks.NETHERITE_SLAB, ItemGroups.BUILDING_BLOCKS, Items.NETHERITE_BLOCK);
    public static final RegistrySupplier<Item> NETHERITE_STAIRS_ITEM  = registerSimpleBlockItem(ModBlocks.NETHERITE_STAIRS, ItemGroups.BUILDING_BLOCKS, Items.NETHERITE_BLOCK);
    public static final RegistrySupplier<Item> TIME_BOMB_ITEM = registerSimpleBlockItem(ModBlocks.TIME_BOMB, ItemGroups.OPERATOR);
    public static final RegistrySupplier<Item> TRANSFINITE_KEY = registerKeyItem();


    public static RegistrySupplier<Item> registerSimpleBlockItem(RegistrySupplier<Block> block, Item.Settings settings) {
        return ITEMS.register(block.getId(), () -> new BlockItem(block.get(), settings));
    }

    public static RegistrySupplier<Item> registerSimpleItem(String item, Item.Settings settings) {
        return ITEMS.register(item, () -> new Item(settings));
    }

    public static RegistrySupplier<Item> registerSimpleItem(String block, RegistryKey<ItemGroup> group) {
        return registerSimpleItem(block, new Item.Settings().arch$tab(group));
    }

    public static RegistrySupplier<Item> registerSimpleBlockItem(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group) {
       return registerSimpleBlockItem(block, new Item.Settings().arch$tab(group));
    }

    public static RegistrySupplier<Item> registerSimpleBlockItem(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group, Item item) {
        if (PlatformMethods.isFabricApiLoaded("fabric-item-group-api-v1")) {
            var blockItem = registerSimpleBlockItem(block, new Item.Settings());
            addAfter(blockItem, group, item);
            return blockItem;
        }
        else {
            return registerSimpleBlockItem(block, group);
        }
    }

    public static RegistrySupplier<Item> registerSimpleItem(String id, RegistryKey<ItemGroup> group, Item item) {
        if (PlatformMethods.isFabricApiLoaded("fabric-item-group-api-v1")) {
            var blockItem = registerSimpleItem(id, new Item.Settings());
            addAfter(blockItem, group, item);
            return blockItem;
        }
        else {
            return registerSimpleItem(id, group);
        }
    }

    public static RegistrySupplier<Item> registerKeyItem() {
        RegistrySupplier<Item> registeredKey = ITEMS.register("key", () ->
                new TransfiniteKeyItem(new Item.Settings()));
        addAfter(registeredKey, ItemGroups.INGREDIENTS, Items.AMETHYST_SHARD);
        return registeredKey;
    }

    public static void registerModItems() {
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
