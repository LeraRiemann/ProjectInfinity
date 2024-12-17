package net.lerariemann.infinity.mixin.qol;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireBlock.class)
public class FireBlockMixin {
    /* Disabling fire tick in infdims */
    @ModifyExpressionValue(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    boolean inj(boolean original, @Local(argsOnly = true) ServerWorld world) {
        if (InfinityMethods.isInfinity(world)) return false;
        return original;
    }
}
