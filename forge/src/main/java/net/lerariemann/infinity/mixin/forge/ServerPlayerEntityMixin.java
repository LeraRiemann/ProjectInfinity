package net.lerariemann.infinity.mixin.forge;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.access.ServerPlayerEntityAccess;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.lerariemann.infinity.util.PortalCreationLogic;
import net.lerariemann.infinity.var.ModPayloads;
import net.lerariemann.infinity.var.ModStats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityAccess {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract boolean teleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch);
    @Shadow public abstract boolean damage(DamageSource source, float amount);
    @Shadow @Final public MinecraftServer server;

    @Inject(method = "lambda$changeDimension$8", at= @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setServerWorld(Lnet/minecraft/server/world/ServerWorld;)V"))
    private void convertReturnPortal(ServerWorld world, RegistryKey<World> registryKey, ServerWorld destination, TeleportTarget teleportTarget, Boolean spawnPortal, CallbackInfoReturnable<Entity> cir) {
        boolean bl = PortalCreationLogic.convertReturnPortal(destination, server, registryKey, teleportTarget);
        if (bl) {
            this.increaseStat(ModStats.PORTALS_OPENED_STAT, 1);
        }
    }

    @Inject(method = "changeDimension",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getPlayerManager()Lnet/minecraft/server/PlayerManager;"))
    private void changeDimensionReload(ServerWorld destination, ITeleporter teleporter, CallbackInfoReturnable<Entity> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        ModPayloads.sendReloadPacket(player, destination);
        ServerPlayNetworking.send(player, ModPayloads.STARS_RELOAD, PacketByteBufs.create());
    }


}
