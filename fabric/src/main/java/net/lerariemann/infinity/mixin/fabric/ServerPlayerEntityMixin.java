package net.lerariemann.infinity.mixin.fabric;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.ServerPlayerEntityAccess;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.lerariemann.infinity.util.RandomProvider;
import net.lerariemann.infinity.options.PacketTransiever;
import net.lerariemann.infinity.var.ModStats;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityAccess {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract boolean teleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch);
    @Shadow public abstract boolean damage(DamageSource source, float amount);
    @Shadow @Final public MinecraftServer server;


    @Inject(method = "moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setServerWorld(Lnet/minecraft/server/world/ServerWorld;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injected2(ServerWorld destination, CallbackInfoReturnable<Entity> ci, ServerWorld serverWorld, RegistryKey<World> registryKey,
                           WorldProperties worldProperties, PlayerManager playerManager, TeleportTarget teleportTarget) {
        if (RandomProvider.getProvider(server).rule("returnPortalsEnabled") &&
                (registryKey.getValue().getNamespace().equals(InfinityMod.MOD_ID))) {
            BlockPos pos = BlockPos.ofFloored(teleportTarget.position);
            boolean bl = false;
            for (BlockPos pos2: new BlockPos[] {pos, pos.add(1, 0, 0), pos.add(0, 0, 1),
                    pos.add(-1, 0, 0), pos.add(0, 0, -1)}) if (destination.getBlockState(pos2).isOf(Blocks.NETHER_PORTAL)) {
                bl = true;
                String keystr = registryKey.getValue().getPath();
                String is = keystr.substring(keystr.lastIndexOf("_") + 1);
                Identifier dimensionName = registryKey.getValue();
                NeitherPortalBlock.modifyPortalRecursive(destination, pos2, destination.getBlockState(pos), dimensionName, true);
                break;
            }
            if (bl) {
                this.increaseStat(ModStats.PORTALS_OPENED_STAT, 1);
            }
        }
    }

    @Inject(method = "moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getPlayerManager()Lnet/minecraft/server/PlayerManager;"))
    private void injected3(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        ServerPlayNetworking.send(((ServerPlayerEntity)(Object)this), InfinityMod.SHADER_RELOAD,
                PacketTransiever.buildPacket(destination));
        ServerPlayNetworking.send(((ServerPlayerEntity)(Object)this), InfinityMod.STARS_RELOAD, PacketByteBufs.create());
    }

}