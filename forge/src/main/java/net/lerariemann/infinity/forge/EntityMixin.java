package net.lerariemann.infinity.forge;

import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    private World world;

    @Redirect(method = "tickPortal",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRegistryKey()Lnet/minecraft/registry/RegistryKey;"),
            slice = @Slice(from = @At(
                    value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getWorld()Lnet/minecraft/world/World;"
            ), to = @At("TAIL")))
    RegistryKey<World> smuggle(World w) {
       if (w == this.world) return World.NETHER;
       return w.getRegistryKey();
    }
}