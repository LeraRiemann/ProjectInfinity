package net.lerariemann.infinity.compat.eiv.portal_crafting;

import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class PortalCraftingViewType implements IEivRecipeViewType {

    protected static final PortalCraftingViewType INSTANCE = new PortalCraftingViewType();

    @Override
    public Text getDisplayName() {
        return Text.translatable("emi.category.infinity.collision_portal");
    }

    @Override
    public int getDisplayWidth() {
        return 100;
    }

    @Override
    public int getDisplayHeight() {
        return 25;
    }

    @Override
    public Identifier getGuiTexture() {
        return InfinityMethods.getId("textures/gui/collision.png");
    }

    @Override
    public int getSlotCount() {
        return 3;
    }

    @Override
    public void placeSlots(RecipeViewMenu.SlotDefinition slotDefinition) {
        slotDefinition.addItemSlot(0, 5, 5);
        slotDefinition.addItemSlot(1, 42, 5);
        slotDefinition.addItemSlot(2, 80, 5);
    }

    @Override
    public Identifier getId() {
        return InfinityMethods.getId("portal_crafting");
    }

    @Override
    public ItemStack getIcon() {
        return ModItems.PORTAL_ITEM.get().getDefaultStack();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(ModItems.PORTAL_ITEM.get().getDefaultStack());
    }

}
