package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.lerariemann.infinity.var.ModCommands;
import net.lerariemann.infinity.var.ModCriteria;
import net.lerariemann.infinity.var.ModStats;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {

	@Inject(at = @At("HEAD"), method = "onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
	private void injected(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo info) {
		if (!world.isClient() && entity instanceof ItemEntity) {
			ItemStack itemStack = ((ItemEntity)entity).getStack();
			if (itemStack.getItem() == Items.WRITTEN_BOOK || itemStack.getItem() == Items.WRITABLE_BOOK) {
				NbtCompound compound = itemStack.getNbt();
				MinecraftServer server = world.getServer();
				if (compound != null && server != null) {
					long i = ModCommands.getDimensionSeed(compound, server, itemStack.getItem());
					boolean b = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier("infinity:generated_" + i))) != null;
					NeitherPortalBlock.modifyPortal(world, pos, state, i, b);
					MinecraftServer s = world.getServer();
					RandomProvider prov = ((MinecraftServerAccess)(s)).getDimensionProvider();
					boolean bl = prov.portalKey.isBlank();
					if (bl) {
						NeitherPortalBlock.open(s, world, pos, false);
						PlayerEntity player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 5, false);
                        if (player != null) {
							player.increaseStat(ModStats.DIMS_OPENED_STAT, 1);
							ModCriteria.DIMS_OPENED.trigger((ServerPlayerEntity)player);
							player.increaseStat(ModStats.PORTALS_OPENED_STAT, 1);
						}
					}
					entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
				}
			}
		}
	}

	@Redirect(method="getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
	private boolean injected(BlockState neighborState, Block block) {
		return (neighborState.getBlock() instanceof NetherPortalBlock);
	}
}
