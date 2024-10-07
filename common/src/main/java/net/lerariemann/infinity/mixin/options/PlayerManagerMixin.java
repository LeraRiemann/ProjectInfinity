package net.lerariemann.infinity.mixin.options;

import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.ModBlockEntities;
import net.lerariemann.infinity.var.ModPayloads;
import net.minecraft.block.BlockState;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At(value="INVOKE",
            target = "Lnet/minecraft/server/PlayerManager;sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void injected(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci, @Local(ordinal=0) ServerWorld serverWorld2) {
        PlatformMethods.sendServerPlayerEntity(player, ModPayloads.setShaderFromWorld(serverWorld2));
        PlatformMethods.sendServerPlayerEntity(player, ModPayloads.StarsRePayLoad.INSTANCE);
        MinecraftServerAccess acc = ((MinecraftServerAccess)(serverWorld2.getServer()));
        if (acc.projectInfinity$needsInvocation()) {
            int y = serverWorld2.getTopY() - 10;
            BlockPos pos = new BlockPos(player.getBlockX(), y, player.getBlockY());
            BlockState st = serverWorld2.getBlockState(pos);
            serverWorld2.setBlockState(pos, ModBlocks.ALTAR_COSMIC.get().getDefaultState());
            serverWorld2.getBlockEntity(pos, ModBlockEntities.ALTAR_COSMIC).ifPresent(e -> e.addNull(st));
            acc.projectInfinity$onInvocation();
        }
    }

    @Inject(method="sendWorldInfo", at = @At("TAIL"))
    private void injected2(ServerPlayerEntity player, ServerWorld world, CallbackInfo ci) {
        PlatformMethods.sendServerPlayerEntity(player, ModPayloads.setShaderFromWorld(world));
        PlatformMethods.sendServerPlayerEntity(player, ModPayloads.StarsRePayLoad.INSTANCE);
    }
}
