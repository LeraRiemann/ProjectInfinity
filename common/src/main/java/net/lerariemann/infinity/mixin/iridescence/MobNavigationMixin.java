package net.lerariemann.infinity.mixin.iridescence;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.MobNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobNavigation.class)
public class MobNavigationMixin {
    @ModifyExpressionValue(method="getPathfindingY", at= @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    boolean inj(boolean original, @Local BlockState blockState) {
        return original || blockState.isIn(ModBlocks.SWIMMABLE);
    }
}
