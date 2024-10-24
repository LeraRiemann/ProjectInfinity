package net.lerariemann.infinity.item;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.var.ModComponentTypes;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);

    public static final RegistrySupplier<Item> BOOK_BOX_ITEM = registerSimpleBlockItem(ModBlocks.BOOK_BOX, ItemGroups.FUNCTIONAL);
    public static final RegistrySupplier<Item> ALTAR_ITEM = registerSimpleBlockItem(ModBlocks.ALTAR, ItemGroups.FUNCTIONAL);
    public static final RegistrySupplier<Item> TIME_BOMB_ITEM = registerSimpleBlockItem(ModBlocks.TIME_BOMB, ItemGroups.OPERATOR);
    
    public static final RegistrySupplier<Item> TRANSFINITE_KEY = ITEMS.register("key", () ->
            new TransfiniteKeyItem(new Item.Settings()
                    .component(ModComponentTypes.KEY_DESTINATION.get(), Identifier.of("minecraft:random"))
                    .arch$tab(ItemGroups.INGREDIENTS)));

    public static RegistrySupplier<Item> registerSimpleBlockItem(RegistrySupplier<Block> block, RegistryKey<ItemGroup> group) {
       return ITEMS.register(block.getId(), () -> new BlockItem(block.get(), new Item.Settings().arch$tab(group)));
    }

    public static void registerModItems() {
        ITEMS.register();
    }
}
