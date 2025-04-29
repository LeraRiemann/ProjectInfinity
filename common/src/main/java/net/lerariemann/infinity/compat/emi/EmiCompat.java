package net.lerariemann.infinity.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.lerariemann.infinity.item.function.ChromaticColoringRecipe;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.item.function.CollisionCraftingRecipe;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.PlatformMethods;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

@EmiEntrypoint
public class EmiCompat implements EmiPlugin {
    public static final Identifier INFINITY_SPRITES = InfinityMethods.getId( "textures/gui/emi_simplified_textures.png");
    public static final EmiStack PORTAL_WORKSTATION = EmiStack.of(ModItems.PORTAL_ITEM.get());
    public static final EmiRecipeCategory PORTAL_CRAFTING
            = new EmiRecipeCategory(ModItemFunctions.PORTAL_CRAFTING.getId(), PORTAL_WORKSTATION, new EmiTexture(INFINITY_SPRITES, 0, 0, 16, 16));
    public static EmiStack IRIDESCENCE_WORKSTATION = EmiStack.of(PlatformMethods.getIridescenceStill().get());
    public static EmiStack IRIDESCENCE_CATALYST = IRIDESCENCE_WORKSTATION.copy().setRemainder(IRIDESCENCE_WORKSTATION);

    public static final EmiRecipeCategory IRIDESCENCE_CRAFTING
            = new EmiRecipeCategory(ModItemFunctions.IRIDESCENCE_CRAFTING.getId(), IRIDESCENCE_WORKSTATION, new EmiTexture(INFINITY_SPRITES, 0, 0, 16, 16));
    public static final EmiRecipeCategory CHROMATIC
            = new EmiRecipeCategory(InfinityMethods.getId("chromatic"),
            EmiStack.of(ModBlocks.CHROMATIC_WOOL.get()), new EmiTexture(INFINITY_SPRITES, 0, 0, 16, 16));

    @Override
    public void register(EmiRegistry registry) {
        RecipeManager manager = registry.getRecipeManager();
        DynamicRegistryManager registryManager = MinecraftClient.getInstance().world.getRegistryManager();

        //collision recipes
        registry.addCategory(PORTAL_CRAFTING);
        registry.addWorkstation(PORTAL_CRAFTING, PORTAL_WORKSTATION);
        for (RecipeEntry<CollisionCraftingRecipe> recipe : manager.listAllOfType(ModItemFunctions.PORTAL_CRAFTING_TYPE.get())) {
            registry.addRecipe(new PortalCraftingEmiRecipe(recipe, registryManager));
        }
        registry.addCategory(IRIDESCENCE_CRAFTING);
        registry.addWorkstation(IRIDESCENCE_CRAFTING, IRIDESCENCE_WORKSTATION);
        for (RecipeEntry<CollisionCraftingRecipe> recipe : manager.listAllOfType(ModItemFunctions.IRIDESCENCE_CRAFTING_TYPE.get())) {
            registry.addRecipe(new IridesenceCraftingEmiRecipe(recipe, registryManager));
        }

        //item interactions when clicked on chromatic blocks
        registry.addCategory(CHROMATIC);
        registry.addWorkstation(CHROMATIC, EmiStack.of(ModBlocks.CHROMATIC_WOOL.get()));
        registry.addWorkstation(CHROMATIC, EmiStack.of(ModBlocks.CHROMATIC_CARPET.get()));
        registry.addRecipe(ChromaticEmiRecipe.withInfo("color", ModItems.CHROMATIC_MATTER.get()));
        registry.addRecipe(ChromaticEmiRecipe.withInfo("hue", ModItems.IRIDESCENT_STAR.get()));
        registry.addRecipe(ChromaticEmiRecipe.of("saturation_plus", Items.AMETHYST_SHARD));
        registry.addRecipe(ChromaticEmiRecipe.of("saturation_minus", ModItems.FOOTPRINT.get()));
        registry.addRecipe(ChromaticEmiRecipe.of("brightness_plus", ModItems.WHITE_MATTER.get()));
        registry.addRecipe(ChromaticEmiRecipe.of("brightness_minus", ModItems.BLACK_MATTER.get()));

        //recoloring with chroma matter in the crafting grid
        EmiStack matter = EmiStack.of(ModItems.CHROMATIC_MATTER.get());
        EmiStack matter_catalyst = matter.copy().setRemainder(matter);
        for (RecipeEntry<ChromaticColoringRecipe> recipe : manager.listAllOfType(ModItemFunctions.CHROMATIC_COLORING_TYPE.get())) {
            List<EmiIngredient> input = new ArrayList<>();
            input.add(matter_catalyst);
            input.add(EmiIngredient.of(recipe.value().getInput()));
            registry.addRecipe(new EmiCraftingRecipe(input,
                    EmiStack.of(recipe.value().getResult(registryManager)),
                    recipe.id()));
        }

        //chroma carpet from wool
        List<EmiIngredient> input = new ArrayList<>();
        input.add(EmiStack.of(ModItems.CHROMATIC_WOOL.get()));
        input.add(EmiStack.of(ModItems.CHROMATIC_WOOL.get()));
        registry.addRecipe(new EmiCraftingRecipe(input,
                EmiStack.of(ModItems.CHROMATIC_CARPET.get().getDefaultStack().copyWithCount(3)),
                InfinityMethods.getId("/chromatic_carpet")));

        //brewing iridescence
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

        //bottling iridescence
        registry.addRecipe(EmiWorldInteractionRecipe.builder()
                .id(InfinityMethods.getId("/iridescence/bottling"))
                .leftInput(EmiStack.of(Items.GLASS_BOTTLE.getDefaultStack()))
                .rightInput(IRIDESCENCE_CATALYST, false)
                .output(EmiStack.of(ModItems.IRIDESCENT_POTION.get().getDefaultStack()))
                .build());

        //star of lang effects
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

    public static void addInfo(WidgetHolder widgets, int x, int y, Text info) {
        widgets.add(new TextWidgetWithTooltip(Text.literal("ℹ").formatted(Formatting.GRAY).asOrderedText(),
                x, y, 0xFFFFFF, false, info));
    }

    public static class TextWidgetWithTooltip extends TextWidget {
        Text tooltip;

        public TextWidgetWithTooltip(OrderedText text, int x, int y, int color, boolean shadow, Text tooltip) {
            super(text, x, y, color, shadow);
            this.tooltip = tooltip;
        }
        public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
            return List.of(TooltipComponent.of(tooltip.asOrderedText()));
        }
    }
}
