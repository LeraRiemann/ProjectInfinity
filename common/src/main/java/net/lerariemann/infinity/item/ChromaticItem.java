package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.entity.ChromaticBlockEntity;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;

public class ChromaticItem extends Item implements PortalDataHolder {
    public ChromaticItem(Settings settings) {
        super(settings);
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

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null && player.isSneaking()
                && context.getWorld().getBlockEntity(context.getBlockPos()) instanceof ChromaticBlockEntity cbe) {
            ItemStack newStack = context.getStack().copy();
            int i = cbe.getTint();
            int j = newStack.getOrDefault(ModItemFunctions.COLOR.get(), 0xFFFFFF);
            if (i != j) {
                newStack.applyComponentsFrom(cbe.asMap(i));
                player.setStackInHand(context.getHand(), newStack);
                player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.5f);
                return ActionResult.SUCCESS;
            }
        }
        return super.useOnBlock(context);
    }
}
