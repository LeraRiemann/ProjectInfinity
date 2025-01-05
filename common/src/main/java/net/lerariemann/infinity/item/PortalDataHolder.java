package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.util.BackportMethods;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface PortalDataHolder {
    @Nullable
    default Identifier getDestination(ItemStack stack) {
        return BackportMethods.getDimensionIdentifier(stack);
    }

    default boolean isDestinationRandom(Identifier id) {
        return (id != null && id.toString().equals(InfinityMethods.ofRandomDim));
    }

    default Identifier getDestinationParsed(ItemStack stack, World world) {
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





    static Optional<ComponentMap> addPortalComponents(Item item, ItemStack oldStack, InfinityPortalBlockEntity ipbe) {
        if (item instanceof PortalDataHolder pdh)
            return Optional.of(pdh.addPortalComponents(oldStack, ipbe));
        return Optional.empty();
    }

    default ComponentMap addPortalComponents(ItemStack oldStack, InfinityPortalBlockEntity ipbe) {
        ComponentMap changes = getPortalComponents(ipbe).build();
        oldStack.applyComponentsFrom(changes);
        return oldStack.getComponents();
    }

    default ComponentMap.Builder getPortalComponents(InfinityPortalBlockEntity ipbe) {
        return ComponentMap.builder()
                .add(ModComponentTypes.DESTINATION.get(), ipbe.getDimension())
                .add(ModComponentTypes.COLOR.get(), ipbe.getPortalColor());
    }

    default ItemStack withPortalData(InfinityPortalBlockEntity ipbe) {
        ItemStack stack = getStack();
        stack.applyComponentsFrom(addPortalComponents(stack, ipbe));
        return stack;
    }

    ItemStack getStack();
     */
}
