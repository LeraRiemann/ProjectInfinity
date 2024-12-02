package net.lerariemann.infinity.item;

import net.lerariemann.infinity.item.function.ModItemFunctions;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.World;

import java.util.List;

public class TransfiniteKeyItem extends Item {
    public TransfiniteKeyItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        String dimension = ModItemFunctions.getDimensionComponents(stack);
        MutableText mutableText;
        if (dimension != null) {
            if (dimension.contains("infinity:generated_")) mutableText
                    = Text.translatable("dimension.infinity.generated")
                    .append(dimension.replace("infinity:generated_", ""));
            else if (dimension.equals("minecraft:random")) mutableText = Text.translatable("dimension.infinity.randomise");
            else {
                String[] forFallback = dimension.split(":")[0].replace("_", " ").split("\\s");
                StringBuilder fallback = new StringBuilder();
                for (String str: forFallback) fallback.append(Character.toUpperCase(str.charAt(0))).append(str.substring(1)).append(" ");
                mutableText = MutableText.of(new TranslatableTextContent(Util.createTranslationKey("dimension", Identifier.tryParse(dimension)),
                        fallback.toString().trim(), TranslatableTextContent.EMPTY_ARGUMENTS));
            }
        }
        else {
            mutableText = Text.translatable("dimension.infinity.randomise");
        }
        tooltip.add(mutableText.formatted(Formatting.GRAY));
    }
}
