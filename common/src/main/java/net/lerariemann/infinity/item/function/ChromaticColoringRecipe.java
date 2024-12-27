package net.lerariemann.infinity.item.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
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
    public boolean matches(CraftingRecipeInput input, World world) {
        boolean foundChroma = false;
        for (int k = 0; k < input.getSize(); k++) {
            ItemStack itemStack = input.getStackInSlot(k);
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
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack chroma = ItemStack.EMPTY;
        int i = 0;
        for (int k = 0; k < input.getSize(); k++) {
            ItemStack itemStack = input.getStackInSlot(k);
            if (itemStack.isOf(ModItems.CHROMATIC_MATTER.get())) chroma = itemStack;
            else if (!itemStack.isEmpty()) {
                i++;
            }
        }
        assert !chroma.isEmpty();
        if (i > 0) {
            ItemStack result = output.copyWithCount(i);
            result.applyComponentsFrom(chroma.getComponents());
            return result;
        }
        else return chroma.getItem().getDefaultStack();
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput craftingRecipeInput) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(craftingRecipeInput.getSize(), ItemStack.EMPTY);
        int j = 0;
        for (int i = 0; i < defaultedList.size(); i++) {
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(i);
            if (itemStack.isOf(ModItems.CHROMATIC_MATTER.get()))
                defaultedList.set(i, itemStack.copyWithCount(1));
            else if (!itemStack.isEmpty()) j++;
        }
        return j > 0 ? defaultedList : DefaultedList.ofSize(craftingRecipeInput.getSize(), ItemStack.EMPTY);
    }

    @Override
    public boolean fits(int width, int height) {
        return width*height > 1;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModItemFunctions.CHROMATIC_COLORING.get();
    }

    public record Serializer(BiFunction<Ingredient, ItemStack, ChromaticColoringRecipe> func)
            implements RecipeSerializer<ChromaticColoringRecipe> {

        @Override
        public MapCodec<ChromaticColoringRecipe> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
                            ItemStack.VALIDATED_CODEC.fieldOf("output").forGetter(recipe -> recipe.output))
                    .apply(instance, func));
        }

        @Override
        public PacketCodec<RegistryByteBuf, ChromaticColoringRecipe> packetCodec() {
            return PacketCodec.ofStatic(this::write, this::read);
        }

        private ChromaticColoringRecipe read(RegistryByteBuf buf) {
            Ingredient input = Ingredient.PACKET_CODEC.decode(buf);
            ItemStack output = ItemStack.PACKET_CODEC.decode(buf);
            return func.apply(input, output);
        }

        private void write(RegistryByteBuf buf, ChromaticColoringRecipe recipe) {
            Ingredient.PACKET_CODEC.encode(buf, recipe.input);
            ItemStack.PACKET_CODEC.encode(buf, recipe.output);
        }
    }
}
