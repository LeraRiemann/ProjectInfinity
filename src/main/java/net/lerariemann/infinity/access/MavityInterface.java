package net.lerariemann.infinity.access;

import net.minecraft.world.World;

public interface MavityInterface {
    World getWorld();

    default double getMavity() {
        double mavity;
        try {
            mavity = ((InfinityOptionsAccess) getWorld()).getInfinityOptions().getMavity();
        } catch (Exception e) {
            mavity = 1.0;
        }
        return mavity;
    }
}
