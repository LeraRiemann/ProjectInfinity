package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.entity.ChromaticBlockEntity;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.util.BackportMethods;
import net.lerariemann.infinity.util.var.ColorLogic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ChromaticBlockItem extends BlockItem implements PortalDataHolder {
    public ChromaticBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    //todo: implement this properly
    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        boolean bl = writeNbtToBlockEntity(world, player, pos, stack);
        if (player != null && player.getStackInHand(Hand.OFF_HAND)
                .isOf(ModItems.CHROMATIC_MATTER.get())
                && world.getBlockEntity(pos) instanceof ChromaticBlockEntity cbe) {
            cbe.setColor(BackportMethods.getOrDefaultInt(stack, ModComponentTypes.COLOR, 0xFFFFFF));
        }
        return bl;
    }

    @Override
    public Text getName() {
        return Text.translatable(this.getTranslationKey()).setStyle(Style.EMPTY.withColor(ColorLogic.defaultChromatic));
    }
}
