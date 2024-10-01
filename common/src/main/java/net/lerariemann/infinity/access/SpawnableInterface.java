package net.lerariemann.infinity.access;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public interface SpawnableInterface {
    static boolean isInfinity(WorldAccess world, BlockPos pos) {
        return world.getBiome(pos).getKey().isPresent() && world.getBiome(pos).getKey().get().getValue().toString().contains("infinity");
    }
}
