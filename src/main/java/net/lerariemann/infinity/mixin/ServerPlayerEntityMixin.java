package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.access.NetherPortalBlockAccess;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setWorld(Lnet/minecraft/server/world/ServerWorld;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injected2(ServerWorld destination, CallbackInfoReturnable<Entity> ci, ServerWorld serverWorld, RegistryKey<World> registryKey,
                           WorldProperties worldProperties, PlayerManager playerManager, TeleportTarget teleportTarget) {
        if (((MinecraftServerAccess)(serverWorld.getServer())).getDimensionProvider().gameRules.get("returnPortalsEnabled") &&
                (registryKey.getValue().getNamespace().equals(InfinityMod.MOD_ID))) {
            BlockPos pos = new BlockPos(teleportTarget.position);
            if (destination.getBlockState(pos).isOf(Blocks.NETHER_PORTAL)) {
                String keystr = registryKey.getValue().getPath();
                String is = keystr.substring(keystr.lastIndexOf("_") + 1);
                int i = Integer.parseInt(is);
                ((NetherPortalBlockAccess)Blocks.NETHER_PORTAL).modifyPortal(destination, pos, destination.getBlockState(pos), i);
            }
        }
    }
}
