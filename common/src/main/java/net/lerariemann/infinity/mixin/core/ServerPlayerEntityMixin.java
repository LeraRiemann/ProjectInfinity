package net.lerariemann.infinity.mixin.core;

import com.mojang.authlib.GameProfile;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.access.ServerPlayerEntityAccess;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.teleport.WarpLogic;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityAccess {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract ServerWorld getServerWorld();
    @Shadow public abstract boolean teleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch);
    @Shadow public abstract boolean damage(DamageSource source, float amount);
    @Shadow public abstract @Nullable Entity teleportTo(TeleportTarget teleportTarget);
    @Unique private long infinity$ticksUntilWarp;
    @Unique private Identifier infinity$idForWarp;

    @Inject(method="findRespawnPosition", at = @At("HEAD"), cancellable = true)
    private static void injected(ServerWorld world, BlockPos pos, float angle, boolean forced, boolean alive, CallbackInfoReturnable<Optional<Vec3d>> cir) {
        if (InfinityMethods.isTimebombed(world)) cir.setReturnValue(Optional.empty());
    }

    @Inject(method = "teleportTo",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getPlayerManager()Lnet/minecraft/server/PlayerManager;"))
    private void injected3(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        InfinityMethods.sendS2CPayload(player, ModPayloads.setShaderFromWorld(teleportTarget.world(), player));
        InfinityMethods.sendS2CPayload(player, ModPayloads.StarsRePayLoad.INSTANCE);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        /* Handle infinity options */
        InfinityOptions.access(getWorld()).effect.tryGiveEffect(player);
        /* Handle the warp command */
        if (--this.infinity$ticksUntilWarp == 0L)
            WarpLogic.performWarp(player, infinity$idForWarp);
        /* Handle effects from dimension deletion */
        ((Timebombable)getServerWorld()).tickTimebombProgress(player);
    }

    @Inject(method = "changeGameMode", at = @At("RETURN"))
    private void injected4(GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
            InfinityMethods.sendS2CPayload(player, ModPayloads.setShader(player));
        }
    }

    @Override
    public void infinity$setWarpTimer(long ticks, Identifier dim) {
        this.infinity$ticksUntilWarp = ticks;
        this.infinity$idForWarp = dim;
    }
}
