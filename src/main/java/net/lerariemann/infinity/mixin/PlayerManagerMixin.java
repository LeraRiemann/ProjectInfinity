package net.lerariemann.infinity.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.client.PacketTransiever;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
    }

    @Inject(method="sendWorldInfo", at = @At("TAIL"))
    private void injected2(ServerPlayerEntity player, ServerWorld world, CallbackInfo ci) {
        ServerPlayNetworking.send(player, InfinityMod.SHADER_RELOAD, PacketTransiever.buildPacket(world));
    }
}
