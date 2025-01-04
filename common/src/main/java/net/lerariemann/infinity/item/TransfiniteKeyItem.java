package net.lerariemann.infinity.item;

import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class TransfiniteKeyItem extends Item implements PortalDataHolder {
    public TransfiniteKeyItem(Settings settings) {
        super(settings);
    }

    @NotNull
    @Override
    public Identifier getDestination(ItemStack stack) {
        return Objects.requireNonNullElse(super.getDestination(stack),
                new Identifier(InfinityMethods.ofRandomDim)); // no destination component -> randomize
    }

    @Override
    public ItemStack getStack() {
        return getDefaultStack();
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        Identifier dimension = stack.getComponents().get(ModComponentTypes.DESTINATION.get());
        MutableText mutableText = (dimension != null)
                ? InfinityMethods.getDimensionNameAsText(dimension)
                : Text.translatable("tooltip.infinity.key.randomise");
        tooltip.add(mutableText.formatted(Formatting.GRAY));
    }
}
