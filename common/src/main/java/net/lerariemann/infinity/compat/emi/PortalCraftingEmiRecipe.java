package net.lerariemann.infinity.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.lerariemann.infinity.item.PortalDataHolder;
import net.lerariemann.infinity.item.function.CollisionCraftingRecipe;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.util.var.ColorLogic;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class PortalCraftingEmiRecipe extends BasicEmiRecipe {
    String lore;

    public PortalCraftingEmiRecipe(RecipeEntry<CollisionCraftingRecipe> recipeEntry, DynamicRegistryManager registryManager) {
        super(EmiCompat.PORTAL_CRAFTING, recipeEntry.id(), 118, 18);
        CollisionCraftingRecipe recipe = recipeEntry.value();
        this.inputs.add(EmiIngredient.of(recipe.getInput()));
        ItemStack out = recipe.getResult(registryManager);
        ComponentChanges.Builder b = ComponentChanges.builder();
        if (out.getItem() instanceof PortalDataHolder) {
            b = b.add(ModComponentTypes.COLOR.get(), ColorLogic.defaultPortal);
        }
        if (out.getItem() instanceof PortalDataHolder.Destinable) {
            b = b.add(ModComponentTypes.DESTINATION.get(), Identifier.of("infinity:generated_0"));
        }
        out.applyChanges(b.build());
        this.outputs.add(EmiStack.of(out));
        this.lore = recipeEntry.value().getLore();
        if (!Objects.equals(lore, "empty")) {
            this.width = 130;
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.getFirst(), 0, 0);
        widgets.addTexture(EmiTexture.PLUS, 26, 2);
        widgets.add(new SlotWidget(EmiCompat.PORTAL_WORKSTATION, 48, 0));
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 70, 0);
        widgets.addSlot(outputs.getFirst(), 100, 0).recipeContext(this);

        // lore handling
        if (!Objects.equals(lore, "empty")) {
            EmiCompat.addInfo(widgets, 120, 5, Text.translatable(lore));
        }
    }
}
