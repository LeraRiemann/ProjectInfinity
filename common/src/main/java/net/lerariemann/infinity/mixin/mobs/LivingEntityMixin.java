package net.lerariemann.infinity.mixin.mobs;

import net.lerariemann.infinity.registry.core.ModStatusEffects;
import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow public abstract double getAttributeValue(RegistryEntry<EntityAttribute> attribute);
    protected LivingEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    /* Hook to allow unconventional effect removal logic */
    @Inject(method = "onStatusEffectRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;updateAttributes()V"))
    void inj(StatusEffectInstance effect, CallbackInfo ci) {
        if (effect.getEffectType().value() instanceof ModStatusEffects.SpecialEffect eff) {
            eff.onRemoved((LivingEntity)(Object)this);
        }
    }

    /* Hook for sheep dropping wool when punched */
    @Inject(method = "damage", at = @At("RETURN"))
    protected void injected_sheep(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {}

    /* Handle fall damage in dimensions with custom mavity */
    @Inject(method = "computeFallDamage", at = @At(value = "HEAD"), cancellable = true)
    protected void inj2(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir) {
        InfinityOptions options = InfinityOptions.access(getWorld());
        if (!options.isEmpty()) {
            if (this.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE)) cir.setReturnValue(0);
            else {
                double f = getAttributeValue(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE);
                double clampedMavity = Math.clamp(options.getMavity(), 0.01, 2);
                double g = fallDistance * clampedMavity - f;
                cir.setReturnValue(MathHelper.ceil(
                        (g * damageMultiplier) * this.getAttributeValue(EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER)));
            }
        }
    }
}
