package net.lerariemann.infinity.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Boopable {
    BooleanProperty BOOP = BooleanProperty.of("boop");
    static boolean getBoop(BlockState state) {
        return state.contains(BOOP) ? state.get(BOOP) : false;
    }
    default void appendBoop(StateManager.Builder<Block, BlockState> builder) {
        builder.add(BOOP);
    }
    static void boop(World w, BlockPos pos) {
        BlockState bs = w.getBlockState(pos);
        if (bs.contains(BOOP)) w.setBlockState(pos, bs.with(BOOP, !bs.get(BOOP)));
    }
}
