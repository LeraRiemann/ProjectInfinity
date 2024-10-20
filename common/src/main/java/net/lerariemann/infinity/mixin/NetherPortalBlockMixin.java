package net.lerariemann.infinity.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.lerariemann.infinity.var.ModCommands;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.lerariemann.infinity.compat.ComputerCraftCompat.checkPrintedPage;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
	@Inject(at = @At("HEAD"), method = "onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
	private void injected(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo info) {
		if (!world.isClient() && entity instanceof ItemEntity) {
			ItemStack itemStack = ((ItemEntity)entity).getStack();
			WritableBookContentComponent writableComponent = itemStack.getComponents().get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
			WrittenBookContentComponent writtenComponent = itemStack.getComponents().get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
			String printedComponent = null;
			if (PlatformMethods.isModLoaded("computercraft")) {
				printedComponent = checkPrintedPage(itemStack);
			}
			if (writableComponent != null || writtenComponent != null || printedComponent != null) {
				String content = NeitherPortalBlock.parseComponents(writableComponent, writtenComponent, printedComponent);
				MinecraftServer server = world.getServer();
				if (server != null) {
					long l = ModCommands.getDimensionSeed(content, server);
					Identifier id = ModCommands.getIdentifier(l, server);
					NeitherPortalBlock.modifyPortalOnCollision(l, id, world, pos, state);
					entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
				}
			}
		}
	}

	@ModifyExpressionValue(method = "createTeleportTarget", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"))
	private @Nullable ServerWorld injected(@Nullable ServerWorld original,
										   @Local(argsOnly = true) ServerWorld world, @Local(argsOnly = true) BlockPos pos) {
		if (!world.getBlockState(pos).isOf(ModBlocks.NEITHER_PORTAL.get())) {
			if (!world.getRegistryKey().getValue().getNamespace().contains("infinity")) {
				return original; //when teleportation should not be redirected
			}
			return world.getServer().getWorld(World.OVERWORLD); //when we return from another dimension
		}

		NeitherPortalBlockEntity e = ((NeitherPortalBlockEntity)world.getBlockEntity(pos));
		if (e==null) return world;
		Identifier id = e.getDimension();

		RegistryKey<World> key2 = RegistryKey.of(RegistryKeys.WORLD, id);
		ServerWorld serverWorld2 = world.getServer().getWorld(key2);

		if (serverWorld2 != null && e.getOpen() && ((Timebombable)serverWorld2).projectInfinity$isTimebobmed() == 0) {
			return serverWorld2;
		}
		return world;
	}

	@Redirect(method="getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
	private boolean injected(BlockState neighborState, Block block) {
		return (neighborState.getBlock() instanceof NetherPortalBlock);
	}
}
