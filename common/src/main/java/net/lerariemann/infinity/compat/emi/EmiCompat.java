package net.lerariemann.infinity.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.item.function.CollisionCraftingRecipe;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.PlatformMethods;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

@EmiEntrypoint
public class EmiCompat implements EmiPlugin {
    public static final Identifier MY_SPRITE_SHEET = InfinityMethods.getId( "textures/gui/emi_simplified_textures.png");
    public static final EmiStack PORTAL_WORKSTATION = EmiStack.of(ModBlocks.PORTAL.get());
    public static final EmiRecipeCategory PORTAL_CRAFTING
            = new EmiRecipeCategory(ModItemFunctions.PORTAL_CRAFTING.getId(), PORTAL_WORKSTATION, new EmiTexture(MY_SPRITE_SHEET, 0, 0, 16, 16));
    public static EmiStack IRIDESCENCE_WORKSTATION = EmiStack.of(PlatformMethods.getIridescenceStill().get());
    public static EmiStack IRIDESCENCE_CATALYST = IRIDESCENCE_WORKSTATION.copy().setRemainder(IRIDESCENCE_WORKSTATION);
    public static final EmiRecipeCategory IRIDESCENCE_CRAFTING
            = new EmiRecipeCategory(ModItemFunctions.IRIDESCENCE_CRAFTING.getId(), IRIDESCENCE_WORKSTATION, new EmiTexture(MY_SPRITE_SHEET, 0, 0, 16, 16));
    public static final EmiRecipeCategory CHROMATIC
            = new EmiRecipeCategory(InfinityMethods.getId("chromatic"),
            EmiStack.of(ModBlocks.CHROMATIC_WOOL.get()), new EmiTexture(MY_SPRITE_SHEET, 0, 0, 16, 16));

    @Override
    public void register(EmiRegistry registry) {
        // Tell EMI to add a tab for your category
        registry.addCategory(PORTAL_CRAFTING);
        registry.addCategory(IRIDESCENCE_CRAFTING);
        registry.addCategory(CHROMATIC);

        // Add all the workstations your category uses
        registry.addWorkstation(PORTAL_CRAFTING, PORTAL_WORKSTATION);
        registry.addWorkstation(IRIDESCENCE_CRAFTING, IRIDESCENCE_WORKSTATION);
        registry.addWorkstation(CHROMATIC, EmiStack.of(ModBlocks.CHROMATIC_WOOL.get()));
        registry.addWorkstation(CHROMATIC, EmiStack.of(ModBlocks.CHROMATIC_CARPET.get()));

        RecipeManager manager = registry.getRecipeManager();
        DynamicRegistryManager registryManager = MinecraftClient.getInstance().world.getRegistryManager();
        // Use vanilla's concept of your recipes and pass them to your EmiRecipe representation
        for (CollisionCraftingRecipe recipe : manager.listAllOfType(ModItemFunctions.PORTAL_CRAFTING_TYPE.get())) {
            registry.addRecipe(new PortalCraftingEmiRecipe(recipe, registryManager));
        }
        for (CollisionCraftingRecipe recipe : manager.listAllOfType(ModItemFunctions.IRIDESCENCE_CRAFTING_TYPE.get())) {
            registry.addRecipe(new IridesenceCraftingEmiRecipe(recipe, registryManager));
        }
        registry.addRecipe(ChromaticEmiRecipe.withInfo("color", ModItems.CHROMATIC_MATTER.get()));
        registry.addRecipe(ChromaticEmiRecipe.withInfo("hue", ModItems.IRIDESCENT_STAR.get()));
        registry.addRecipe(ChromaticEmiRecipe.of("saturation_plus", Items.AMETHYST_SHARD));
        registry.addRecipe(ChromaticEmiRecipe.of("saturation_minus", ModItems.FOOTPRINT.get()));
        registry.addRecipe(ChromaticEmiRecipe.of("brightness_plus", ModItems.WHITE_MATTER.get()));
        registry.addRecipe(ChromaticEmiRecipe.of("brightness_minus", ModItems.BLACK_MATTER.get()));

        ItemStack awkward = Items.POTION.getDefaultStack();
        awkward.apply(DataComponentTypes.POTION_CONTENTS,
                PotionContentsComponent.DEFAULT,
                Potions.AWKWARD,
                PotionContentsComponent::with);
        EmiRecipe chromaBrew = new EmiBrewingRecipe(EmiStack.of(awkward),
                EmiIngredient.of(Ingredient.ofItems(ModItems.CHROMATIC_MATTER.get())),
                EmiStack.of(ModItems.CHROMATIC_POTION.get().getDefaultStack()),
                InfinityMethods.getId("/chromatic_bottle"));
        registry.addRecipe(chromaBrew);

        registry.addRecipe(EmiWorldInteractionRecipe.builder()
                .id(InfinityMethods.getId("/iridescence/bottling"))
                .leftInput(EmiStack.of(Items.GLASS_BOTTLE.getDefaultStack()))
                .rightInput(IRIDESCENCE_CATALYST, false)
                .output(EmiStack.of(ModItems.IRIDESCENT_POTION.get().getDefaultStack()))
                .build());
        registry.addRecipe(EmiWorldInteractionRecipe.builder()
                .id(InfinityMethods.getId("/iridescence/wool_uncolor"))
                .leftInput(EmiStack.of(ModItems.IRIDESCENT_WOOL.get().getDefaultStack()))
                .rightInput(EmiStack.of(ModItems.STAR_OF_LANG.get().getDefaultStack()), true)
                .output(EmiStack.of(ModItems.CHROMATIC_WOOL.get().getDefaultStack()))
                .build());
        registry.addRecipe(EmiWorldInteractionRecipe.builder()
                .id(InfinityMethods.getId("/iridescence/carpet_uncolor"))
                .leftInput(EmiStack.of(ModItems.IRIDESCENT_CARPET.get().getDefaultStack()))
                .rightInput(EmiStack.of(ModItems.STAR_OF_LANG.get().getDefaultStack()), true)
                .output(EmiStack.of(ModItems.CHROMATIC_CARPET.get().getDefaultStack()))
                .build());
    }
}
