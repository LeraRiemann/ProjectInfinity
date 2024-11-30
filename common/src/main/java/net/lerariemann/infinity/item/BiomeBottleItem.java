package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.custom.BiomeBottle;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.World;

import java.util.List;

public class BiomeBottleItem extends BlockItem {
    public BiomeBottleItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        String s = ModItemFunctions.getBiomeComponents(stack);
        if (s != null) {
            Identifier biome = Identifier.tryParse(s);
            tooltip.add(Text.translatable(Util.createTranslationKey("biome", biome)).formatted(Formatting.GRAY));
        }
        else {
            MutableText mutableText = Text.literal("Empty");
            tooltip.add(mutableText.formatted(Formatting.GRAY));
        }
        if (context.isAdvanced()) {
            tooltip.add(Text.literal("Charge: " + BiomeBottle.getCharge(stack)).formatted(Formatting.GRAY));
        }
    }
}
