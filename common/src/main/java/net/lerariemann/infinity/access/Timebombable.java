package net.lerariemann.infinity.access;

import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.WarpLogic;
import net.lerariemann.infinity.registry.var.ModCriteria;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Timebombable {
    int cooldownTicks = 6000;

    void infinity$timebomb();
    boolean infinity$tryRestore();

    boolean infinity$isTimebombed();
    int infinity$getTimebombProgress();

    default void tickTimebombProgress(ServerPlayerEntity player) {
        int i = infinity$getTimebombProgress();
        if (i > 3540) {
            WarpLogic.respawnAlive(player);
        }
        else if (i > 3500) {
            ModCriteria.WHO_REMAINS.get().trigger(player);
        }
        else if (i > 200) {
            if (i%4 == 0) {
                Registry<DamageType> r = player.getServerWorld().getServer().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);
                RegistryEntry<DamageType> entry = r.getEntry(r.get(InfinityMethods.getId("world_ceased")));
                player.damage(new DamageSource(entry), i > 400 ? 2.0f : 1.0f);
            }
        }
    }
}
