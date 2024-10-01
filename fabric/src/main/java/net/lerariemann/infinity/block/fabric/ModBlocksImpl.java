package net.lerariemann.infinity.block.fabric;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;

public class ModBlocksImpl {
    public static void addAfter(RegistryKey<ItemGroup> functional, Item addBefore, Item item) {
        ItemGroupEvents.modifyEntriesEvent(functional).register(content -> content.addAfter(addBefore, item));
    }

    public static void add(RegistryKey<ItemGroup> functional, Item item) {
        ItemGroupEvents.modifyEntriesEvent(functional).register(content -> content.add(item));

    }
}
