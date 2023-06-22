package net.lerariemann.infinity.access;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface NetherPortalBlockAccess {
    void modifyPortal(World world, BlockPos pos, BlockState state, int i);
}
