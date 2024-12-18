package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.List;
import java.util.Optional;

public abstract class PortalDataHolder extends Item {
    public PortalDataHolder(Settings settings) {
        super(settings);
    }

    public int getColorFromId(Identifier id) {
        return ColorHelper.Argb.fullAlpha((int) InfinityMethods.getNumericFromId(id) & 0xFFFFFF);
    }

    public static Optional<ComponentMap> addKeyComponents(Item item, Identifier dim) {
        if (item instanceof PortalDataHolder pdh)
            return Optional.of(pdh.addKeyComponents(dim));
        return Optional.empty();
    }

    public Identifier getDestination(ItemStack stack) {
        return stack.getComponents().get(ModItemFunctions.KEY_DESTINATION.get());
    }

    public ComponentMap addKeyComponents(Identifier dim) {
        Integer keycolor = getColorFromId(dim);
        return (ComponentMap.builder()
                .add(ModItemFunctions.KEY_DESTINATION.get(), dim)
                .add(ModItemFunctions.COLOR.get(), keycolor)).build();
    }

    public ItemStack withPortalData(InfinityPortalBlockEntity ipbe) {
        ItemStack stack = getDefaultStack();
        stack.applyComponentsFrom(addKeyComponents(ipbe.getDimension()));
        return stack;
    }

    public abstract MutableText defaultTooltip();

    public abstract MutableText getTooltip(Identifier dimension);

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        Identifier dimension = stack.getComponents().get(ModItemFunctions.KEY_DESTINATION.get());
        MutableText mutableText = (dimension != null) ? getTooltip(dimension) : defaultTooltip();
        tooltip.add(mutableText.formatted(Formatting.GRAY));
    }
}
