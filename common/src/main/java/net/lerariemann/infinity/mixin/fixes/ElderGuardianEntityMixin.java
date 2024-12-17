package net.lerariemann.infinity.mixin.fixes;

import net.lerariemann.infinity.access.MobEntityAccess;
import net.lerariemann.infinity.util.InfinityMethods;
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

    /* Vanilla assumes all elder guardians are exempt from mobcap logic. This allows me to actually spawn them in infdims */
    @Inject(method = "<init>", at = @At("TAIL"))
    void injected(EntityType<? extends ElderGuardianEntity> entityType, World world, CallbackInfo ci) {
        if (InfinityMethods.isInfinity(world)) {
            ((MobEntityAccess)this).infinity$setPersistent(false);
        }
    }
}
