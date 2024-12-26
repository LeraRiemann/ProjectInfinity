package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class ChromaticBlockItem extends BlockItem implements PortalDataHolder {
    public ChromaticBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ComponentMap.Builder getPortalComponents(InfinityPortalBlockEntity ipbe) {
        return ComponentMap.builder()
                .add(ModItemFunctions.COLOR.get(), ipbe.getPortalColor());
    }

    @Override
    public ItemStack getStack() {
        return getDefaultStack();
    }
}
