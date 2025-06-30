package net.lerariemann.infinity.compat.eiv.portal_crafting;

import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.api.recipe.IEivViewRecipe;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.lerariemann.infinity.item.PortalDataHolder;
import net.lerariemann.infinity.item.function.CollisionCraftingRecipe;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.var.ColorLogic;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

public class PortalCraftingViewRecipe implements IEivViewRecipe {

    private final SlotContent input, output;
    private String lore;

    public PortalCraftingViewRecipe(ItemStack input, ItemStack output) {
        this.input = SlotContent.of(input);
        this.output = SlotContent.of(output);
    }

    public PortalCraftingViewRecipe(PortalCraftingServerRecipe modRecipe) {
        var output = modRecipe.getResult();
        ComponentChanges.Builder b = ComponentChanges.builder();
        if (output.getItem() instanceof PortalDataHolder) {
            b = b.add(ModComponentTypes.COLOR.get(), ColorLogic.defaultPortal);
            b = b.add(DataComponentTypes.CUSTOM_MODEL_DATA, InfinityMethods.getColoredModel(ColorLogic.defaultPortal));
        }
        if (output.getItem() instanceof PortalDataHolder.Destinable) {
            b = b.add(ModComponentTypes.DESTINATION.get(), Identifier.of("infinity:generated_0"));
        }
        output.applyChanges(b.build());
        this.lore = modRecipe.getLore();
        this.input = SlotContent.of(modRecipe.getIngredient());
        this.output = SlotContent.of(output);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return PortalCraftingViewType.INSTANCE;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext slotFillContext) {
        slotFillContext.bindOptionalSlot(0, this.input, RecipeViewMenu.OptionalSlotRenderer.DEFAULT);
        slotFillContext.bindOptionalSlot(1, SlotContent.of(ModItems.PORTAL_ITEM.get()), RecipeViewMenu.OptionalSlotRenderer.DEFAULT);
        slotFillContext.bindOptionalSlot(2, this.output, RecipeViewMenu.OptionalSlotRenderer.DEFAULT);
        if (this.lore != null && I18n.hasTranslation(this.lore)) {
            slotFillContext.addAdditionalStackModifier(2, (stack, tooltip) -> {
                tooltip.add(Text.translatable(this.lore));
            });
        }
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(this.input);
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(this.output);
    }
}
