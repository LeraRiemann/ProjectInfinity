package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.custom.AltarBlock;
import net.lerariemann.infinity.block.entity.ChromaticBlockEntity;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.var.ModTags;
import net.lerariemann.infinity.util.BackportMethods;
import net.lerariemann.infinity.util.var.ColorLogic;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
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
        if (player == null) return super.useOnBlock(context);
        return useOnBlock(player, context.getHand(), context.getWorld(), context.getBlockPos(), context.getStack())
                ? ActionResult.SUCCESS : super.useOnBlock(context);
    }

    public boolean useOnBlock(PlayerEntity player, Hand hand, World world, BlockPos pos, ItemStack currStack) {
        BlockState oldState = world.getBlockState(pos);
        int currColor = BackportMethods.getOrDefaultInt(currStack, ModComponentTypes.KEY_COLOR, 0xFFFFFF);
        if (player.isSneaking()) { //copy color
            ItemStack newStack = currStack.copy();
            int i = -1;
            if (world.getBlockEntity(pos) instanceof ChromaticBlockEntity cbe) i = cbe.getTint();
            else if (oldState.isOf(ModBlocks.ALTAR.get())) {
                int altarState = oldState.get(AltarBlock.COLOR);
                if (altarState == 0) i = ColorLogic.getChromaticColor(DyeColor.LIGHT_GRAY);
                else {
                    int hue = (altarState - 1) * (360 / (AltarBlock.numColors - 1));
                    if (!ColorLogic.matchesPureHue(currColor, hue))
                        BackportMethods.apply(newStack, ModComponentTypes.HUE, hue);
                    else return false;
                }
            }
            else { //copy color from vanilla blocks
                DyeColor dyeColor = ColorLogic.getColorByState(oldState);
                if (dyeColor != null &&
                        !(dyeColor.getId() == (BackportMethods.getOrDefaultInt(newStack, ModComponentTypes.DYE_COLOR, -1)))) {
                    BackportMethods.apply(newStack, ModComponentTypes.DYE_COLOR, dyeColor.getId());
                }
                else return false;
            }
            if (i > 0) {
                if (i != currColor) BackportMethods.apply(newStack, ModComponentTypes.DYE_COLOR, i);
                else return false;
            }
            player.setStackInHand(hand, newStack);
            playDing(player, 0.5f);
            return true;
        }
        else { //paste color to blocks
            if (oldState.isIn(ModTags.IRIDESCENT_BLOCKS)) return false;
            boolean bl = BackportMethods.contains(currStack, ModComponentTypes.DYE_COLOR);
            BlockState state;
            if (bl) state = ColorLogic.recolor(BackportMethods.getOrDefaultString(currStack, ModComponentTypes.DYE_COLOR, "infinity:chromatic"), oldState);
            else state = ColorLogic.recolor("infinity:chromatic", oldState);
            if (state == null) return false;
            world.setBlockState(pos, state);
            AtomicBoolean cancel = new AtomicBoolean(false);
            if (!bl && world.getBlockEntity(pos) instanceof ChromaticBlockEntity cbe) {
                int hue = BackportMethods.getOrDefaultInt(currStack, ModComponentTypes.HUE, -1);
                if (hue > 0) cbe.setColor(hue, 255, 255, cancel);
                else cbe.setColor(currColor, cancel);
            }
            if (!cancel.get()) {
                playDing(player, 1f);
                return true;
            }
        }
        return false;
    }

    @Override
    public Text getName() {
        return Text.translatable(this.getTranslationKey()).setStyle(Style.EMPTY.withColor(ColorLogic.defaultChromatic));
    }
}
