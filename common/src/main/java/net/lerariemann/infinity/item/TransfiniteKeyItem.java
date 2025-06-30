package net.lerariemann.infinity.item;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.options.PortalColorApplier;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TransfiniteKeyItem extends Item implements PortalDataHolder.Destinable {
    public TransfiniteKeyItem(Settings settings) {
        super(settings);
    }

    @Override
    public ComponentChanges getPortalComponents(InfinityPortalBlockEntity ipbe) {
        Identifier id = ipbe.getDimension();
        int color = (ipbe.getWorld() instanceof ServerWorld w) ?
                PortalColorApplier.of(id, w.getServer()).apply(BlockPos.ORIGIN) :
                (int)InfinityMethods.getNumericFromId(id);
        return ComponentChanges.builder()
                .add(ModComponentTypes.DESTINATION.get(), id)
                .add(ModComponentTypes.COLOR.get(), color & 0xFFFFFF)
                .add(DataComponentTypes.CUSTOM_MODEL_DATA, getDataForClientItem(id, color & 0xFFFFFF))
                .build();
    }

    @Override
    public Optional<ComponentChanges> getIridComponents(ItemEntity entity) {
        return Optional.of(ComponentChanges.builder()
                .remove(ModComponentTypes.DESTINATION.get())
                .remove(ModComponentTypes.COLOR.get())
                .build());
    }

    @NotNull
    @Override
    public Identifier getDestination(ItemStack stack) {
        return Objects.requireNonNullElse(stack.getComponents().get(ModComponentTypes.DESTINATION.get()),
                Identifier.of(InfinityMethods.ofRandomDim)); // no destination component -> randomize
    }

    @Override
    public ItemStack getStack() {
        return getDefaultStack();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        Identifier dimension = stack.getComponents().get(ModComponentTypes.DESTINATION.get());
        MutableText mutableText = (dimension != null)
                ? InfinityMethods.getDimensionNameAsText(dimension)
                : Text.translatable("tooltip.infinity.key.randomise");
        tooltip.add(mutableText.formatted(Formatting.GRAY));
    }

    public CustomModelDataComponent getDataForClientItem(ItemStack stack) {
        return getDataForClientItem(stack.getComponents().getOrDefault(ModComponentTypes.DESTINATION.get(), InfinityMethods.getId("random")), stack.getComponents().getOrDefault(ModComponentTypes.COLOR.get(), 0));
    }

    public CustomModelDataComponent getDataForClientItem(Identifier dimension, int color) {
        String data;
        if (dimension.getPath().contains("generated")) {
            data = "generated";
        } else if (dimension.equals(Identifier.ofVanilla("end"))) {
            data = "end";
        } else if (dimension.equals(InfinityMethods.getId("pride"))) {
            data = "pride";
        } else if (InfinityMod.provider.easterizer.isEaster(dimension.getPath()) || !dimension.getNamespace().equals("infinity")) {
            data = "golden";
        } else {
            data = "random";
        }
        return new CustomModelDataComponent(List.of(), List.of(), List.of(data), List.of(color));
    }
}
