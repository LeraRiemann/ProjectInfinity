package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.custom.AltarBlock;
import net.lerariemann.infinity.block.entity.ChromaticBlockEntity;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.util.var.ColorLogic;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentChanges;
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

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChromaticItem extends Item implements PortalDataHolder {
    public ChromaticItem(Settings settings) {
        super(settings);
    }
    @Override
    public ComponentChanges.Builder getPortalComponents(InfinityPortalBlockEntity ipbe) {
        return ComponentChanges.builder()
                .add(ModComponentTypes.COLOR.get(), ipbe.getPortalColor())
                .remove(ModComponentTypes.DYE_COLOR.get());
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
            BlockState oldState = world.getBlockState(pos);
            ItemStack currStack = context.getStack();
            int itemColor = currStack.getOrDefault(ModComponentTypes.COLOR.get(), 0xFFFFFF);
            if (player.isSneaking()) { //copy color
                ItemStack newStack = currStack.copy();
                int i = -1;
                if (world.getBlockEntity(pos) instanceof ChromaticBlockEntity cbe) { //copy color from chroma blocks
                    i = cbe.getTint();
                }
                else if (oldState.isOf(ModBlocks.ALTAR.get())) {
                    int hue = (oldState.get(AltarBlock.COLOR) - 1) * (360 / (AltarBlock.numColors - 1));
                    i = hue < 0 ? 0xCCCCCC : Color.HSBtoRGB(hue/360f, 1.0f, 1.0f) & 0xFFFFFF;
                }
                if (i > 0 && i != itemColor) {
                    newStack.applyComponentsFrom(ChromaticBlockEntity.asMap(i));
                    newStack.remove(ModComponentTypes.DYE_COLOR.get());
                    player.setStackInHand(context.getHand(), newStack);
                    playDing(player, 0.5f);
                    return ActionResult.SUCCESS;
                }
                else { //copy color from vanilla blocks
                    DyeColor color = ColorLogic.getColorByState(oldState);
                    if (color == null) return super.useOnBlock(context);
                    if (!color.getName().equals(newStack.getOrDefault(ModComponentTypes.DYE_COLOR.get(), "null"))) {
                        newStack.applyComponentsFrom(ComponentMap.builder()
                                        .add(ModComponentTypes.DYE_COLOR.get(), color.getName())
                                        .add(ModComponentTypes.COLOR.get(), ColorLogic.getChromaticColor(color))
                                .build());
                        player.setStackInHand(context.getHand(), newStack);
                        playDing(player, 0.5f);
                        return ActionResult.SUCCESS;
                    }
                }
            }
            if (!player.isSneaking()) { //paste color to blocks
                boolean bl = currStack.contains(ModComponentTypes.DYE_COLOR.get());
                BlockState state = ColorLogic.recolor(bl ? currStack.get(ModComponentTypes.DYE_COLOR.get()) : "infinity:chromatic", oldState);
                if (state == null) return super.useOnBlock(context);
                world.setBlockState(pos, state);
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
