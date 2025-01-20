package net.lerariemann.infinity.item;

import net.lerariemann.infinity.util.BackportMethods;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class TransfiniteKeyItem extends Item implements PortalDataHolder {
    public TransfiniteKeyItem(Settings settings) {
        super(settings);
    }

    @Override
    public ComponentChanges.Builder getPortalComponents(InfinityPortalBlockEntity ipbe) {
        Identifier id = ipbe.getDimension();
        int color = (ipbe.getWorld() instanceof ServerWorld w) ?
                PortalColorApplier.of(id, w.getServer()).apply(BlockPos.ORIGIN) :
                (int)InfinityMethods.getNumericFromId(id);
        return ComponentChanges.builder()
                .add(ModComponentTypes.DESTINATION.get(), id)
                .add(ModComponentTypes.COLOR.get(), color & 0xFFFFFF);
    }

    @NotNull
    @Override
    public Identifier getDestination(ItemStack stack) {
        return Objects.requireNonNullElse(BackportMethods.getDimensionIdentifier(stack),
                new Identifier(InfinityMethods.ofRandomDim)); // no destination component -> randomize
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext type) {
        super.appendTooltip(stack, world, tooltip, type);
        Identifier dimension = BackportMethods.getDimensionIdentifier(stack);
        MutableText mutableText = (dimension != null)
                ? InfinityMethods.getDimensionNameAsText(dimension)
                : Text.translatable("tooltip.infinity.key.randomise");
        tooltip.add(mutableText.formatted(Formatting.GRAY));
    }
}
