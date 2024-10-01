package net.lerariemann.infinity.mixin.mobs.passive;

import net.lerariemann.infinity.access.SpawnableInterface;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MooshroomEntity.class)
public abstract class MooshroomEntityMixin extends AnimalEntity {
    protected MooshroomEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "canSpawn", at = @At("HEAD"), cancellable = true)
    private static void injected(EntityType<? extends AnimalEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (SpawnableInterface.isInfinity(world, pos)) {
            cir.setReturnValue(canMobSpawn(type, world, spawnReason, pos, random));
        }
    }
}
