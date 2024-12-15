package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import net.lerariemann.infinity.util.PlatformMethods;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    protected BlockPos lastNetherPortalPosition;
    @Shadow
    private World world;

    @ModifyArg(method = "tickPortal()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"), index = 0)
    private RegistryKey<World> injected(RegistryKey<World> key) {
        return this.world.getRegistryKey() == World.OVERWORLD ? World.NETHER : World.OVERWORLD;
    }

    @ModifyArg(method = "tickPortal()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"), index = 0)
    private ServerWorld injected(ServerWorld serverWorld2) {
        ServerWorld serverWorld = (ServerWorld)this.world;
        if (serverWorld.getBlockState(this.lastNetherPortalPosition).isOf(ModBlocks.PORTAL.get())) {
            InfinityPortalBlockEntity e = ((InfinityPortalBlockEntity)serverWorld.getBlockEntity(this.lastNetherPortalPosition));
            if (e == null) return serverWorld;
            Identifier d = e.getDimension();
            serverWorld2 = serverWorld.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD,d));
            return (serverWorld2 != null && e.getOpen() && !((Timebombable) serverWorld2).infinity$isTimebombed()) ? serverWorld2 : serverWorld;
        }
        return (serverWorld2 != null) ? serverWorld2 : serverWorld;
    }
    
    @Inject(method="updateMovementInFluid", at = @At(value = "RETURN"), cancellable = true)
    void inj(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir) {
        if (tag.equals(FluidTags.WATER))
            if (PlatformMethods.acidTest((Entity)(Object)this, false))
                cir.setReturnValue(true);
    }

    @Inject(method = "isSubmergedIn", at = @At("RETURN"), cancellable = true)
    void inj(TagKey<Fluid> fluidTag, CallbackInfoReturnable<Boolean> cir) {
        if (fluidTag.equals(FluidTags.WATER))
            if (PlatformMethods.acidTest((Entity)(Object)this, true))
                cir.setReturnValue(true);
    }

    @Inject(method = "getFluidHeight", at = @At("RETURN"), cancellable = true)
    void inj2(TagKey<Fluid> fluid, CallbackInfoReturnable<Double> cir) {
        if (fluid.equals(FluidTags.WATER))
            cir.setReturnValue(Math.max(cir.getReturnValue(), PlatformMethods.acidHeightTest((Entity)(Object)this)));
    }
}

