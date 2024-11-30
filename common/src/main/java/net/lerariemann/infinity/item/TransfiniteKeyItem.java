package net.lerariemann.infinity.item;

import net.lerariemann.infinity.item.function.ModItemFunctions;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TransfiniteKeyItem extends Item {
    public TransfiniteKeyItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        String dimension = ModItemFunctions.getDimensionComponents(stack);
        if (dimension != null) {
            MutableText mutableText = Text.literal(dimension.equals("minecraft:random") ? "Dimension randomised" : dimension);
            tooltip.add(mutableText.formatted(Formatting.GRAY));
        }
        else {
            MutableText mutableText = Text.literal("Dimension randomised");
            tooltip.add(mutableText.formatted(Formatting.GRAY));
        }

    }
}
