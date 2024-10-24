package net.lerariemann.infinity.item;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.var.ModComponentTypes;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.PlatformMethods.addAfter;
import static net.lerariemann.infinity.PlatformMethods.isModLoaded;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);

    public static final RegistrySupplier<Item> BOOK_BOX_ITEM = registerSimpleBlockItem(ModBlocks.BOOK_BOX, ItemGroups.FUNCTIONAL, Items.CHISELED_BOOKSHELF);
    public static final RegistrySupplier<Item> ALTAR_ITEM = registerSimpleBlockItem(ModBlocks.ALTAR, ItemGroups.FUNCTIONAL, Items.CHISELED_BOOKSHELF);
    public static final RegistrySupplier<Item> TIME_BOMB_ITEM = registerSimpleBlockItem(ModBlocks.TIME_BOMB, ItemGroups.OPERATOR);

    public static final RegistrySupplier<Item> TRANSFINITE_KEY = ITEMS.register("key", () ->
            new TransfiniteKeyItem(new Item.Settings()
                    .component(ModComponentTypes.KEY_DESTINATION.get(), Identifier.of("minecraft:random"))
                    .arch$tab(ItemGroups.INGREDIENTS)));


    public static RegistrySupplier<Item> registerSimpleBlockItem(RegistrySupplier<Block> block, Item.Settings settings) {
        return ITEMS.register(block.getId(), () -> new BlockItem(block.get(), settings));
    }

    public static RegistrySupplier<Item> registerSimpleBlockItem(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group) {
       return registerSimpleBlockItem(block, new Item.Settings().arch$tab(group));
    }

    public static RegistrySupplier<Item> registerSimpleBlockItem(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group, Item item) {
        if (isModLoaded("fabric_item_group_api_v1") || Platform.isFabric()) {
            var blockItem = registerSimpleBlockItem(block, new Item.Settings());
            addAfter(blockItem, group, item);
            return blockItem;
        }
        else {
            return registerSimpleBlockItem(block, group);
        }
    }

    public static void registerModItems() {
        ITEMS.register();
    }
}
