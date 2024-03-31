package net.lerariemann.infinity.mixin.mavity;

import net.lerariemann.infinity.access.MavityInterface;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable, MavityInterface {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyArg(method = "computeFallDamage", at = @At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;ceil(F)I"))
    float injected(float value) {
        return (float)getMavity() * value;
    }

    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.08))
    double inj2(double value) {
        return value*getMavity();
    }

    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.01))
    double inj3(double value) {
        return value*getMavity();
    }
}
