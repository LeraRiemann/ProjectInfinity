package net.lerariemann.infinity.compat;

import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.world.World;

public class PonderCompat {
    public static boolean isPonderLevel(World world) {
        return world instanceof PonderLevel;
    }
}
