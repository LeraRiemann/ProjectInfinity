package net.lerariemann.infinity.compat.fabric;

import com.simibubi.create.content.trains.track.AllPortalTracks;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Pair;
import io.github.fabricators_of_create.porting_lib.entity.ITeleporter;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.util.WarpLogic;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import static com.simibubi.create.content.trains.track.AllPortalTracks.standardPortalProvider;

public class CreateFabricCompat {

    public static void register() {
        AllPortalTracks.registerIntegration(ModBlocks.PORTAL.get(), CreateFabricCompat::infinity);
    }

    public static Pair<ServerWorld, BlockFace> infinity(Pair<ServerWorld, BlockFace> inbound) {
        return standardPortalProvider(inbound, World.OVERWORLD, getInfinityWorld(inbound), CreateFabricCompat::getTeleporter);
    }

    private static ITeleporter getTeleporter(ServerWorld level) {
        return (ITeleporter)level.getPortalForcer();
    }

    public static RegistryKey<World> getInfinityWorld(Pair<ServerWorld, BlockFace> inbound) {
        ServerWorld world = inbound.getFirst();
        BlockFace blockFace = inbound.getSecond();
        BlockEntity blockEntity = world.getBlockEntity(blockFace.getConnectedPos());
        if (blockEntity instanceof InfinityPortalBlockEntity portalEntity && portalEntity.isOpen()) {
            Identifier id = portalEntity.getDimension();
            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, id);
            if (WarpLogic.dimExists(world.getServer().getWorld(key))) return key;
        }
        return null;
    }
}
