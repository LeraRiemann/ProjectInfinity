package net.lerariemann.infinity.mixin.options;

import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow public abstract double getAttributeValue(RegistryEntry<EntityAttribute> attribute);

    protected LivingEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "damage", at = @At("RETURN"))
    protected void injected_sheep(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {}

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
