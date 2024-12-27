package net.lerariemann.infinity.item.neoforge;

import net.lerariemann.infinity.item.StarOfLangItem;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StarOfLangItemNeoforge extends StarOfLangItem {
    public StarOfLangItemNeoforge(Settings settings) {
        super(settings);
    }
    @Override
    public @NotNull ItemStack getCraftingRemainingItem(@NotNull ItemStack itemStack) {
        return new ItemStack(ModItems.STAR_OF_LANG.get());
    }
    @Override
    public boolean hasCraftingRemainingItem(@NotNull ItemStack stack) {
        return true;
    }
}
