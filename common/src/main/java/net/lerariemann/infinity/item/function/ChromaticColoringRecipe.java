package net.lerariemann.infinity.item.function;

import com.google.gson.JsonObject;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public class ChromaticColoringRecipe implements CraftingRecipe {
    private final Ingredient input;
    private final ItemStack output;

    public ChromaticColoringRecipe(Ingredient input, ItemStack output) {
        this.input = input;
        this.output = output;
    }
    @Override
    public CraftingRecipeCategory getCategory() {
        return CraftingRecipeCategory.MISC;
    }

    @Override
    public boolean matches(RecipeInputInventory input, World world) {
        boolean foundChroma = false;
        for (int k = 0; k < input.getInputStacks().size(); k++) {
            ItemStack itemStack = input.getStack(k);
            if (itemStack.isOf(ModItems.CHROMATIC_MATTER.get())) {
                if (foundChroma) return false;
                foundChroma = true;
            }
            else if (!itemStack.isEmpty()) {
                if (!this.input.test(itemStack)) return false;
            }
        }
        return foundChroma;
    }

    @Override
    public ItemStack craft(RecipeInputInventory input, DynamicRegistryManager lookup) {
        ItemStack chroma = ItemStack.EMPTY;
        int i = 0;
        for (int k = 0; k < input.getInputStacks().size(); k++) {
            ItemStack itemStack = input.getStack(k);
            if (itemStack.isOf(ModItems.CHROMATIC_MATTER.get())) chroma = itemStack;
            else if (!itemStack.isEmpty()) {
                i++;
            }
        }
        assert !chroma.isEmpty();
        if (i > 0) {
            ItemStack result = output.copyWithCount(i);
            result.setNbt(chroma.getNbt());
            return result;
        }
        else return chroma.getItem().getDefaultStack();
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory craftingRecipeInput) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(craftingRecipeInput.getInputStacks().size(), ItemStack.EMPTY);
        int j = 0;
        for (int i = 0; i < defaultedList.size(); i++) {
            ItemStack itemStack = craftingRecipeInput.getStack(i);
            if (itemStack.isOf(ModItems.CHROMATIC_MATTER.get()))
                defaultedList.set(i, itemStack.copyWithCount(1));
            else if (!itemStack.isEmpty()) j++;
        }
        return j > 0 ? defaultedList : DefaultedList.ofSize(craftingRecipeInput.getInputStacks().size(), ItemStack.EMPTY);
    }

    @Override
    public boolean fits(int width, int height) {
        return width*height > 1;
    }

    @Override
    public Identifier getId() {
        Identifier id = Registries.ITEM.getId(output.getItem());
        return Identifier.of(id.getNamespace(), id.getPath()+"_chromatic");
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registriesLookup) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModItemFunctions.CHROMATIC_COLORING.get();
    }

    public record Serializer(BiFunction<Ingredient, ItemStack, ChromaticColoringRecipe> func)
            implements RecipeSerializer<ChromaticColoringRecipe> {

        @Override
        public ChromaticColoringRecipe read(Identifier id, JsonObject json) {
            Ingredient input = Ingredient.fromJson(JsonHelper.getElement(json, "input"));
            ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "output"));
            return func.apply(input, output);
        }

        @Override
        public ChromaticColoringRecipe read(Identifier id, PacketByteBuf buf) {
            Ingredient input = Ingredient.fromPacket(buf);
            ItemStack output = buf.readItemStack();
            return func.apply(input, output);
        }

        @Override
        public void write(PacketByteBuf buf, ChromaticColoringRecipe recipe) {
            recipe.input.write(buf);
            buf.writeItemStack(recipe.output);
        }
    }
}
