package net.lerariemann.infinity.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin extends MobEntity {
    protected EnderDragonEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }
    @ModifyExpressionValue(method = "destroyBlocks", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    boolean mev(boolean original) {
        return original && !InfinityMethods.isInfinity(getWorld());
    }
}
