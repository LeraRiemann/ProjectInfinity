package net.lerariemann.infinity.mixin.iridescence;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.FluidTags;
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
    private void inj(PathContext context, int x, int y, int z, MobEntity mob, CallbackInfoReturnable<PathNodeType> cir) {
        if (Iridescence.isIridescence(entity.getWorld(), new BlockPos(x, y, z))) {
            if (entity instanceof ChaosPawn pawn && pawn.isChess())
                cir.setReturnValue(PathNodeType.BLOCKED);
        }
    }

    @ModifyExpressionValue(method= "getStart()Lnet/minecraft/entity/ai/pathing/PathNode;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    boolean inj(boolean original, @Local BlockState blockState) {
        return original || blockState.getFluidState().isIn(FluidTags.WATER);
    }
}
