package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.custom.BiomeBottle;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class BiomeBottleCombiningRecipe extends SpecialCraftingRecipe {
    public BiomeBottleCombiningRecipe(CraftingRecipeCategory craftingRecipeCategory) {
        super(craftingRecipeCategory);
    }

    public boolean matches(CraftingRecipeInput craftingRecipeInput, World world) {
        ItemStack stack1 = null;
        boolean bl = true;
        int charge = 0;
        for (int k = 0; k < craftingRecipeInput.getSize(); k++) {
            if (!bl) return false;
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(k);
            if (!itemStack.isEmpty()) {
                if (Block.getBlockFromItem(itemStack.getItem()) instanceof BiomeBottle) {
                    charge += BiomeBottle.getCharge(itemStack);
                    if (stack1 == null) {
                        stack1 = itemStack;
                        bl = !BiomeBottle.isEmpty(stack1);
                    }
                    else bl = !BiomeBottle.isEmpty(itemStack)
                            && BiomeBottle.getBiome(stack1).equals(BiomeBottle.getBiome(itemStack));
                }
                else return false;
            }
        }
        return bl && (stack1 != null) && (charge < BiomeBottle.maxAllowedCharge);
    }

    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemStack stack1 = ItemStack.EMPTY;
        int i = 0;
        int charge = 0;
        for (int k = 0; k < craftingRecipeInput.getSize(); k++) {
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(k);
            if (!itemStack.isEmpty()) {
                if (Block.getBlockFromItem(itemStack.getItem()) instanceof BiomeBottle) {
                    if (stack1 == ItemStack.EMPTY) stack1 = itemStack;
                    charge += BiomeBottle.getCharge(itemStack);
                    i += 1;
                }
            }
        }
        if (i < 2) return ModItems.BIOME_BOTTLE_ITEM.get().getDefaultStack();
        ItemStack result = stack1.copy();
        BiomeBottle.updateLevel(result, charge);
        return result;
    }

    @Override
    public boolean fits(int width, int height) {
        return width*height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModItemFunctions.BIOME_BOTTLE_COMBINING.get();
    }
}
