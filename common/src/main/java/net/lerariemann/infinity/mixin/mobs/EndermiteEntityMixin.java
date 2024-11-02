package net.lerariemann.infinity.mixin.mobs;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndermiteEntity.class)
public class EndermiteEntityMixin {
    @Inject(method = "canSpawn", at = @At("HEAD"), cancellable = true)
    private static void injected(EntityType<EndermiteEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (world.getBiome(pos).getKey().isPresent() && world.getBiome(pos).getKey().get().getValue().toString().contains("infinity")) {
            cir.setReturnValue(HostileEntity.canMobSpawn(type, world, spawnReason, pos, random) && world.getDifficulty() != Difficulty.PEACEFUL && HostileEntity.isSpawnDark((ServerWorldAccess)world, pos, random));
        }
    }
}
