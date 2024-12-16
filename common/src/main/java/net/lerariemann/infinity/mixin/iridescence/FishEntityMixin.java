package net.lerariemann.infinity.mixin.iridescence;

import net.lerariemann.infinity.iridescence.Iridescence;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FishEntity.class)
public class FishEntityMixin extends WaterCreatureEntity {
    protected FishEntityMixin(EntityType<? extends WaterCreatureEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(method="travel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/passive/FishEntity;updateVelocity(FLnet/minecraft/util/math/Vec3d;)V"),
    index = 1)
    Vec3d inj(Vec3d par2) {
        if (Iridescence.isUnderEffect(this)) {
            return par2.multiply(-1, 1, -1);
        }
        return par2;
    }

    @Mixin(FishEntity.FishMoveControl.class)
    public static class FishMoveControlMixin extends MoveControl {
        @Final
        @Shadow
        private FishEntity fish;

        public FishMoveControlMixin(MobEntity entity) {
            super(entity);
        }

        @ModifyArg(method = "tick", at= @At(value = "INVOKE",
                target = "Lnet/minecraft/util/math/MathHelper;atan2(DD)D"), index = 0)
        double inj(double y) {
            if (Iridescence.isUnderEffect(fish)) {
                return -y;
            }
            return y;
        }
        @ModifyArg(method = "tick", at= @At(value = "INVOKE",
                target = "Lnet/minecraft/util/math/MathHelper;atan2(DD)D"), index = 1)
        double inj2(double y) {
            if (Iridescence.isUnderEffect(fish)) {
                return -y;
            }
            return y;
        }
    }
}
