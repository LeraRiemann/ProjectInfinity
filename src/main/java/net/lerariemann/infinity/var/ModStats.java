package net.lerariemann.infinity.var;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

import static net.lerariemann.infinity.InfinityMod.getId;

public class ModStats {
    public static Identifier DIMS_OPENED = getId("dimensions_opened_stat");
    public static Stat<Identifier> DIMS_OPENED_STAT;
    public static Identifier PORTALS_OPENED = getId("portals_opened_stat");
    public static Stat<Identifier> PORTALS_OPENED_STAT;

    public static void load() {
        DIMS_OPENED_STAT = Stats.CUSTOM.getOrCreateStat(DIMS_OPENED);
        PORTALS_OPENED_STAT = Stats.CUSTOM.getOrCreateStat(PORTALS_OPENED);
    }

    public static void registerStats() {
        Registry.register(Registries.CUSTOM_STAT, DIMS_OPENED, DIMS_OPENED);
        Registry.register(Registries.CUSTOM_STAT, PORTALS_OPENED, PORTALS_OPENED);
        ModStats.load();
    }
}

