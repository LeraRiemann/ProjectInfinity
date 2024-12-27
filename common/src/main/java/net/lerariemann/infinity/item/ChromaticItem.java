package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.entity.ChromaticBlockEntity;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.util.ColorLogic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
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
                .add(ModComponentTypes.COLOR.get(), ipbe.getPortalColor());
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
            BlockState bs = world.getBlockState(pos);
            ItemStack currStack = context.getStack();
            int itemColor = currStack.getOrDefault(ModComponentTypes.COLOR.get(), 0xFFFFFF);
            if (player.isSneaking()) {
                ItemStack newStack = currStack.copy();
                if (world.getBlockEntity(pos) instanceof ChromaticBlockEntity cbe) { //copy color from chroma blocks
                    int i = cbe.getTint();
                    if (i != itemColor) {
                        newStack.applyComponentsFrom(ChromaticBlockEntity.asMap(i));
                        newStack.remove(ModComponentTypes.DYE_COLOR.get());
                        player.setStackInHand(context.getHand(), newStack);
                        playDing(player, 0.5f);
                        return ActionResult.SUCCESS;
                    }
                }
                else { //copy color from vanilla blocks
                    DyeColor color = ColorLogic.getColorByState(bs);
                    if (color == null) return super.useOnBlock(context);
                    if (!color.getName().equals(newStack.getOrDefault(ModComponentTypes.DYE_COLOR.get(), "null"))) {
                        newStack.applyComponentsFrom(ComponentMap.builder()
                                        .add(ModComponentTypes.DYE_COLOR.get(), color.getName())
                                        .add(ModComponentTypes.COLOR.get(), color.getEntityColor())
                                .build());
                        player.setStackInHand(context.getHand(), newStack);
                        playDing(player, 0.5f);
                        return ActionResult.SUCCESS;
                    }
                }
            }
            if (!player.isSneaking()) { //paste color to blocks
                boolean bl = currStack.contains(ModComponentTypes.DYE_COLOR.get());
                Block block = ColorLogic.recolor(bl ? currStack.get(ModComponentTypes.DYE_COLOR.get()) : "infinity:chromatic", bs);
                if (block == null) return super.useOnBlock(context);
                world.setBlockState(pos, block.getDefaultState());
                AtomicBoolean cancel = new AtomicBoolean(false);
                if (!bl && world.getBlockEntity(pos) instanceof ChromaticBlockEntity cbe) {
                    cbe.setColor(itemColor, cancel);
                }
                if (!cancel.get()) {
                    playDing(player, 1f);
                    return ActionResult.SUCCESS;
                }
            }
        }
        return super.useOnBlock(context);
    }
}
