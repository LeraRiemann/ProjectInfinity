package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
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

    static boolean isDestinationRandom(Identifier id) {
        return (id != null && id.toString().equals(InfinityMethods.ofRandomDim));
    }

    default Identifier getDestinationParsed(ItemStack stack, World world) {
        Identifier id = getDestination(stack);
        return (isDestinationRandom(id)) ? InfinityMethods.getRandomId(world.random) : id;
    }

    static Optional<ComponentChanges> addPortalComponents(Item item, InfinityPortalBlockEntity ipbe) {
        if (item instanceof PortalDataHolder pdh)
            return Optional.of(pdh.getPortalComponents(ipbe));
        return Optional.empty();
    }
    default ComponentChanges getPortalComponents(InfinityPortalBlockEntity ipbe) {
        return ComponentChanges.builder()
                .add(ModComponentTypes.COLOR.get(), ipbe.getPortalColor())
                .add(DataComponentTypes.CUSTOM_MODEL_DATA, InfinityMethods.getColoredModel(ipbe.getPortalColor()))
                .build();
    }

    static Optional<ComponentChanges> getIridComponents(Item item, ItemEntity entity) {
        if (item instanceof PortalDataHolder pdh)
            return pdh.getIridComponents(entity);
        return Optional.empty();
    }
    default Optional<ComponentChanges> getIridComponents(ItemEntity entity) {
        return Optional.empty();
    }

    default ItemStack withPortalData(InfinityPortalBlockEntity ipbe) {
        ItemStack stack = getStack();
        stack.applyChanges(getPortalComponents(ipbe));
        return stack;
    }

    ItemStack getStack();

    interface Destinable extends PortalDataHolder {
        @Override
        default ComponentChanges getPortalComponents(InfinityPortalBlockEntity ipbe) {
            return ComponentChanges.builder()
                    .add(ModComponentTypes.DESTINATION.get(), ipbe.getDimension())
                    .add(ModComponentTypes.COLOR.get(), ipbe.getPortalColor())
                    .add(DataComponentTypes.CUSTOM_MODEL_DATA, InfinityMethods.getColoredModel(ipbe.getPortalColor()))
                    .build();
        }
    }
}
