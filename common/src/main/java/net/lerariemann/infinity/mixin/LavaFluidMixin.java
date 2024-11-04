package net.lerariemann.infinity.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LavaFluid.class)
public class LavaFluidMixin {
    @ModifyExpressionValue(method = "onRandomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    boolean inj(boolean original, @Local(argsOnly = true) World world) {
        if (InfinityMod.isInfinity(world)) return false;
        return original;
    }
}