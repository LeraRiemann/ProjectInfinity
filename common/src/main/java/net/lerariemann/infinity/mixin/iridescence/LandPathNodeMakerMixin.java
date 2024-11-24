package net.lerariemann.infinity.mixin.iridescence;

import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LandPathNodeMaker.class)
public abstract class LandPathNodeMakerMixin extends PathNodeMaker {
    @Inject(method = "getNodeType(Lnet/minecraft/world/BlockView;IIILnet/minecraft/entity/mob/MobEntity;)Lnet/minecraft/entity/ai/pathing/PathNodeType;",
            at = @At("HEAD"), cancellable = true)
    private void inj(BlockView world, int x, int y, int z, MobEntity mob, CallbackInfoReturnable<PathNodeType> cir) {
        if (entity instanceof ChaosPawn pawn && pawn.isChess() && Iridescence.isIridescence(pawn.getWorld(), new BlockPos(x, y, z)))
            cir.setReturnValue(PathNodeType.BLOCKED);
    }
}
