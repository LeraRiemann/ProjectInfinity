package net.lerariemann.infinity.item.function;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

public abstract class CollisionCraftingRecipe extends SingleStackRecipe {
    private final Ingredient input;
    private final ItemStack output;
    private final String lore;

    public CollisionCraftingRecipe(Ingredient input, ItemStack output, String lore) {
        super("collision", input, output);
        this.input = input;
        this.output = output;
        this.lore = lore;
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

//    @Override
//    public boolean fits(int width, int height) {
//        return true;
//    }

//    @Override
//    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
//        return output.copy();
//    }

    public Ingredient getInput() {
        return input;
    }
    public String getLore() { return lore; }

    public enum Type implements RecipeType<CollisionCraftingRecipe> {
        PORTAL,
        IRIDESCENCE
    }

    public record Serializer(Function3<Ingredient, ItemStack, String, CollisionCraftingRecipe> func)
            implements RecipeSerializer<CollisionCraftingRecipe> {

        @Override
        public MapCodec<CollisionCraftingRecipe> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Ingredient.CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
                            ItemStack.VALIDATED_CODEC.fieldOf("output").forGetter(recipe -> recipe.output),
                            Codecs.NON_EMPTY_STRING.fieldOf("lore").forGetter(recipe -> recipe.lore))
                    .apply(instance, func));
        }

        @Override
        public PacketCodec<RegistryByteBuf, CollisionCraftingRecipe> packetCodec() {
            return PacketCodec.ofStatic(this::write, this::read);
        }

        private CollisionCraftingRecipe read(RegistryByteBuf buf) {
            Ingredient input = Ingredient.PACKET_CODEC.decode(buf);
            ItemStack output = ItemStack.PACKET_CODEC.decode(buf);
            String str = PacketCodecs.STRING.decode(buf);
            return func.apply(input, output, str);
        }

        private void write(RegistryByteBuf buf, CollisionCraftingRecipe recipe) {
            Ingredient.PACKET_CODEC.encode(buf, recipe.input);
            ItemStack.PACKET_CODEC.encode(buf, recipe.output);
            PacketCodecs.STRING.encode(buf, recipe.lore);
        }
    }

    public static class OfPortal extends CollisionCraftingRecipe {
        public OfPortal(Ingredient input, ItemStack output, String lore) {
            super(input, output, lore);
        }
        @Override
        public RecipeSerializer<? extends SingleStackRecipe> getSerializer() {
            return ModItemFunctions.PORTAL_CRAFTING.get();
        }
        @Override
        public RecipeType<? extends SingleStackRecipe> getType() {
            return ModItemFunctions.PORTAL_CRAFTING_TYPE.get();
        }

        @Override
        public IngredientPlacement getIngredientPlacement() {
            return IngredientPlacement.NONE;
        }

        @Override
        public RecipeBookCategory getRecipeBookCategory() {
            return null;
        }
    }

    public static class OfIridescence extends CollisionCraftingRecipe {
        public OfIridescence(Ingredient input, ItemStack output, String lore) {
            super(input, output, lore);
        }
        @Override
        public RecipeSerializer<? extends SingleStackRecipe> getSerializer() {
            return ModItemFunctions.IRIDESCENCE_CRAFTING.get();
        }
        @Override
        public RecipeType<? extends SingleStackRecipe> getType() {
            return ModItemFunctions.IRIDESCENCE_CRAFTING_TYPE.get();
        }

        @Override
        public IngredientPlacement getIngredientPlacement() {
            return IngredientPlacement.NONE;
        }

        @Override
        public RecipeBookCategory getRecipeBookCategory() {
            return null;
        }
    }
}
