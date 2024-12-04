package net.lerariemann.infinity.item.function;

import net.lerariemann.infinity.block.custom.BiomeBottle;
import net.lerariemann.infinity.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class BiomeBottleCombiningRecipe extends SpecialCraftingRecipe {
    public BiomeBottleCombiningRecipe(Identifier id, CraftingRecipeCategory craftingRecipeCategory) {
        super(id, craftingRecipeCategory);
    }

    public boolean matches(RecipeInputInventory inventory, World world) {
        ItemStack stack1 = null;
        boolean bl = true;
        int charge = 0;
        for (int k = 0; k < inventory.getInputStacks().size(); k++) {
            if (!bl) return false;
            ItemStack itemStack = inventory.getStack(k);
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

    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack stack1 = ItemStack.EMPTY;
        int bottles = 0;
        int charge = 0;
        for (int k = 0; k < inventory.size(); k++) {
            ItemStack itemStack = inventory.getStack(k);
            if (!itemStack.isEmpty()) {
                if (itemStack.getItem().equals(ModItems.BIOME_BOTTLE_ITEM.get())) {
                    if (stack1 == ItemStack.EMPTY) stack1 = itemStack;
                    charge += BiomeBottle.getCharge(itemStack);
                    bottles += 1;
                }
            }
        }
        if (bottles < 2) {
            return ModItems.BIOME_BOTTLE_ITEM.get().getDefaultStack();
        }

        ItemStack result = stack1.copy();
        BiomeBottle.updateCharge(result, charge);
        result.setCount(1);
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
