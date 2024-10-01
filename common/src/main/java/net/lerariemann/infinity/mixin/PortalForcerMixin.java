package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.var.ModPoi;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.dimension.PortalForcer;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {
    @ModifyArg(method = "getPortalPos", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/poi/PointOfInterestStorage;getInSquare(Ljava/util/function/Predicate;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/world/poi/PointOfInterestStorage$OccupationStatus;)Ljava/util/stream/Stream;"))
    Predicate<RegistryEntry<PointOfInterestType>> injected(Predicate<RegistryEntry<PointOfInterestType>> typePredicate) {
        return typePredicate.or(poiType -> poiType.matchesKey(ModPoi.NEITHER_PORTAL_KEY));
    }
}
