package net.lerariemann.infinity.mixin.options;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.ModBlockEntities;
import net.lerariemann.infinity.util.WarpLogic;
import net.lerariemann.infinity.var.ModPayloads;
import net.minecraft.block.BlockState;
import net.minecraft.network.ClientConnection;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Unique
    public boolean infinity$needsTpOut;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injected(MinecraftServer server, CombinedDynamicRegistries registryManager, WorldSaveHandler saveHandler, int maxPlayers, CallbackInfo ci) {
        infinity$needsTpOut = false;
    }

    @Inject(method="onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getOverworld()Lnet/minecraft/server/world/ServerWorld;"))
    void inj(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        infinity$needsTpOut = true;
    }

    @Inject(method="onPlayerConnect", at = @At(value = "TAIL"))
    void inj2(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        if (infinity$needsTpOut) {
            infinity$needsTpOut = false;
            WarpLogic.respawnAlive(player);
            WarpLogic.sendToMissingno(player);
        }
    }

    @Inject(method = "onPlayerConnect", at = @At(value="INVOKE",
            target = "Lnet/minecraft/server/PlayerManager;sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void injected(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci, @Local(ordinal=0) ServerWorld serverWorld2) {
        if (serverWorld2 == null) return;
        ModPayloads.sendReloadPacket(player, serverWorld2);
        ServerPlayNetworking.send(player, ModPayloads.STARS_RELOAD, PacketByteBufs.create());
        MinecraftServerAccess acc = ((MinecraftServerAccess)(serverWorld2.getServer()));
        if (acc.infinity$needsInvocation()) {
            int y = serverWorld2.getTopY() - 10;
            BlockPos pos = new BlockPos(player.getBlockX(), y, player.getBlockZ());
            BlockState st = serverWorld2.getBlockState(pos);
            serverWorld2.setBlockState(pos, ModBlocks.ALTAR_COSMIC.get().getDefaultState());
            serverWorld2.getBlockEntity(pos, ModBlockEntities.ALTAR_COSMIC.get()).ifPresent(e -> {
                InfinityMod.LOGGER.info("Invoking the name of the Cosmic Altar...");
                e.startTime();
                e.addNull(st);
            });
        }
    }

    @Inject(method="sendWorldInfo", at = @At("TAIL"))
    private void injected2(ServerPlayerEntity player, ServerWorld world, CallbackInfo ci) {
        ModPayloads.sendReloadPacket(player, world);
        ServerPlayNetworking.send(player, ModPayloads.STARS_RELOAD, PacketByteBufs.create());
    }
}
