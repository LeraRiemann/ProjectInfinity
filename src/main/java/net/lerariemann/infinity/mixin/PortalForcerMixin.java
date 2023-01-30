package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.poi.ModPoi;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.PortalForcer;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {
    @ModifyArg(method = "getPortalRect(Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/world/border/WorldBorder;)Ljava/util/Optional;",
    at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/poi/PointOfInterestStorage;getInSquare(Ljava/util/function/Predicate;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/world/poi/PointOfInterestStorage$OccupationStatus;)Ljava/util/stream/Stream;"),
    index = 0)
    Predicate<RegistryEntry<PointOfInterestType>> injected(Predicate<RegistryEntry<PointOfInterestType>> typePredicate)
    {
        return typePredicate.or(poiType -> (poiType.matchesKey(ModPoi.NEITHER_PORTAL_KEY)));
    }
}
