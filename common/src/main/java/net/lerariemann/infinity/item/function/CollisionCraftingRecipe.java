package net.lerariemann.infinity.item.function;

import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public abstract class CollisionCraftingRecipe implements Recipe<Inventory> {
    private final Ingredient input;
    private final ItemStack output;

    public CollisionCraftingRecipe(Ingredient input, ItemStack output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(Inventory input, World world) {
        if (world.isClient) return false;
        return this.input.test(input.getStack(0));
    }

    @Override
    public ItemStack craft(Inventory input, DynamicRegistryManager lookup) {
        return output;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registriesLookup) {
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

        public CollisionCraftingRecipe read(Identifier id, JsonObject json) {
            Ingredient input = Ingredient.fromJson(JsonHelper.getElement(json, "input"));
            ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "output"));
            return func.apply(input, output);
        }

        @Override
        public CollisionCraftingRecipe read(Identifier id, PacketByteBuf buf) {
            Ingredient input = Ingredient.fromPacket(buf);
            ItemStack output = buf.readItemStack();
            return func.apply(input, output);
        }

        public void write(PacketByteBuf buf, CollisionCraftingRecipe recipe) {
            recipe.input.write(buf);
            buf.writeItemStack(recipe.output);
        }
    }

    public static class OfPortal extends CollisionCraftingRecipe {
        ItemStack output;
        public OfPortal(Ingredient input, ItemStack output) {
            super(input, output);
            this.output = output;
        }

        @Override
        public Identifier getId() {
            Identifier id = Registries.ITEM.getId(output.getItem());
            return Identifier.of(id.getNamespace(), id.getPath()+"_of_portal");
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
        ItemStack output;

        public OfIridescence(Ingredient input, ItemStack output) {
            super(input, output);
            this.output = output;
        }

        @Override
        public Identifier getId() {
            Identifier id = Registries.ITEM.getId(output.getItem());
            return Identifier.of(id.getNamespace(), id.getPath()+"_of_iridescence");
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
