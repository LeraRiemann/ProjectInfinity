package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.util.BackportMethods;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public abstract class PortalDataHolder extends Item {
    public PortalDataHolder(Settings settings) {
        super(settings);
    }

    public int getColorFromId(Identifier id) {
        return Math.toIntExact(InfinityMethods.getNumericFromId(id));
    }

    public Identifier getDestination(ItemStack stack) {
        return BackportMethods.getDimensionIdentifier(stack);
    }

    public boolean isDestinationRandom(Identifier id) {
        return (id != null && id.toString().equals(InfinityMethods.ofRandomDim));
    }

    public Identifier getDestinationParsed(ItemStack stack, World world) {
        Identifier id = getDestination(stack);
        return (isDestinationRandom(id)) ? InfinityMethods.getRandomId(world.random) : id;
    }
/*
        public NbtCompound getPortalComponents(InfinityPortalBlockEntity ipbe) {
            Identifier dim = ipbe.getDimension();
            int keycolor = getColorFromId(dim);
            var c = new NbtCompound();
            c.putString(ModItemFunctions.DESTINATION, dim.toString());
            c.putInt(ModItemFunctions.COLOR, keycolor);
            return c;
        }
    
        public static Optional<NbtCompound> addPortalComponents(Item item, ItemStack oldStack, InfinityPortalBlockEntity ipbe) {
            if (item instanceof PortalDataHolder pdh)
                return Optional.of(pdh.addPortalComponents(oldStack, ipbe));
            return Optional.empty();
        }





    public ComponentMap.Builder getPortalComponents(InfinityPortalBlockEntity ipbe) {
        Identifier dim = ipbe.getDimension();
        Integer keycolor = getColorFromId(dim);
        return ComponentMap.builder()
                .add(ModItemFunctions.DESTINATION.get(), dim)
                .add(ModItemFunctions.COLOR.get(), keycolor);
    }

            oldStack.applyComponentsFrom(changes);
            return oldStack.getComponents();
        }

        public ItemStack withPortalData(InfinityPortalBlockEntity ipbe) {
            ItemStack stack = getDefaultStack();
            stack.setNbt(addPortalComponents(stack, ipbe));
            return stack;
        }
    */
    public abstract MutableText defaultDimensionTooltip();

    public abstract MutableText getDimensionTooltip(Identifier dimension);

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext type) {
        super.appendTooltip(stack, world, tooltip, type);
        Identifier dimension = BackportMethods.getDimensionIdentifier(stack);
        MutableText mutableText = (dimension != null) ? getDimensionTooltip(dimension) : defaultDimensionTooltip();
        tooltip.add(mutableText.formatted(Formatting.GRAY));
    }
}
