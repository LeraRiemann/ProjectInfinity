package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.entity.ChromaticBlockEntity;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.util.BackportMethods;
import net.lerariemann.infinity.util.var.ColorLogic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
            int itemColor = BackportMethods.getOrDefaultInt(currStack, ModComponentTypes.COLOR, 0xFFFFFF);
            if (player.isSneaking()) {
                ItemStack newStack = currStack.copy();
                if (world.getBlockEntity(pos) instanceof ChromaticBlockEntity cbe) { //copy color from chroma blocks
                    int i = cbe.getTint();
                    if (i != itemColor) {
                        if (currStack.hasNbt()) {
                            newStack.setNbt(ChromaticBlockEntity.asMap(i));
                            newStack.getNbt().remove(ModComponentTypes.DYE_COLOR);
                        }
                        player.setStackInHand(context.getHand(), newStack);
                        playDing(player, 0.5f);
                        return ActionResult.SUCCESS;
                    }
                }
                else { //copy color from vanilla blocks
                    DyeColor color = ColorLogic.getColorByState(bs);
                    if (color == null) return super.useOnBlock(context);
                    if (!color.getName().equals(BackportMethods.getOrDefaultString(newStack, ModComponentTypes.DYE_COLOR, "null"))) {
                        BackportMethods.apply(newStack, ModComponentTypes.DYE_COLOR, color.getName());
                        BackportMethods.apply(newStack, ModComponentTypes.COLOR, color.getFireworkColor());
                        player.setStackInHand(context.getHand(), newStack);
                        playDing(player, 0.5f);
                        return ActionResult.SUCCESS;
                    }
                }
            }
            if (!player.isSneaking()) { //paste color to blocks
                if (currStack.hasNbt()) {
                    assert currStack.getNbt() != null;
                    boolean bl = currStack.getNbt().contains(ModComponentTypes.DYE_COLOR);
                    Block block = ColorLogic.recolor(bl ? currStack.getNbt().getString(ModComponentTypes.DYE_COLOR) : "infinity:chromatic", bs);
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
        }
        return super.useOnBlock(context);
    }
}
