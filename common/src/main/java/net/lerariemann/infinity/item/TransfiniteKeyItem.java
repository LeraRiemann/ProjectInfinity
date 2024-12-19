package net.lerariemann.infinity.item;

import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TransfiniteKeyItem extends PortalDataHolder {
    public TransfiniteKeyItem(Settings settings) {
        super(settings);
    }

    @Override
    public MutableText defaultDimensionTooltip() {
        return Text.translatable("tooltip.infinity.key.randomise");
    }

    @Override
    public MutableText getDimensionTooltip(Identifier dimension) {
        String s = dimension.toString();
        // Keys to randomly generated dimensions.
        if (s.contains("infinity:generated_"))
            return Text.translatable("tooltip.infinity.key.generated")
                    .append(s.replace("infinity:generated_", ""));
        // Keys without a dimension attached.
        if (s.equals("minecraft:random"))
            return Text.translatable("tooltip.infinity.key.randomise");
        // Easter Egg dimensions.
        return Text.translatableWithFallback(
                Util.createTranslationKey("dimension", dimension),
                InfinityMethods.fallback(dimension.getPath()));
    }
}
