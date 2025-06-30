package net.lerariemann.infinity.compat.eiv;

import de.crafty.eiv.common.api.IExtendedItemViewIntegration;
import de.crafty.eiv.common.api.recipe.ItemView;
import de.crafty.eiv.common.recipe.ServerRecipeManager;
import net.lerariemann.infinity.compat.eiv.portal_crafting.PortalCraftingServerRecipe;
import net.lerariemann.infinity.compat.eiv.portal_crafting.PortalCraftingViewRecipe;
import net.lerariemann.infinity.item.function.CollisionCraftingRecipe;

import java.util.Collections;
import java.util.List;

public class EivIntegration implements IExtendedItemViewIntegration {
    @Override
    public void onIntegrationInitialize() {
        // Serverside recipes
        ItemView.addRecipeProvider(recipeList -> {
            // Portal Crafting - serverside
            ServerRecipeManager.INSTANCE.getRecipesForType(CollisionCraftingRecipe.Type.PORTAL).forEach(recipe -> recipeList.add(new PortalCraftingServerRecipe(recipe.getInput(), recipe.getResult(), recipe.getLore())));
        });
        // Portal Crafting - Clientside
        ItemView.registerRecipeWrapper(PortalCraftingServerRecipe.TYPE, modRecipe -> Collections.singletonList(new PortalCraftingViewRecipe(modRecipe)));
    }
}
