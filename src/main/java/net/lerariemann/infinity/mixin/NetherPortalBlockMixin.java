package net.lerariemann.infinity.mixin;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import net.lerariemann.infinity.access.NetherPortalBlockAccess;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.lerariemann.infinity.var.ModCommands;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Queue;
import java.util.Set;

import static net.minecraft.block.NetherPortalBlock.AXIS;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin implements NetherPortalBlockAccess {

	@Inject(at = @At("HEAD"), method = "onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
	private void injected(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo info) {
		if (!world.isClient() && entity instanceof ItemEntity) {
			ItemStack itemStack = ((ItemEntity)entity).getStack();
			if (itemStack.getItem() == Items.WRITTEN_BOOK || itemStack.getItem() == Items.WRITABLE_BOOK) {
				NbtCompound compound = itemStack.getNbt();
				MinecraftServer server = world.getServer();
				if (compound != null && server != null) {
					long i = ModCommands.getDimensionSeed(compound.asString(), server);
					modifyPortal(world, pos, state, i);
					entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
				}
			}
		}
	}

	@Redirect(method="getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
	private boolean injected(BlockState neighborState, Block block) {
		return (neighborState.getBlock() instanceof NetherPortalBlock);
	}

	@Unique
	private void changeDim(World world, BlockPos pos, Direction.Axis axis, long i) {
		world.setBlockState(pos, ModBlocks.NEITHER_PORTAL.getDefaultState().with(AXIS, axis));
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity != null) ((NeitherPortalBlockEntity)blockEntity).setDimension(i);
	}

	@Override
	public void modifyPortal(World world, BlockPos pos, BlockState state, long i) {
		Set<BlockPos> set = Sets.newHashSet();
		Queue<BlockPos> queue = Queues.newArrayDeque();
		queue.add(pos);
		BlockPos blockPos;
		Direction.Axis axis = state.get(AXIS);
		while ((blockPos = queue.poll()) != null) {
			set.add(blockPos);
			BlockState blockState = world.getBlockState(blockPos);
			if (blockState.getBlock() instanceof NetherPortalBlock || blockState.getBlock() instanceof NeitherPortalBlock) {
				this.changeDim(world, blockPos, axis, i);
				BlockPos blockPos2 = blockPos.offset(Direction.UP);
				if (!set.contains(blockPos2))
					queue.add(blockPos2);
				blockPos2 = blockPos.offset(Direction.DOWN);
				if (!set.contains(blockPos2))
					queue.add(blockPos2);
				blockPos2 = blockPos.offset(Direction.NORTH);
				if (!set.contains(blockPos2))
					queue.add(blockPos2);
				blockPos2 = blockPos.offset(Direction.SOUTH);
				if (!set.contains(blockPos2))
					queue.add(blockPos2);
				blockPos2 = blockPos.offset(Direction.WEST);
				if (!set.contains(blockPos2))
					queue.add(blockPos2);
				blockPos2 = blockPos.offset(Direction.EAST);
				if (!set.contains(blockPos2))
					queue.add(blockPos2);
			}
		}
	}
}
