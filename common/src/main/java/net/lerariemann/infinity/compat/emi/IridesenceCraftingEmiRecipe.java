package net.lerariemann.infinity.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.lerariemann.infinity.item.function.CollisionCraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;

import java.util.Objects;

public class IridesenceCraftingEmiRecipe extends BasicEmiRecipe {
    String lore;

    public IridesenceCraftingEmiRecipe(RecipeEntry<CollisionCraftingRecipe> recipeEntry, DynamicRegistryManager registryManager) {
        super(EmiCompat.IRIDESCENCE_CRAFTING, recipeEntry.id(), 118, 18);
        CollisionCraftingRecipe recipe = recipeEntry.value();
        this.inputs.add(EmiIngredient.of(recipe.getInput()));
        this.outputs.add(EmiStack.of(recipe.getResult(registryManager)));
        this.lore = recipeEntry.value().getLore();
        if (!Objects.equals(lore, "empty")) {
            this.width = 130;
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.getFirst(), 0, 0);
        widgets.addTexture(EmiTexture.PLUS, 26, 2);
        widgets.addSlot(EmiCompat.IRIDESCENCE_WORKSTATION, 48, 0);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 70, 0);
        widgets.addSlot(outputs.getFirst(), 100, 0).recipeContext(this);

        // lore handling
        if (!Objects.equals(lore, "empty")) {
            EmiCompat.addInfo(widgets, 120, 5, Text.translatable(lore));
        }
    }
}
