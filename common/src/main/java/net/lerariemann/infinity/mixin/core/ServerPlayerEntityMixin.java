package net.lerariemann.infinity.mixin.core;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.access.ServerPlayerEntityAccess;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.teleport.WarpLogic;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityAccess {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract ServerWorld getServerWorld();
    @Shadow public abstract boolean teleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch);
    @Shadow public abstract boolean damage(DamageSource source, float amount);

    @Shadow public ServerPlayNetworkHandler networkHandler;
    @Shadow private int syncedExperience;
    @Shadow private float syncedHealth;
    @Shadow private int syncedFoodLevel;
    @Unique private long infinity$ticksUntilWarp;
    @Unique private Identifier infinity$idForWarp;

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
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        if (cir.getReturnValue())
            ModPayloads.sendReloadPacket(player, player.getServerWorld());
    }

    @Inject(method= "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V", at = @At(value="INVOKE", target ="Lnet/minecraft/server/PlayerManager;sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void injected5(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        ModPayloads.sendReloadPacket(player, targetWorld);
        ServerPlayNetworking.send(player, ModPayloads.STARS_RELOAD, PlatformMethods.createPacketByteBufs());
        this.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(getAbilities()));
        for(StatusEffectInstance effect: getStatusEffects())
            networkHandler.sendPacket(new EntityStatusEffectS2CPacket(getId(), effect));
        syncedExperience = -1;
        syncedHealth = -1.0F;
        syncedFoodLevel = -1;
    }

    @Override
    public void infinity$setWarpTimer(long ticks, Identifier dim) {
        this.infinity$ticksUntilWarp = ticks;
        this.infinity$idForWarp = dim;
    }
}