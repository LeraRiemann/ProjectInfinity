package net.lerariemann.infinity.item.fabric;

import net.lerariemann.infinity.item.StarOfLangItem;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class StarOfLangItemFabric extends StarOfLangItem {
    public StarOfLangItemFabric(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return ModItems.STAR_OF_LANG.get().getDefaultStack();
    }
    @Override
    public boolean hasRecipeRemainder() {
        return true;
    }
}
