package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class F4Item extends PortalDataHolder {
    public F4Item(Settings settings) {
        super(settings);
    }

    Direction getDir(PlayerEntity player) {
        float yawBreak = player.getYaw() - 45.0f;
        while (yawBreak < 0) yawBreak += 360.0f;
        if (yawBreak > 270.0f) return Direction.SOUTH;
        if (yawBreak > 180.0f) return Direction.EAST;
        if (yawBreak > 90.0f) return Direction.NORTH;
        return Direction.WEST;
    }

    @Override
    public MutableText getTooltip(Identifier dimension) {
        String s = dimension.toString();
        // Randomly generated dimensions.
        if (s.contains("infinity:generated_"))
            return Text.translatable("tooltip.infinity.key.generated")
                    .append(s.replace("infinity:generated_", ""));
        // All other dimensions.
        return Text.translatableWithFallback(
                Util.createTranslationKey("dimension", dimension),
                InfinityMethods.fallback(dimension.getPath()));
    }

    @Override
    public MutableText defaultTooltip() {
        return Text.translatable("tooltip.infinity.f4.default");
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        Direction dir = getDir(player);
        Direction dir2 = dir.rotateClockwise(Direction.Axis.Y);
        BlockPos center = player.getBlockPos().offset(dir, 4);
        ItemStack st = player.getStackInHand(hand);
        Identifier id = getDestination(st);

        //finding a place position
        int y = center.getY();
        if (world.isOutOfHeightLimit(y)) return TypedActionResult.pass(st);
        int i = 0;
        while (!world.getBlockState(center).isAir()) {
            center = center.up();
            if (i++ > 8 || world.isOutOfHeightLimit(y+i+4)) return TypedActionResult.pass(st);
        }
        center = center.up();

        //placing the portal
        for (i = -2; i <= 2; i++) {
            world.setBlockState(center.offset(dir2, i).up(2), Blocks.OBSIDIAN.getDefaultState());
            world.setBlockState(center.offset(dir2, i).down(2), Blocks.OBSIDIAN.getDefaultState());
        }
        for (i = -1; i <= 1; i++) {
            world.setBlockState(center.offset(dir2, 2).offset(Direction.UP, i), Blocks.OBSIDIAN.getDefaultState());
            world.setBlockState(center.offset(dir2, -2).offset(Direction.UP, i), Blocks.OBSIDIAN.getDefaultState());
            if (!world.isClient()) for (int j = -1; j <= 1; j++) {
                BlockPos pos = center.offset(dir2, j).offset(Direction.UP, i);
                world.setBlockState(pos,
                        ((id == null) ? Blocks.NETHER_PORTAL : ModBlocks.PORTAL.get())
                                .getDefaultState().with(NetherPortalBlock.AXIS, dir2.getAxis()));
                if (id != null && world.getBlockEntity(pos) instanceof InfinityPortalBlockEntity ipbe) {
                    ipbe.setData(world.getServer(), id);
                }
            }
        }
        player.playSound(SoundEvents.BLOCK_BELL_USE, 1, 0.75f);
        st.decrementUnlessCreative(1, player);
        return TypedActionResult.consume(st);
    }
}
