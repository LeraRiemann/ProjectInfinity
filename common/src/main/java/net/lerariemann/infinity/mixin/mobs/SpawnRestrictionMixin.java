package net.lerariemann.infinity.mixin.mobs;

import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.var.InfinitySpawnHelper;
import net.minecraft.entity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Assumes direct control over restrictions on mob spawning in infinity biomes. */
@Mixin(SpawnRestriction.class)
public class SpawnRestrictionMixin {
    @Inject(method = "canSpawn", at = @At("HEAD"), cancellable = true)
    private static <T extends Entity> void inj(EntityType<T> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (InfinityMethods.isBiomeInfinity(world, pos)) {
            cir.setReturnValue(InfinitySpawnHelper.canSpawn(type, world, spawnReason, pos, random));
        }
    }
}
