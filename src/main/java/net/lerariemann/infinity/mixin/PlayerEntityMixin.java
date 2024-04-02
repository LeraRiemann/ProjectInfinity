package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.access.Timebombable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method="findRespawnPosition", at = @At("HEAD"), cancellable = true)
    private static void injected(ServerWorld world, BlockPos pos, float angle, boolean forced, boolean alive, CallbackInfoReturnable<Optional<Vec3d>> cir) {
        if (((Timebombable)world).isTimebobmed() > 0) cir.setReturnValue(Optional.empty());
    }
}
