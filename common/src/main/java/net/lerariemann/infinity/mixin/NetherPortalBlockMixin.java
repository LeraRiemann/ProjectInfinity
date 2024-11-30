package net.lerariemann.infinity.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.lerariemann.infinity.util.PortalCreationLogic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
	@Inject(at = @At("HEAD"), method = "onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
	private void injected(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo info) {
		if (world instanceof ServerWorld w && entity instanceof ItemEntity e) {
			PortalCreationLogic.tryCreatePortalFromItem(w, pos, e);
		}
	}

	@ModifyExpressionValue(method = "createTeleportTarget", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"))
	private @Nullable ServerWorld injected(@Nullable ServerWorld original,
										   @Local(argsOnly = true) ServerWorld world, @Local(argsOnly = true) BlockPos pos) {
		if (!world.getBlockState(pos).isOf(ModBlocks.NEITHER_PORTAL.get())) {
			if (!InfinityMod.isInfinity(world)) {
				return original; //when teleportation should not be redirected
			}
			return world.getServer().getWorld(World.OVERWORLD); //when we return from another dimension
		}

		if (world.getBlockEntity(pos) instanceof NeitherPortalBlockEntity e) {
			Identifier id = e.getDimension();

			RegistryKey<World> key2 = RegistryKey.of(RegistryKeys.WORLD, id);
			ServerWorld serverWorld2 = world.getServer().getWorld(key2);

			if (serverWorld2 != null && e.getOpen() && !((Timebombable)serverWorld2).infinity$isTimebombed()) {
				return serverWorld2;
			}
		}
		return world;
	}

	@Redirect(method="getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
	private boolean injected(BlockState neighborState, Block block) {
		return (neighborState.getBlock() instanceof NetherPortalBlock);
	}
}
