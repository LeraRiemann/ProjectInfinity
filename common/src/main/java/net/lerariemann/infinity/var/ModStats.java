package net.lerariemann.infinity.var;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

import static net.lerariemann.infinity.InfinityMod.getId;
import static net.lerariemann.infinity.PlatformMethods.freeze;
import static net.lerariemann.infinity.PlatformMethods.unfreeze;

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

    public static void registerStats() {
        unfreeze(Registries.CUSTOM_STAT);
        Registry.register(Registries.CUSTOM_STAT, DIMS_OPENED, DIMS_OPENED);
        Registry.register(Registries.CUSTOM_STAT, PORTALS_OPENED, PORTALS_OPENED);
        Registry.register(Registries.CUSTOM_STAT, WORLDS_DESTROYED, WORLDS_DESTROYED);
        ModStats.load();
        freeze(Registries.CUSTOM_STAT);
    }
}

