package net.lerariemann.infinity.item.fabric;

import net.lerariemann.infinity.item.StarOfLangItem;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.item.ItemStack;

public class StarOfLangFabricItem extends StarOfLangItem {
    public StarOfLangFabricItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return ModItems.STAR_OF_LANG.get().getDefaultStack();
    }
}
