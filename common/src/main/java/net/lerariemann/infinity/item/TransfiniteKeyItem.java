package net.lerariemann.infinity.item;

import net.lerariemann.infinity.item.function.ModItemFunctions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
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
        if (dimension != null) {
            MutableText mutableText;
            String s = dimension.toString();
            if (s.contains("infinity:generated_")) mutableText
                    = Text.translatable("dimension.infinity.generated")
                    .append(s.replace("infinity:generated_", ""));
            else if (s.equals("minecraft:random")) mutableText = Text.translatable("dimension.infinity.randomise");
            else {
                String fallback = dimension.getPath().replace("_", " ");
                fallback = Character.toUpperCase(fallback.charAt(0)) + fallback.substring(1);
                mutableText = MutableText.of(new TranslatableTextContent(Util.createTranslationKey("dimension", dimension),
                        fallback, TranslatableTextContent.EMPTY_ARGUMENTS));
            }
            tooltip.add(mutableText.formatted(Formatting.GRAY));
        }
        else {
            MutableText mutableText = Text.translatable("dimension.infinity.randomise");
            tooltip.add(mutableText.formatted(Formatting.GRAY));
        }
    }
}
