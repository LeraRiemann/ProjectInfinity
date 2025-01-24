package net.lerariemann.infinity.mixin.core;

import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.core.RandomProvider;
import net.lerariemann.infinity.util.teleport.PortalCreator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin extends AbstractBlockMixin {
	@Shadow @Final public static EnumProperty<Direction.Axis> AXIS;

	/* The root hook for "throw a book in the portal" logic. */
	@Inject(at = @At("HEAD"), method = "onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
	private void injected(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo info) {
		if (world instanceof ServerWorld w && entity instanceof ItemEntity e) {
			PortalCreator.tryCreatePortalFromItem(w, pos, e);
		}
	}

	/* Allows infinity portals to consider themselves valid block configurations. */
	@Redirect(method="getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
	private boolean injected(BlockState neighborState, Block block) {
		return (neighborState.getBlock() instanceof NetherPortalBlock);
	}

	/* Makes sure that newly lit portals in infdims become infinity portals. */
	@Override
	protected void injected_onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
		if (state.isOf(Blocks.NETHER_PORTAL) &&
				(RandomProvider.rule("randomizeAllNetherPortals")
						|| InfinityMethods.isInfinity(world)))
			world.scheduleBlockTick(pos, Blocks.NETHER_PORTAL, 2);
	}
	@Override
	protected void injected_scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		if (state.isOf(Blocks.NETHER_PORTAL)) {
			boolean bl = RandomProvider.rule("randomizeAllNetherPortals");
			if (!bl && !InfinityMethods.isInfinity(world)) return;
			world.setBlockState(pos, ModBlocks.PORTAL.get().getDefaultState().
					with(AXIS, state.get(AXIS)));
			if (bl && world.getBlockEntity(pos) instanceof InfinityPortalBlockEntity ipbe) {
				long l = InfinityMethods.getRandomSeed(new java.util.Random(world.getTime()));
				ipbe.setDimension(l);
			}
		}
	}
}
