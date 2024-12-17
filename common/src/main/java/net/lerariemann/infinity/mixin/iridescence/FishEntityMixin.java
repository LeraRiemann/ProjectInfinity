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
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/* These two mixin classes are to make fish swim backwards in iridescence */
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

        @ModifyArgs(method = "tick", at= @At(value = "INVOKE",
                target = "Lnet/minecraft/util/math/MathHelper;atan2(DD)D"))
        void inj(Args args) {
            if (Iridescence.isUnderEffect(fish)) {
                double g = args.get(0);
                double d = args.get(1);
                args.set(0, -1*g);
                args.set(1, -1*d);
            }
        }
    }
}
