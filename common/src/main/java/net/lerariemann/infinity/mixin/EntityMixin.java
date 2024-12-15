package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.block.custom.InfinityPortalBlock;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.PortalCreationLogic;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import net.lerariemann.infinity.util.PlatformMethods;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    protected BlockPos lastNetherPortalPosition;

    @Shadow public abstract World getWorld();

    @ModifyArg(method = "tickPortal()V", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"))
    private ServerWorld injected(ServerWorld originalWorldTo) {
        if (getWorld() instanceof ServerWorld worldFrom) {
            BlockPos pos = this.lastNetherPortalPosition;
            RegistryKey<World> keyTo;
            if (worldFrom.getBlockEntity(pos) instanceof InfinityPortalBlockEntity portal) {
                keyTo = RegistryKey.of(RegistryKeys.WORLD, portal.getDimension()); //redirect teleportation to infdims
            }
            else if (worldFrom.getBlockState(pos).isOf(Blocks.NETHER_PORTAL)
                    && InfinityMethods.isInfinity(worldFrom)) { //redirect returning from infdims and make portals in them infinity (for sync reasons)
                keyTo = World.OVERWORLD;
                PortalCreationLogic.modifyPortalRecursive(worldFrom, pos, keyTo.getValue(), true);
            }
            else return originalWorldTo;
            return Objects.requireNonNullElse(worldFrom.getServer().getWorld(keyTo), worldFrom);
        }
        return originalWorldTo;
    }

    @Inject(method = "getTeleportTarget", at = @At(value = "HEAD"), cancellable = true)
    private void injected(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir) {
        if (getWorld() instanceof ServerWorld worldFrom) {
            BlockPos posFrom = lastNetherPortalPosition;
            if (worldFrom.getBlockEntity(posFrom) instanceof InfinityPortalBlockEntity ipbe) {
                cir.setReturnValue(InfinityPortalBlock.getTeleportTarget((Entity)(Object)this, ipbe,
                        worldFrom, posFrom, destination));
            }
        }
    }

    /* Forge-specific mixins to allow mobs to swim in iridescence */
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

