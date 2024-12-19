package net.lerariemann.infinity.item;

import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TransfiniteKeyItem extends PortalDataHolder {
    public TransfiniteKeyItem(Settings settings) {
        super(settings);
    }

    @Override
    public MutableText defaultDimensionTooltip() {
        return Text.translatable("tooltip.infinity.key.randomise");
    }

    @NotNull
    @Override
    public Identifier getDestination(ItemStack stack) {
        return Objects.requireNonNullElse(super.getDestination(stack),
                Identifier.of(InfinityMethods.ofRandomDim)); // no destination component -> randomize
    }

    @Override
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
}
