package net.lerariemann.infinity.compat.eiv.portal_crafting;

import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import de.crafty.eiv.common.recipe.util.EivTagUtil;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.recipe.Ingredient;

import java.util.List;

public class PortalCraftingServerRecipe implements IEivServerRecipe {

    //Create a server recipe type (the id does not have to match your client side viewtype id)
    public static final EivRecipeType<PortalCraftingServerRecipe> TYPE = EivRecipeType.register(
            InfinityMethods.getId("portal_crafting"),
            () -> new PortalCraftingServerRecipe(null, null, null)
    );
    private Ingredient input;
    private ItemStack output;
    private String lore;

    public PortalCraftingServerRecipe(Ingredient input, ItemStack output, String lore) {
        this.input = input;
        this.output = output;
        this.lore = lore;
    }

    public Ingredient getIngredient() {
        return this.input;
    }

    public ItemStack getResult() {
        return this.output;
    }

    public String getLore() {
        return this.lore;
    }

    @Override
    public void writeToTag(NbtCompound tag) {
        tag.put("ingredient", EivTagUtil.writeIngredient(this.input));
        tag.put("result", EivTagUtil.encodeItemStack(this.output));
        tag.put("lore", NbtString.of(this.lore));
    }

    @Override
    public void loadFromTag(NbtCompound tag) {
        this.input = EivTagUtil.readIngredient(tag.getCompound("ingredient"));
        this.output = EivTagUtil.decodeItemStack(tag.getCompound("result"));
        this.lore = tag.getString("lore");
    }

    @Override
    public EivRecipeType<? extends IEivServerRecipe> getRecipeType() {
        return TYPE;
    }
}
