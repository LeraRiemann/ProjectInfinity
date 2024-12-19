package net.lerariemann.infinity.item.function;

import net.lerariemann.infinity.item.F4Item;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class F4RechargingRecipe extends SpecialCraftingRecipe {
    public F4RechargingRecipe(CraftingRecipeCategory craftingRecipeCategory) {
        super(craftingRecipeCategory);
    }

    public boolean matches(CraftingRecipeInput craftingRecipeInput, World world) {
        boolean foundF4 = false;
        for (int k = 0; k < craftingRecipeInput.getSize(); k++) {
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(k);
            if (itemStack.isOf(ModItems.F4.get())) {
                if (foundF4) return false;
                foundF4 = true;
            }
            else if (!itemStack.isOf(Items.OBSIDIAN)) return false;
        }
        return foundF4;
    }

    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemStack f4 = null;
        int i = 0;
        for (int k = 0; k < craftingRecipeInput.getSize(); k++) {
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(k);
            if (itemStack.isOf(ModItems.F4.get())) f4 = itemStack;
            else if (!itemStack.isEmpty()) i++;
        }
        assert f4 != null;
        int charge = F4Item.getCharge(f4);
        if (i == 0) {
            return Items.OBSIDIAN.getDefaultStack().copyWithCount(Math.min(charge, 64));
        }
        ItemStack result = f4.copy();
        result.applyComponentsFrom(ComponentMap.builder()
                .add(ModItemFunctions.CHARGE.get(), charge + i)
                .build());
        return result;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput craftingRecipeInput) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(craftingRecipeInput.getSize(), ItemStack.EMPTY);
        ItemStack f4 = null;
        int f4pos = 0;
        for (int i = 0; i < defaultedList.size(); i++) {
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(i);
            if (itemStack.isOf(ModItems.F4.get())) {
                f4 = itemStack;
                f4pos = i;
            }
            else if (!itemStack.isEmpty()) return defaultedList;
        }
        assert f4 != null;
        ItemStack result = f4.copy();
        int charge = Math.max(F4Item.getCharge(f4) - 64, 0);
        result.applyComponentsFrom(ComponentMap.builder()
                .add(ModItemFunctions.CHARGE.get(), charge)
                .build());
        defaultedList.set(f4pos, result);
        return defaultedList;
    }

    @Override
    public boolean fits(int width, int height) {
        return width*height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModItemFunctions.F4_RECHARGING.get();
    }
}

