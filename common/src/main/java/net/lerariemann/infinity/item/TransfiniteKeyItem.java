package net.lerariemann.infinity.item;

import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.List;

public class TransfiniteKeyItem extends Item {
    public TransfiniteKeyItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        Identifier dimension = stack.getComponents().get(ModItemFunctions.KEY_DESTINATION.get());
        MutableText mutableText;
        if (dimension != null) {
            String s = dimension.toString();
            // Keys to randomly generated dimensions.
            if (s.contains("infinity:generated_"))
                mutableText = Text.translatable("dimension.infinity.generated")
                        .append(s.replace("infinity:generated_", ""));
            // Keys without a dimension attached.
            else if (s.equals("minecraft:random"))
                mutableText = Text.translatable("dimension.infinity.randomise");
            // Easter Egg dimensions.
            else
                mutableText = Text.translatableWithFallback(
                        Util.createTranslationKey("dimension", dimension),
                        InfinityMethods.fallback(dimension.getPath()));
        }
        else {
            mutableText = Text.translatable("dimension.infinity.randomise");
        }
        tooltip.add(mutableText.formatted(Formatting.GRAY));
    }
}
