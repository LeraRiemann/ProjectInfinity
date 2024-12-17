package net.lerariemann.infinity.mixin.core;

import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFireBlock.class)
public class AbstractFireBlockMixin {
    @Inject(method = "isOverworldOrNether(Lnet/minecraft/world/World;)Z", at = @At("RETURN"), cancellable = true)
    private static void injected(World world, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() || InfinityMethods.isInfinity(world));
    }
}
