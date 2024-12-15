package net.lerariemann.infinity.mixin.mobs;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.access.SpawnableInterface;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.SchoolingFishEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TropicalFishEntity.class)
public abstract class TropicalFishEntityMixin extends SchoolingFishEntity {
    public TropicalFishEntityMixin(EntityType<? extends SchoolingFishEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(method="initialize", at= @At(value = "INVOKE", target = "Lnet/minecraft/util/math/random/Random;nextFloat()F"))
    float mev(float original, @Local(argsOnly = true) ServerWorldAccess world){
        if (SpawnableInterface.isBiomeInfinity(world, getBlockPos())) return 1.0f;
        return original;
    }

    @Inject(method = "canTropicalFishSpawn", at=@At("HEAD"), cancellable = true)
    private static void inj(EntityType<TropicalFishEntity> type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (SpawnableInterface.isBiomeInfinity(world, pos)) {
            cir.setReturnValue(reason == SpawnReason.SPAWNER ||
                    (MobEntity.canMobSpawn(type, world, reason, pos, random)
                            && world.getFluidState(pos).isIn(FluidTags.WATER)
                            && world.getFluidState(pos.up()).isIn(FluidTags.WATER)));
        }
    }
}
