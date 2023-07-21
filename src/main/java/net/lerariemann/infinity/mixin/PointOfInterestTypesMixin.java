package net.lerariemann.infinity.mixin;

import com.google.common.collect.ImmutableSet;
import net.lerariemann.infinity.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(PointOfInterestTypes.class)
public class PointOfInterestTypesMixin {
    private static Set<BlockState> getAllStatesOf(Block... blocks) {
        return (Set<BlockState>) Stream.<Block>of(blocks).flatMap(block -> block.getStateManager().getStates().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Shadow private static PointOfInterestType register(Registry<PointOfInterestType> registry, RegistryKey<PointOfInterestType> key, Set<BlockState> states, int ticketCount, int searchDistance) {
        return null;
    }

        @Redirect(method = "registerAndGetDefault(Lnet/minecraft/registry/Registry;)Lnet/minecraft/world/poi/PointOfInterestType;", at = @At(value="INVOKE",
            target="Lnet/minecraft/world/poi/PointOfInterestTypes;register(Lnet/minecraft/registry/Registry;Lnet/minecraft/registry/RegistryKey;Ljava/util/Set;II)Lnet/minecraft/world/poi/PointOfInterestType;"))
    private static PointOfInterestType injected(Registry<PointOfInterestType> registry, RegistryKey<PointOfInterestType> portal, Set<BlockState> states, int i0, int i1) {
        if (portal == PointOfInterestTypes.NETHER_PORTAL) {
            Set<BlockState> set = new HashSet<>(Blocks.NETHER_PORTAL.getStateManager().getStates());
            set.addAll(ModBlocks.NEITHER_PORTAL.getStateManager().getStates());
            return register(registry, portal, ImmutableSet.copyOf(set), i0, i1);
        }
        return register(registry, portal, states, i0, i1);
    }
}
