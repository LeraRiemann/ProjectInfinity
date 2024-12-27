package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.entity.ChromaticBlockEntity;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.concurrent.atomic.AtomicBoolean;

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

    public static void playDing(PlayerEntity player, float pitch) {
        player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, pitch);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null) {
            World world = context.getWorld();
            BlockPos pos = context.getBlockPos();
            int itemColor = context.getStack().getOrDefault(ModItemFunctions.COLOR.get(), 0xFFFFFF);
            if (player.isSneaking()
                    && world.getBlockEntity(pos) instanceof ChromaticBlockEntity cbe) {
                ItemStack newStack = context.getStack().copy();
                int i = cbe.getTint();
                if (i != itemColor) {
                    newStack.applyComponentsFrom(cbe.asMap(i));
                    player.setStackInHand(context.getHand(), newStack);
                    playDing(player, 0.5f);
                    return ActionResult.SUCCESS;
                }
            }
            if (!player.isSneaking()) {
                BlockState bs = world.getBlockState(pos);
                Block block;
                if (bs.isIn(BlockTags.WOOL)) block = ModBlocks.CHROMATIC_WOOL.get();
                else if (bs.isIn(BlockTags.WOOL_CARPETS)) block = ModBlocks.CHROMATIC_CARPET.get();
                else return super.useOnBlock(context);
                world.setBlockState(pos, block.getDefaultState());
                if (world.getBlockEntity(pos) instanceof ChromaticBlockEntity cbe) {
                    AtomicBoolean cancel = new AtomicBoolean(false);
                    cbe.setColor(itemColor, cancel);
                    if (!cancel.get()) {
                        playDing(player, 1f);
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }
        return super.useOnBlock(context);
    }
}
