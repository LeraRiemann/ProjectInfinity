package net.lerariemann.infinity.mixin.mavity;

import net.lerariemann.infinity.access.MavityInterface;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({FallingBlockEntity.class, TntEntity.class, ItemEntity.class, ExperienceOrbEntity.class, AbstractMinecartEntity.class, LlamaSpitEntity.class})
public abstract class SeveralEntitiesMixin extends Entity implements MavityInterface {
    public SeveralEntitiesMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyArg(method = "tick", at = @At(value="INVOKE", target="Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"), index = 1)
    double injected(double x) {
        return getMavity() * x;
    }
}
