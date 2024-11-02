package net.lerariemann.infinity.var;

import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.InfinityMod.getId;

public class ModStats {
    public static Identifier DIMS_OPENED = getId("dimensions_opened_stat");
    public static Stat<Identifier> DIMS_OPENED_STAT;
    public static Identifier WORLDS_DESTROYED = getId("worlds_destroyed_stat");
    public static Stat<Identifier> WORLDS_DESTROYED_STAT;
    public static Identifier PORTALS_OPENED = getId("portals_opened_stat");
    public static Stat<Identifier> PORTALS_OPENED_STAT;

    public static void load() {
        DIMS_OPENED_STAT = Stats.CUSTOM.getOrCreateStat(DIMS_OPENED);
        PORTALS_OPENED_STAT = Stats.CUSTOM.getOrCreateStat(PORTALS_OPENED);
        WORLDS_DESTROYED_STAT = Stats.CUSTOM.getOrCreateStat(WORLDS_DESTROYED);
    }

    public static final DeferredRegister<Identifier> STATS = DeferredRegister.create(MOD_ID, RegistryKeys.CUSTOM_STAT);


    public static void registerStats() {
        STATS.register(DIMS_OPENED, () -> DIMS_OPENED);
        STATS.register(PORTALS_OPENED, () -> PORTALS_OPENED);
        STATS.register(WORLDS_DESTROYED, () -> WORLDS_DESTROYED);
        STATS.register();
    }
}
