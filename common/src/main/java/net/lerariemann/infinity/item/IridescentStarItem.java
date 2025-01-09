package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.custom.AltarBlock;
import net.lerariemann.infinity.block.custom.IridescentBlock;
import net.lerariemann.infinity.block.entity.ChromaticBlockEntity;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class IridescentStarItem extends Item {
    public IridescentStarItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) return super.useOnBlock(context);
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState oldState = world.getBlockState(pos);
        boolean reverse = player.isSneaking();
        if (world.getBlockEntity(pos) instanceof ChromaticBlockEntity cbe) {
            cbe.onIridStarUse(reverse);
        }
        else if (oldState.getBlock() instanceof AltarBlock) {
            AltarBlock.setColor(world, pos, oldState,
                     InfinityMethods.properMod(oldState.get(AltarBlock.COLOR) + (reverse ? -1 : 1),
                             AltarBlock.numColors));
        }
        else if (oldState.getBlock() instanceof IridescentBlock) {
            world.setBlockState(pos, oldState.with(IridescentBlock.COLOR_OFFSET,
                    InfinityMethods.properMod(oldState.get(IridescentBlock.COLOR_OFFSET) + (reverse ? -1 : 1),
                    IridescentBlock.num_models)));
        }
        else return super.useOnBlock(context);
        if (!world.isClient())
            world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.BLOCKS, 1f, 1f);
        return ActionResult.SUCCESS;
    }
}
