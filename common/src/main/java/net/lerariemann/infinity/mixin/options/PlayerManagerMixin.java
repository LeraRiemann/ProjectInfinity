package net.lerariemann.infinity.mixin.options;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.ModBlockEntities;
import net.lerariemann.infinity.options.PacketTransiever;
import net.minecraft.block.BlockState;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
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
    private void injected(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci, @Local(ordinal=0) ServerWorld serverWorld2) {
        ServerPlayNetworking.send(player, InfinityMod.SHADER_RELOAD, PacketTransiever.buildPacket(serverWorld2));
        ServerPlayNetworking.send(player, InfinityMod.STARS_RELOAD, PacketByteBufs.create());
        MinecraftServerAccess acc = ((MinecraftServerAccess)(serverWorld2.getServer()));
        if (acc.projectInfinity$needsInvocation()) {
            int y = serverWorld2.getTopY() - 10;
            BlockPos pos = new BlockPos(player.getBlockX(), y, player.getBlockY());
            BlockState st = serverWorld2.getBlockState(pos);
            serverWorld2.setBlockState(pos, ModBlocks.ALTAR_COSMIC.get().getDefaultState());
            serverWorld2.getBlockEntity(pos, ModBlockEntities.ALTAR_COSMIC.get()).ifPresent(e -> e.addNull(st));
            acc.projectInfinity$onInvocation();
        }
    }

    @Inject(method="sendWorldInfo", at = @At("TAIL"))
    private void injected2(ServerPlayerEntity player, ServerWorld world, CallbackInfo ci) {
        ServerPlayNetworking.send(player, InfinityMod.SHADER_RELOAD, PacketTransiever.buildPacket(world));
        ServerPlayNetworking.send(player, InfinityMod.STARS_RELOAD, PacketByteBufs.create());
    }
}
