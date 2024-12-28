package net.lerariemann.infinity.mixin.mobs;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SchoolingFishEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TropicalFishEntity.class)
public abstract class TropicalFishEntityMixin extends SchoolingFishEntity {
    public TropicalFishEntityMixin(EntityType<? extends SchoolingFishEntity> entityType, World world) {
        super(entityType, world);
    }

    /* In infinity biomes, tropical fish spawn in a fully random variant. */
    @ModifyExpressionValue(method="initialize", at= @At(value = "INVOKE", target = "Lnet/minecraft/util/math/random/Random;nextFloat()F"))
    float mev(float original, @Local(argsOnly = true) ServerWorldAccess world){
        if (InfinityMethods.isBiomeInfinity(world, getBlockPos())) return 1.0f;
        return original;
    }
}
