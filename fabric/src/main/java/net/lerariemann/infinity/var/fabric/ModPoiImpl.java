package net.lerariemann.infinity.var.fabric;

import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.ModBlocks;

@SuppressWarnings("unused")
public class ModPoiImpl {
    public static void registerPoiFabric() {
        PointOfInterestHelper.register(InfinityMod.getId("neither_portal"), 0, 1, ImmutableSet.copyOf(ModBlocks.NEITHER_PORTAL.get().getStateManager().getStates()));
    }
}
