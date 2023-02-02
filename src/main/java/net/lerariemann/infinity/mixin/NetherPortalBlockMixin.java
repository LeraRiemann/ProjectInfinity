package net.lerariemann.infinity.mixin;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

import java.util.Queue;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import static net.minecraft.block.NetherPortalBlock.AXIS;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {

	@Inject(at = @At("HEAD"), method = "onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
	private void injected(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo info) {
		if (entity instanceof ItemEntity) {
			ItemStack itemStack = ((ItemEntity)entity).getStack();
			if (itemStack.getItem() == Items.WRITTEN_BOOK || itemStack.getItem() == Items.WRITABLE_BOOK) {
				BookScreen.Contents bookContent = BookScreen.Contents.create(itemStack);
				String string = IntStream.range(0, bookContent.getPageCount()).mapToObj(bookContent::getPage).map(u -> {return u.getString();}).collect(Collectors.joining("\n"));
				if(!string.isEmpty()){
					int i = Hashing.sha256().hashString(string, StandardCharsets.UTF_8).asInt() & Integer.MAX_VALUE;
					if (!world.isClient()) {
						RandomDimension d = new RandomDimension(i, new RandomProvider("config/"+ InfinityMod.MOD_ID + "/"), world.getServer().getSavePath(WorldSavePath.DATAPACKS).toString());
					}
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

	private void changeDim(World world, BlockPos pos, Direction.Axis axis, int i) {
		world.setBlockState(pos, ModBlocks.NEITHER_PORTAL.getDefaultState().with((Property)AXIS, (Comparable)axis));
		BlockEntity blockEntity = world.getBlockEntity(pos);
		((NeitherPortalBlockEntity)blockEntity).setDimension(i);
	}

	private void modifyPortal(World world, BlockPos pos, BlockState state, int i) {
		Set<BlockPos> set = Sets.newHashSet();
		Queue<BlockPos> queue = Queues.newArrayDeque();
		queue.add(pos);
		BlockPos blockPos;
		Direction.Axis axis = (Direction.Axis)state.get((Property<Direction.Axis>)AXIS);
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
