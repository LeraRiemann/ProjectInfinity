package net.lerariemann.infinity.registry.var.fabric;

import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.util.InfinityMethods;

@SuppressWarnings("unused")
public class ModPoiImpl {
    public static void registerPoiFabric() {
        PointOfInterestHelper.register(InfinityMethods.getId("neither_portal"), 0, 1, ImmutableSet.copyOf(ModBlocks.PORTAL.get().getStateManager().getStates()));
    }
}
