package net.lerariemann.infinity.item;

import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class StarOfLangItem extends Item {
    public StarOfLangItem(Settings settings) {
        super(settings);
    }
    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return ModItems.STAR_OF_LANG.get().getDefaultStack();
    }

    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
