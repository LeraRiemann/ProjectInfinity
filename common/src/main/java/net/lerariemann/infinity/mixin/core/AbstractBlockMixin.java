package net.lerariemann.infinity.mixin.core;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* Provides hooks for NetherPortalBlockMixin. */
@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "onBlockAdded", at = @At("RETURN"))
    protected void injected_onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {}

    @Inject(method = "scheduledTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V", at = @At("RETURN"))
    protected void injected_scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {}
}
