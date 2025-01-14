package net.lerariemann.infinity.item.function;

import net.lerariemann.infinity.block.custom.BiomeBottleBlock;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.registry.core.ModItems;
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
                if (Block.getBlockFromItem(itemStack.getItem()) instanceof BiomeBottleBlock) {
                    charge += BiomeBottleBlock.getCharge(itemStack);
                    if (stack1 == null) {
                        stack1 = itemStack;
                        bl = !BiomeBottleBlock.isEmpty(stack1);
                    }
                    else bl = !BiomeBottleBlock.isEmpty(itemStack)
                            && BiomeBottleBlock.getBiome(stack1).equals(BiomeBottleBlock.getBiome(itemStack));
                }
                else return false;
            }
        }
        return bl && (stack1 != null) && (charge < BiomeBottleBlock.maxAllowedCharge);
    }

    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemStack stack1 = ItemStack.EMPTY;
        int i = 0;
        int charge = 0;
        for (int k = 0; k < craftingRecipeInput.getSize(); k++) {
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(k);
            if (!itemStack.isEmpty()) {
                if (Block.getBlockFromItem(itemStack.getItem()) instanceof BiomeBottleBlock) {
                    if (stack1 == ItemStack.EMPTY) stack1 = itemStack;
                    charge += BiomeBottleBlock.getCharge(itemStack);
                    i += 1;
                }
            }
        }
        if (i < 2) return ModItems.BIOME_BOTTLE_ITEM.get().getDefaultStack();
        ItemStack result = stack1.copy();
        BiomeBottleBlock.updateCharge(result, charge);
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
