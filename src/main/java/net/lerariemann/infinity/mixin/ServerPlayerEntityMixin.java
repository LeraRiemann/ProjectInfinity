package net.lerariemann.infinity.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.access.ServerPlayerEntityAccess;
import net.lerariemann.infinity.client.PacketTransiever;
import net.lerariemann.infinity.var.ModCommands;
import net.lerariemann.infinity.var.ModStats;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashSet;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements ServerPlayerEntityAccess {
    @Shadow public abstract ServerWorld getServerWorld();

    @Shadow public abstract boolean teleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch);

    @Shadow public abstract Entity getCameraEntity();
    @Shadow public ServerPlayNetworkHandler networkHandler;

    @Unique
    private long ticksUntilWarp;
    @Unique
    private long idForWarp;


    @Inject(method = "moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setServerWorld(Lnet/minecraft/server/world/ServerWorld;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injected2(ServerWorld destination, CallbackInfoReturnable<Entity> ci, ServerWorld serverWorld, RegistryKey<World> registryKey,
                           WorldProperties worldProperties, PlayerManager playerManager, TeleportTarget teleportTarget) {
        if (((MinecraftServerAccess)(serverWorld.getServer())).getDimensionProvider().rule("returnPortalsEnabled") &&
                (registryKey.getValue().getNamespace().equals(InfinityMod.MOD_ID))) {
            BlockPos pos = BlockPos.ofFloored(teleportTarget.position);
            boolean bl = false;
            for (BlockPos pos2: new BlockPos[] {pos, pos.add(1, 0, 0), pos.add(0, 0, 1),
                    pos.add(-1, 0, 0), pos.add(0, 0, -1)}) if (destination.getBlockState(pos2).isOf(Blocks.NETHER_PORTAL)) {
                bl = true;
                String keystr = registryKey.getValue().getPath();
                String is = keystr.substring(keystr.lastIndexOf("_") + 1);
                long i;
                try {
                    i = Long.parseLong(is);
                } catch (Exception e) {
                    i = ModCommands.getDimensionSeed(is, serverWorld.getServer());
                }
                NeitherPortalBlock.modifyPortal(destination, pos2, destination.getBlockState(pos), i, true);
                break;
            }
            if (bl) {
                ((PlayerEntity)(Object)this).increaseStat(ModStats.PORTALS_OPENED_STAT, 1);
            }
        }
    }

    @Inject(method = "moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getPlayerManager()Lnet/minecraft/server/PlayerManager;"))
    private void injected3(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        ServerPlayNetworking.send(((ServerPlayerEntity)(Object)this), InfinityMod.SHADER_RELOAD,
                PacketTransiever.buildPacket(destination));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (--this.ticksUntilWarp == 0L) {
            MinecraftServer s = this.getServerWorld().getServer();
            ServerWorld w = s.getWorld(ModCommands.getKey(this.idForWarp, s));
            if (w==null) return;
            double d = DimensionType.getCoordinateScaleFactor(this.getServerWorld().getDimension(), w.getDimension());
            Entity self = getCameraEntity();
            BlockPos blockPos2 = w.getWorldBorder().clamp(self.getX() * d, self.getY(), self.getZ() * d);
            this.teleport(w, blockPos2.getX(), blockPos2.getY(), blockPos2.getZ(), new HashSet<>(), self.getYaw(), self.getPitch());
            ServerPlayNetworking.send(((ServerPlayerEntity)(Object)this), InfinityMod.SHADER_RELOAD, PacketTransiever.buildPacket(w));
        }
    }

    @Inject(method = "changeGameMode", at = @At("RETURN"))
    private void injected4(GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) ServerPlayNetworking.send(((ServerPlayerEntity)(Object)this), InfinityMod.SHADER_RELOAD, PacketTransiever.buildPacket(this.getServerWorld()));
    }

    @Override
    public void setWarpTimer(long ticks, long dim) {
        this.ticksUntilWarp = ticks;
        this.idForWarp = dim;
    }
}
