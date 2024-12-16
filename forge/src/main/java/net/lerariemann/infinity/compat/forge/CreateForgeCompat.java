package net.lerariemann.infinity.compat.forge;

import com.simibubi.create.content.trains.track.AllPortalTracks;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Pair;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.util.WarpLogic;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

import static com.simibubi.create.content.trains.track.AllPortalTracks.standardPortalProvider;
import static net.lerariemann.infinity.compat.CreateCompat.*;

public class CreateForgeCompat {

    public static void register() {
        AllPortalTracks.registerIntegration(ModBlocks.PORTAL.get(), CreateForgeCompat::infinity);
    }

    private static Pair<ServerWorld, BlockFace> infinity(Pair<ServerWorld, BlockFace> inbound) {
        return standardPortalProvider(inbound, World.OVERWORLD, getInfinityWorld(inbound), CreateForgeCompat::getTeleporter);
    }

    private static ITeleporter getTeleporter(ServerWorld level) {
        return level.getPortalForcer();
    }

}
