package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.InfinityMod;
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
        cir.setReturnValue(cir.getReturnValue() || InfinityMod.isInfinity(world));
    }
}
