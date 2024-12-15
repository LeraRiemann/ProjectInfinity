package net.lerariemann.infinity.mixin.fabric;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.trains.track.AllPortalTracks;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Pair;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.InfinityPortalBlock;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(AllPortalTracks.class)
public abstract class TrackBlockMixin {

    @Redirect(method = "standardPortalProvider",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"))
    private static ServerWorld injected(MinecraftServer instance, RegistryKey<World> key, @Local Pair<ServerWorld, BlockFace> portalPos) {
        ServerWorld level = portalPos.getFirst();
        BlockFace face = portalPos.getSecond();
        RegistryKey<World> true_key = key;
        BlockState portalState = level.getBlockState(face.getConnectedPos());
        InfinityMod.LOGGER.info(portalState.getBlock().toString());
        if (portalState.getBlock() instanceof InfinityPortalBlock) {
            BlockEntity blockEntity = level.getBlockEntity(face.getConnectedPos());
            if (blockEntity instanceof InfinityPortalBlockEntity portalBlockEntity) {
                Identifier id = portalBlockEntity.getDimension();
                true_key = RegistryKey.of(RegistryKeys.WORLD, id);
            }
        }
        else {
            true_key = level.getRegistryKey() == World.OVERWORLD ? World.NETHER : World.OVERWORLD;
        }
        LogManager.getLogger().info(true_key);
        return instance.getWorld(true_key);
    }
}
