package net.lerariemann.infinity.mixin.mavity;

import net.lerariemann.infinity.access.MavityInterface;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin extends ProjectileEntity implements MavityInterface {
    public PersistentProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(method = "tick", at = @At(value="INVOKE", target="Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setVelocity(DDD)V"), index = 1)
    double injected(double x) {
        return x - 0.05 * (getMavity() - 1);
    }
}
