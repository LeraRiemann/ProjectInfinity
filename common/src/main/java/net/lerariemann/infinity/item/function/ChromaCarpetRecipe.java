package net.lerariemann.infinity.item.function;

import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class ChromaCarpetRecipe extends SpecialCraftingRecipe {
    public ChromaCarpetRecipe(CraftingRecipeCategory craftingRecipeCategory) {
        super(craftingRecipeCategory);
    }

    public boolean matches(CraftingRecipeInput craftingRecipeInput, World world) {
        Integer color = null;
        int k_first = 0;
        boolean carpetDone = false;
        int w = craftingRecipeInput.getWidth();
        for (int k = 0; k < craftingRecipeInput.getSize(); k++) {
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(k);
            if (itemStack.isOf(ModItems.CHROMATIC_WOOL.get())) {
                if (carpetDone) return false;
                int newColor = itemStack.getOrDefault(ModComponentTypes.COLOR.get(), 0);
                if (color == null) {
                    color = newColor;
                    if (k % w == w - 1) return false;
                    k_first = k;
                }
                else {
                    if (k - k_first != 1 || color != newColor) return false;
                    carpetDone = true;
                }
            }
            else if (!itemStack.isEmpty()) return false;
        }
        return carpetDone;
    }

    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        int color = 0;
        for (int k = 0; k < craftingRecipeInput.getSize(); k++) {
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(k);
            if (itemStack.isOf(ModItems.CHROMATIC_WOOL.get())) {
                color = itemStack.getOrDefault(ModComponentTypes.COLOR.get(), 0);
                break;
            }
        }
        ItemStack result = ModItems.CHROMATIC_CARPET.get().getDefaultStack();
        result.applyComponentsFrom(ComponentMap.builder() //"adding obsidian" recipe
                .add(ModComponentTypes.COLOR.get(), color)
                .build());
        return result.copyWithCount(3);
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 2 && height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModItemFunctions.CARPET.get();
    }
}

