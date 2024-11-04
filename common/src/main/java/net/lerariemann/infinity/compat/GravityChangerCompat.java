package net.lerariemann.infinity.compat;

import gravity_changer.api.GravityChangerAPI;
import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.minecraft.server.world.ServerWorld;

public class GravityChangerCompat  {
    public static void changeMavity(ServerWorld world) {
        GravityChangerAPI.setDimensionGravityStrength(world, getMavity(world));
    }

    public static double getMavity(ServerWorld world) {
        double mavity;
        try {
            mavity = ((InfinityOptionsAccess) world).infinity$getOptions().getMavity();
        } catch (Exception e) {
            mavity = 1.0;
        }
        return mavity;
    }
}
