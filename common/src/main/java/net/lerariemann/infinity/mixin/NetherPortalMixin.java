package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.world.dimension.NetherPortal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherPortal.class)
public class NetherPortalMixin {
    @Inject(method = "validStateInsidePortal(Lnet/minecraft/block/BlockState;)Z", at = @At("RETURN"), cancellable = true)
    private static void injected(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() || state.isOf(ModBlocks.NEITHER_PORTAL));
    }
}
