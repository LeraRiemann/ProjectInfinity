package net.lerariemann.infinity.item.function;

import net.lerariemann.infinity.item.F4Item;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.util.BackportMethods;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class F4RechargingRecipe extends SpecialCraftingRecipe {
    public F4RechargingRecipe(Identifier id, CraftingRecipeCategory craftingRecipeCategory) {
        super(id, craftingRecipeCategory);
    }

    public boolean matches(RecipeInputInventory inventory, World world) {
        boolean foundF4 = false;
        for (int k = 0; k < inventory.getInputStacks().size(); k++) {
            ItemStack itemStack = inventory.getStack(k);
            if (itemStack.isOf(ModItems.F4.get())) {
                if (foundF4) return false;
                foundF4 = true;
            }
            else if (!itemStack.isOf(Items.OBSIDIAN)) return false;
        }
        return foundF4;
    }

    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack f4 = null;
        int i = 0;
        for (int k = 0; k < inventory.getInputStacks().size(); k++) {
            ItemStack itemStack = inventory.getStack(k);
            if (itemStack.isOf(ModItems.F4.get())) f4 = itemStack;
            else if (!itemStack.isEmpty()) i++;
        }
        assert f4 != null;
        int charge = F4Item.getCharge(f4);
        if (i == 0) {
            if (charge > 0) return Items.OBSIDIAN.getDefaultStack().copyWithCount(Math.min(charge, 64)); //"removing obsidian" recipe
        }
        ItemStack result = f4.copy();
        BackportMethods.apply(result, ModItemFunctions.F4_CHARGE, charge+i);
        return result;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory craftingRecipeInput) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(craftingRecipeInput.getInputStacks().size(), ItemStack.EMPTY);
        ItemStack f4 = null;
        int f4pos = 0;
        for (int i = 0; i < defaultedList.size(); i++) {
            ItemStack itemStack = craftingRecipeInput.getStack(i);
            if (itemStack.isOf(ModItems.F4.get())) {
                f4 = itemStack;
                f4pos = i;
            }
            else if (!itemStack.isEmpty()) return defaultedList;//"adding obsidian" recipe
        }
        assert f4 != null;
        int charge = F4Item.getCharge(f4);
        if (charge == 0) return defaultedList; //"clearing data" recipe
        charge = Math.max(charge - 64, 0);
        ItemStack result = f4.copy();
        int charge = Math.max(F4Item.getCharge(f4) - 64, 0);
        BackportMethods.apply(result, ModItemFunctions.F4_CHARGE, charge);
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

