package net.lerariemann.infinity.mixin.mobs;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MobEntityAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElderGuardianEntity.class)
public class ElderGuardianEntityMixin extends GuardianEntity {
    public ElderGuardianEntityMixin(EntityType<? extends GuardianEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    void injected(EntityType<? extends ElderGuardianEntity> entityType, World world, CallbackInfo ci) {
        if (InfinityMod.isInfinity(world)) {
            ((MobEntityAccess)this).infinity$setPersistent(false);
        }
    }
}