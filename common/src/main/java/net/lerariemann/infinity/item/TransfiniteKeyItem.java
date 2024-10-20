package net.lerariemann.infinity.item;

import net.lerariemann.infinity.var.ModComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class TransfiniteKeyItem extends Item {
    public TransfiniteKeyItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        Identifier dimension = stack.getComponents().get(ModComponentTypes.KEY_DESTINATION.get());
        if (dimension != null) {
            String s = dimension.toString();
            MutableText mutableText = Text.literal(s.equals("minecraft:random") ? "Dimension randomised" : s);
            tooltip.add(mutableText.formatted(Formatting.GRAY));
        }
    }
}
