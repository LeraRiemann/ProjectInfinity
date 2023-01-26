package net.lerariemann.infinity.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.IntStream;
import java.util.stream.Collectors;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
	private int dimension;

	public int getDimension(){
		return this.dimension;
	}
	public void setDimension(int dimension) {
		this.dimension = dimension;
	}
	@Inject(at = @At("HEAD"), method = "onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
	private void injected(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo info) {
		if (entity instanceof ItemEntity) {
			ItemStack itemStack = ((ItemEntity)entity).getStack();
			if (itemStack.getItem() == Items.WRITTEN_BOOK || itemStack.getItem() == Items.WRITABLE_BOOK) {
				BookScreen.Contents bookContent = BookScreen.Contents.create(itemStack);
				String string = IntStream.range(0, bookContent.getPageCount()).mapToObj(bookContent::getPage).map(u -> {return u.getString();}).collect(Collectors.joining("\n"));
				if(!string.isEmpty()){
					int i = 0;
					modifyPortal(world, pos, state, i);
					entity.remove(Entity.RemovalReason.DISCARDED);
				}
			}
		}
	}

	private void modifyPortal(World world, BlockPos pos, BlockState state, int i) {
		world.setBlockState(pos, (BlockState)Blocks.BUDDING_AMETHYST.getDefaultState());
	}
}
