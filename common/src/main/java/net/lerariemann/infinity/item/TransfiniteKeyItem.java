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
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class TransfiniteKeyItem extends Item implements PortalDataHolder {
    public TransfiniteKeyItem(Settings settings) {
        super(settings);
    }

    public MutableText defaultDimensionTooltip() {
        return Text.translatable("tooltip.infinity.key.randomise");
    }

    @NotNull
    @Override
    public Identifier getDestination(ItemStack stack) {
        return Objects.requireNonNullElse(stack.getComponents().get(ModComponentTypes.DESTINATION.get()),
                Identifier.of(InfinityMethods.ofRandomDim)); // no destination component -> randomize
    }

    @Override
    public ItemStack getStack() {
        return getDefaultStack();
    }

    public MutableText getDimensionTooltip(Identifier dimension) {
        String s = dimension.toString();
        // Keys to randomly generated dimensions.
        if (s.contains("infinity:generated_"))
            return Text.translatable("tooltip.infinity.key.generated")
                    .append(s.replace("infinity:generated_", ""));
        // Keys without a dimension attached.
        if (s.equals(InfinityMethods.ofRandomDim))
            return Text.translatable("tooltip.infinity.key.randomise");
        // Easter Egg dimensions.
        return Text.translatableWithFallback(
                Util.createTranslationKey("dimension", dimension),
                InfinityMethods.fallback(dimension.getPath()));
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        Identifier dimension = stack.getComponents().get(ModComponentTypes.DESTINATION.get());
        MutableText mutableText = (dimension != null) ? getDimensionTooltip(dimension) : defaultDimensionTooltip();
        tooltip.add(mutableText.formatted(Formatting.GRAY));
    }
}
