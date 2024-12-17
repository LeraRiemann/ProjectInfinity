package net.lerariemann.infinity.item.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public abstract class CollisionCraftingRecipe implements Recipe<SingleStackRecipeInput> {
    private final Ingredient input;
    private final ItemStack output;

    public CollisionCraftingRecipe(Ingredient input, ItemStack output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(SingleStackRecipeInput input, World world) {
        if (world.isClient) return false;
        return this.input.test(input.item());
    }

    @Override
    public ItemStack craft(SingleStackRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        return output;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return output.copy();
    }


    public Ingredient getInput() {
        return input;
    }

    public enum Type implements RecipeType<CollisionCraftingRecipe> {
        PORTAL,
        IRIDESCENCE
    }

    public record Serializer(BiFunction<Ingredient, ItemStack, CollisionCraftingRecipe> func)
            implements RecipeSerializer<CollisionCraftingRecipe> {

        @Override
        public MapCodec<CollisionCraftingRecipe> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
                            ItemStack.VALIDATED_CODEC.fieldOf("output").forGetter(recipe -> recipe.output))
                    .apply(instance, func));
        }

        @Override
        public PacketCodec<RegistryByteBuf, CollisionCraftingRecipe> packetCodec() {
            return PacketCodec.ofStatic(this::write, this::read);
        }

        private CollisionCraftingRecipe read(RegistryByteBuf buf) {
            Ingredient input = Ingredient.PACKET_CODEC.decode(buf);
            ItemStack output = ItemStack.PACKET_CODEC.decode(buf);
            return func.apply(input, output);
        }

        private void write(RegistryByteBuf buf, CollisionCraftingRecipe recipe) {
            Ingredient.PACKET_CODEC.encode(buf, recipe.input);
            ItemStack.PACKET_CODEC.encode(buf, recipe.output);
        }
    }

    public static class OfPortal extends CollisionCraftingRecipe {
        public OfPortal(Ingredient input, ItemStack output) {
            super(input, output);
        }
        @Override
        public RecipeSerializer<?> getSerializer() {
            return ModItemFunctions.PORTAL_CRAFTING.get();
        }
        @Override
        public RecipeType<?> getType() {
            return ModItemFunctions.PORTAL_CRAFTING_TYPE.get();
        }
    }

    public static class OfIridescence extends CollisionCraftingRecipe {
        public OfIridescence(Ingredient input, ItemStack output) {
            super(input, output);
        }
        @Override
        public RecipeSerializer<?> getSerializer() {
            return ModItemFunctions.IRIDESCENCE_CRAFTING.get();
        }
        @Override
        public RecipeType<?> getType() {
            return ModItemFunctions.IRIDESCENCE_CRAFTING_TYPE.get();
        }
    }
}
