package net.lerariemann.infinity.item.function;

import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.Map;

public class ChromaticColoringRecipe extends SpecialCraftingRecipe {
    public ChromaticColoringRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    public static final Map<TagKey<Item>, RegistrySupplier<? extends Item>> map = Map.ofEntries(
            Map.entry(ItemTags.WOOL, ModItems.CHROMATIC_WOOL),
            Map.entry(ItemTags.WOOL_CARPETS, ModItems.CHROMATIC_CARPET));

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        boolean foundChroma = false;
        TagKey<Item> itemTag = null;
        for (int k = 0; k < input.getSize(); k++) {
            ItemStack itemStack = input.getStackInSlot(k);
            if (itemStack.isOf(ModItems.CHROMATIC_MATTER.get())) {
                if (foundChroma) return false;
                foundChroma = true;
            }
            else if (!itemStack.isEmpty()) {
                RegistryEntry<Item> entry = itemStack.getItem().getRegistryEntry();
                if (itemTag == null) {
                    for (TagKey<Item> tag: map.keySet()) if (entry.isIn(tag)) itemTag = tag;
                }
                else if (!entry.isIn(itemTag)) return false;
            }
        }
        return (itemTag != null && foundChroma);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack chroma = null;
        ItemStack result = null;
        int i = 0;
        for (int k = 0; k < input.getSize(); k++) {
            ItemStack itemStack = input.getStackInSlot(k);
            if (itemStack.isOf(ModItems.CHROMATIC_MATTER.get())) chroma = itemStack;
            else if (!itemStack.isEmpty()) {
                RegistryEntry<Item> entry = itemStack.getItem().getRegistryEntry();
                if (result == null) for (TagKey<Item> tag: map.keySet()) if (entry.isIn(tag))
                            result = map.get(tag).get().getDefaultStack();
                i++;
            }
        }
        assert result != null && chroma != null && i > 0;
        result.applyComponentsFrom(chroma.getComponents());
        return result.copyWithCount(i);
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput craftingRecipeInput) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(craftingRecipeInput.getSize(), ItemStack.EMPTY);
        for (int i = 0; i < defaultedList.size(); i++) {
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(i);
            if (itemStack.isOf(ModItems.CHROMATIC_MATTER.get())) {
                defaultedList.set(i, itemStack.copyWithCount(1));
            }
        }
        return defaultedList;
    }

    @Override
    public boolean fits(int width, int height) {
        return width*height > 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModItemFunctions.CHROMATIC_COLORING.get();
    }
}
