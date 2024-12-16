package net.lerariemann.infinity.compat.fabric;

import com.simibubi.create.content.trains.track.AllPortalTracks;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Pair;
import io.github.fabricators_of_create.porting_lib.entity.ITeleporter;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.compat.CreateCompat;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import static com.simibubi.create.content.trains.track.AllPortalTracks.standardPortalProvider;

public class CreateFabricCompat {

    public static void register() {
        AllPortalTracks.registerIntegration(ModBlocks.PORTAL.get(), CreateFabricCompat::infinity);
    }

    private static Pair<ServerWorld, BlockFace> infinity(Pair<ServerWorld, BlockFace> inbound) {
        return standardPortalProvider(inbound, World.OVERWORLD, CreateCompat.getInfinityWorld(inbound), CreateFabricCompat::getTeleporter);
    }

    private static ITeleporter getTeleporter(ServerWorld level) {
        return (ITeleporter)level.getPortalForcer();
    }
}
