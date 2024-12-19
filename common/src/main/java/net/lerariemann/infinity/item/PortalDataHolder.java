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

    public static Identifier getDestination(ItemStack stack) {
        return stack.getComponents().get(ModItemFunctions.DESTINATION.get());
    }

    public ComponentMap.Builder getPortalComponents(InfinityPortalBlockEntity ipbe) {
        Identifier dim = ipbe.getDimension();
        Integer keycolor = getColorFromId(dim);
        return ComponentMap.builder()
                .add(ModItemFunctions.DESTINATION.get(), dim)
                .add(ModItemFunctions.COLOR.get(), keycolor);
    }

    public static Optional<ComponentMap> addPortalComponents(Item item, ItemStack oldStack, InfinityPortalBlockEntity ipbe) {
        if (item instanceof PortalDataHolder pdh)
            return Optional.of(pdh.addPortalComponents(oldStack, ipbe));
        return Optional.empty();
    }

    public ComponentMap addPortalComponents(ItemStack oldStack, InfinityPortalBlockEntity ipbe) {
        ComponentMap changes = getPortalComponents(ipbe).build();
        oldStack.applyComponentsFrom(changes);
        return oldStack.getComponents();
    }

    public ItemStack withPortalData(InfinityPortalBlockEntity ipbe) {
        ItemStack stack = getDefaultStack();
        stack.applyComponentsFrom(addPortalComponents(stack, ipbe));
        return stack;
    }

    public abstract MutableText defaultDimensionTooltip();

    public abstract MutableText getDimensionTooltip(Identifier dimension);

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        Identifier dimension = stack.getComponents().get(ModItemFunctions.DESTINATION.get());
        MutableText mutableText = (dimension != null) ? getDimensionTooltip(dimension) : defaultDimensionTooltip();
        tooltip.add(mutableText.formatted(Formatting.GRAY));
    }
}
