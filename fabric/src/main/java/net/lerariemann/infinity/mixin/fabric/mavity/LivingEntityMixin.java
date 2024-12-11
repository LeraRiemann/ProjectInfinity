package net.lerariemann.infinity.mixin.fabric.mavity;

import net.lerariemann.infinity.access.MavityInterface;
import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable, MavityInterface {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyConstant(method = "travel", constant = @Constant(doubleValue = 0.01))
    double inj3(double value) {
        return value*getMavity();
    }

    @Inject(method = "computeFallDamage", at = @At(value = "HEAD"), cancellable = true)
    protected void inj2(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir) {
        InfinityOptions options = InfinityOptions.access(getWorld());
        if (!options.isEmpty()) {
            if (this.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE)) cir.setReturnValue(0);
            else {
                double f = 3.0F;
                double clampedMavity = MathHelper.clamp(options.getMavity(), 0.01, 2);
                double g = fallDistance * clampedMavity - f;
                cir.setReturnValue(MathHelper.ceil(
                        (g * damageMultiplier) * 1.0F));
            }
        }
    }
}
