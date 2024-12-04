package net.lerariemann.infinity.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.item.ModItems;
import net.lerariemann.infinity.item.function.CollisionCraftingRecipe;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.DynamicRegistryManager;

public class PortalCraftingEmiRecipe extends BasicEmiRecipe {

    public PortalCraftingEmiRecipe(RecipeEntry<CollisionCraftingRecipe> recipeEntry, DynamicRegistryManager registryManager) {
        super(EmiCompat.PORTAL_CRAFTING, recipeEntry.id(), 118, 18);
        CollisionCraftingRecipe recipe = recipeEntry.value();
        this.inputs.add(EmiIngredient.of(recipe.getInput()));
        this.outputs.add(EmiStack.of(recipe.getResult(registryManager)));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Adds an input slot on the left
        widgets.addSlot(inputs.getFirst(), 0, 0);

        // Add a plus texture to indicate processing
        widgets.addTexture(EmiTexture.PLUS, 26, 2);

        // Add the portal block texture.
        widgets.addSlot(EmiStack.of(ModBlocks.PORTAL.get()), 48, 0);

        // Add an arrow texture to indicate processing
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 70, 1);

        // Adds an output slot on the right
        // Note that output slots need to call `recipeContext` to inform EMI about their recipe context
        // This includes being able to resolve recipe trees, favorite stacks with recipe context, and more
        widgets.addSlot(outputs.getFirst(), 100, 0).recipeContext(this);
    }
}
