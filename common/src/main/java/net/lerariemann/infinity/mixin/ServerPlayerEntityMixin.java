package net.lerariemann.infinity.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.access.ServerPlayerEntityAccess;
import net.lerariemann.infinity.var.ModPayloads;
import net.lerariemann.infinity.util.WarpLogic;
import net.lerariemann.infinity.var.ModCriteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityAccess {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract ServerWorld getServerWorld();

    @Shadow public abstract boolean teleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch);

    @Shadow public abstract Entity getCameraEntity();

    @Shadow public abstract boolean damage(DamageSource source, float amount);

    @Unique private long infinity$ticksUntilWarp;
    @Unique private Identifier infinity$idForWarp;


    @Inject(method="findRespawnPosition", at = @At("HEAD"), cancellable = true)
    private static void injected(ServerWorld world, BlockPos pos, float angle, boolean forced, boolean alive, CallbackInfoReturnable<Optional<Vec3d>> cir) {
        if (((Timebombable)world).infinity$isTimebombed() > 0) cir.setReturnValue(Optional.empty());
    }

    /* When the player is using the Infinity portal, this modifies the portal on the other side if needed. */
    @Inject(method = "teleportTo",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setServerWorld(Lnet/minecraft/server/world/ServerWorld;)V")
    )
    private void injected2(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir, @Local(ordinal = 0) ServerWorld serverWorld, @Local RegistryKey<World> registryKey) {
        if (RandomProvider.getProvider(serverWorld.getServer()).rule("returnPortalsEnabled") &&
                (registryKey.getValue().getNamespace().equals(InfinityMod.MOD_ID))) {
            BlockPos pos = BlockPos.ofFloored(teleportTarget.pos());
            ServerWorld destination = teleportTarget.world();
            for (BlockPos pos2: new BlockPos[] {pos, pos.add(1, 0, 0), pos.add(0, 0, 1),
                    pos.add(-1, 0, 0), pos.add(0, 0, -1)}) if (destination.getBlockState(pos2).isOf(Blocks.NETHER_PORTAL)) {
                Identifier dimensionName = registryKey.getValue();

                NeitherPortalBlock.modifyPortalRecursive(destination, pos2, destination.getBlockState(pos), dimensionName, true);
                break;
            }
        }
    }

    @Inject(method = "teleportTo",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getPlayerManager()Lnet/minecraft/server/PlayerManager;"))
    private void injected3(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir) {
        PlatformMethods.sendS2CPayload(((ServerPlayerEntity)(Object)this), ModPayloads.setShaderFromWorld(teleportTarget.world()));
        PlatformMethods.sendS2CPayload(((ServerPlayerEntity)(Object)this), ModPayloads.StarsRePayLoad.INSTANCE);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (--this.infinity$ticksUntilWarp == 0L) {
            MinecraftServer s = this.getServerWorld().getServer();
            ServerWorld w = s.getWorld(RegistryKey.of(RegistryKeys.WORLD, this.infinity$idForWarp));
            if (w==null) return;
            double d = DimensionType.getCoordinateScaleFactor(this.getServerWorld().getDimension(), w.getDimension());
            Entity self = getCameraEntity();
            double y = MathHelper.clamp(self.getY(), w.getBottomY(), w.getTopY());
            BlockPos blockPos2 = WarpLogic.getPosForWarp(w.getWorldBorder().clamp(self.getX() * d, y, self.getZ() * d), w);
            this.teleport(w, blockPos2.getX(), blockPos2.getY(), blockPos2.getZ(), new HashSet<>(), self.getYaw(), self.getPitch());
        }
        int i = ((Timebombable)(getServerWorld())).infinity$isTimebombed();
        if (i > 200) {
            if (i%4 == 0) {
                Registry<DamageType> r = getServerWorld().getServer().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);
                RegistryEntry<DamageType> entry = r.getEntry(r.get(InfinityMod.getId("world_ceased")));
                damage(new DamageSource(entry), i > 400 ? 2.0f : 1.0f);
            }
            if (i > 3500) ModCriteria.WHO_REMAINS.trigger((ServerPlayerEntity)(Object)this);
            if (i > 3540) {
                WarpLogic.respawnAlive((ServerPlayerEntity)(Object)this);
            }
        }
    }

    @Inject(method = "changeGameMode", at = @At("RETURN"))
    private void injected4(GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) ServerPlayNetworking.send(((ServerPlayerEntity)(Object)this), ModPayloads.SHADER_RELOAD, ModPayloads.buildPacket(this.getServerWorld()));
    }

    @Inject(method= "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V", at = @At(value="INVOKE", target ="Lnet/minecraft/server/PlayerManager;sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void injected5(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        ServerPlayNetworking.send(((ServerPlayerEntity)(Object)this), ModPayloads.SHADER_RELOAD, ModPayloads.buildPacket(targetWorld));
        ServerPlayNetworking.send(((ServerPlayerEntity)(Object)this), ModPayloads.STARS_RELOAD, PlatformMethods.createPacketByteBufs());
    }


    @Override
    public void projectInfinity$setWarpTimer(long ticks, Identifier dim) {
        this.infinity$ticksUntilWarp = ticks;
        this.infinity$idForWarp = dim;
    }
}
