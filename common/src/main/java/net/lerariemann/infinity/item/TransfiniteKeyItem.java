package net.lerariemann.infinity.item;

import net.lerariemann.infinity.item.function.ModItemFunctions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TransfiniteKeyItem extends Item {
    public TransfiniteKeyItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        String dimension = ModItemFunctions.getDimensionComponents(stack);
        MutableText mutableText;
        if (dimension != null) {
            // Keys to randomly generated dimensions.
            if (dimension.contains("infinity:generated_"))
                mutableText = Text.translatable("dimension.infinity.generated")
                        .append(dimension.replace("infinity:generated_", ""));
            // Keys without a dimension attached.
            else if (dimension.equals("minecraft:random"))
                mutableText = Text.translatable("dimension.infinity.randomise");
            // Easter Egg dimensions.
            else {
                Identifier id = new Identifier(dimension);
                mutableText = Text.translatableWithFallback(
                        Util.createTranslationKey("dimension", id),
                        InfinityMethods.fallback(id.getPath()));
            }
        }
        else {
            mutableText = Text.translatable("dimension.infinity.randomise");
        }
        tooltip.add(mutableText.formatted(Formatting.GRAY));
    }
}
