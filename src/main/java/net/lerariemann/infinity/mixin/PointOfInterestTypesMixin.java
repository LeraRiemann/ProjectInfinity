package net.lerariemann.infinity.mixin;

import com.google.common.collect.ImmutableSet;
import net.lerariemann.infinity.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Set;
import java.util.stream.Stream;

@Mixin(PointOfInterestTypes.class)
public class PointOfInterestTypesMixin {
    private static Set<BlockState> getAllStatesOf(Block... blocks) {
        return (Set<BlockState>) Stream.<Block>of(blocks).flatMap(block -> block.getStateManager().getStates().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @ModifyArgs(method = "registerAndGetDefault(Lnet/minecraft/registry/Registry;)Lnet/minecraft/world/poi/PointOfInterestType;", at = @At(value="INVOKE",
            target="Lnet/minecraft/world/poi/PointOfInterestTypes;register(Lnet/minecraft/registry/Registry;Lnet/minecraft/registry/RegistryKey;Ljava/util/Set;II)Lnet/minecraft/world/poi/PointOfInterestType;"))
    private static void injected(Args args){
        if (args.get(1) == PointOfInterestTypes.NETHER_PORTAL) {
            args.set(2, (Set<BlockState>)Stream.<Block>of(ModBlocks.NEITHER_PORTAL, Blocks.NETHER_PORTAL).flatMap(block -> block.getStateManager().getStates().stream()).collect(ImmutableSet.toImmutableSet()));
        }
    }
}
