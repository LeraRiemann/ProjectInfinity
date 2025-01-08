package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface PortalDataHolder {
    @Nullable
    default Identifier getDestination(ItemStack stack) {
        return stack.getComponents().get(ModComponentTypes.DESTINATION.get());
    }

    default boolean isDestinationRandom(Identifier id) {
        return (id != null && id.toString().equals(InfinityMethods.ofRandomDim));
    }

    default Identifier getDestinationParsed(ItemStack stack, World world) {
        Identifier id = getDestination(stack);
        return (isDestinationRandom(id)) ? InfinityMethods.getRandomId(world.random) : id;
    }

    static Optional<ComponentChanges> addPortalComponents(Item item, ItemStack oldStack, InfinityPortalBlockEntity ipbe) {
        if (item instanceof PortalDataHolder pdh)
            return Optional.of(pdh.addPortalComponents(oldStack, ipbe));
        return Optional.empty();
    }

    default ComponentChanges addPortalComponents(ItemStack oldStack, InfinityPortalBlockEntity ipbe) {
        ComponentChanges changes = getPortalComponents(ipbe).build();
        oldStack.applyChanges(changes);
        return changes;
    }

    default ComponentChanges.Builder getPortalComponents(InfinityPortalBlockEntity ipbe) {
        return ComponentChanges.builder()
                .add(ModComponentTypes.DESTINATION.get(), ipbe.getDimension())
                .add(ModComponentTypes.COLOR.get(), ipbe.getPortalColor());
    }

    default ItemStack withPortalData(InfinityPortalBlockEntity ipbe) {
        ItemStack stack = getStack();
        stack.applyChanges(addPortalComponents(stack, ipbe));
        return stack;
    }

    ItemStack getStack();
}
