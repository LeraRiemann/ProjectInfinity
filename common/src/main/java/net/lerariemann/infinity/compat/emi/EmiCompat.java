package net.lerariemann.infinity.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.item.ModItems;
import net.lerariemann.infinity.item.function.CollisionCraftingRecipe;
import net.lerariemann.infinity.item.function.ModItemFunctions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

@EmiEntrypoint
public class EmiCompat implements EmiPlugin {
    public static final Identifier MY_SPRITE_SHEET = InfinityMethods.getId( "textures/gui/emi_simplified_textures.png");
    public static final EmiStack PORTAL_WORKSTATION = EmiStack.of(ModBlocks.PORTAL.get());
    public static final EmiRecipeCategory PORTAL_CRAFTING
            = new EmiRecipeCategory(ModItemFunctions.PORTAL_CRAFTING.getId(), PORTAL_WORKSTATION, new EmiTexture(MY_SPRITE_SHEET, 0, 0, 16, 16));
    public static final EmiStack IRIDESENCE_WORKSTATION = EmiStack.of(ModItems.IRIDESCENCE_BUCKET.get());
    public static final EmiRecipeCategory IRIDESENCE_CRAFTING
            = new EmiRecipeCategory(ModItemFunctions.IRIDESCENCE_CRAFTING.getId(), IRIDESENCE_WORKSTATION, new EmiTexture(MY_SPRITE_SHEET, 0, 0, 16, 16));

    @Override
    public void register(EmiRegistry registry) {
        // Tell EMI to add a tab for your category
        registry.addCategory(PORTAL_CRAFTING);
        registry.addCategory(IRIDESENCE_CRAFTING);

        // Add all the workstations your category uses
        registry.addWorkstation(PORTAL_CRAFTING, PORTAL_WORKSTATION);
        registry.addWorkstation(IRIDESENCE_CRAFTING, IRIDESENCE_WORKSTATION);
        RecipeManager manager = registry.getRecipeManager();
        DynamicRegistryManager registryManager = MinecraftClient.getInstance().world.getRegistryManager();
        // Use vanilla's concept of your recipes and pass them to your EmiRecipe representation
        for (RecipeEntry<CollisionCraftingRecipe> recipe : manager.listAllOfType(ModItemFunctions.PORTAL_CRAFTING_TYPE.get())) {
            registry.addRecipe(new PortalCraftingEmiRecipe(recipe, registryManager));
        }
        for (RecipeEntry<CollisionCraftingRecipe> recipe : manager.listAllOfType(ModItemFunctions.IRIDESCENCE_CRAFTING_TYPE.get())) {
            registry.addRecipe(new IridesenceCraftingEmiRecipe(recipe, registryManager));
        }
    }
}
