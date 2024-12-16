package net.lerariemann.infinity.compat;

import com.simibubi.create.content.trains.track.AllPortalTracks;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Pair;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Set;

public class CreateCompat {
    public static Pair<ServerWorld, BlockFace> infinityPortalProvider(Pair<ServerWorld, BlockFace> inbound) {
        ServerWorld worldFrom = inbound.getFirst();
        MinecraftServer server = worldFrom.getServer();
        if (!server.isNetherAllowed()) return null;
        BlockPos posFrom = inbound.getSecond().getConnectedPos();
        if (worldFrom.getBlockEntity(posFrom) instanceof InfinityPortalBlockEntity ipbe
                && ipbe.isConnectedBothSides()) { //we only allow trains through if portals are in sync
            ServerWorld worldTo = ipbe.getDimensionAsWorld();
            BlockPos posTo = ipbe.getOtherSidePos();
            assert posTo != null;
            Direction targetDirection = inbound.getSecond().getFace();
            Direction.Axis axisTo = worldTo.getBlockState(posTo).get(Properties.HORIZONTAL_AXIS);
            if (targetDirection.getAxis().equals(axisTo)) {
                targetDirection = targetDirection.rotateYClockwise();
            }
            return Pair.of(worldTo, new BlockFace(posTo.offset(targetDirection), targetDirection.getOpposite()));
        }
        return null;
    }

    public static void register() {
        AllPortalTracks.registerIntegration(ModBlocks.PORTAL.get(), CreateCompat::infinityPortalProvider);
    }

    public static void tryModifyRails(InfinityPortalBlockEntity ipbe) {
        World w = ipbe.getWorld();
        if (w instanceof ServerWorld worldFrom) {
            BlockPos posFrom = ipbe.getPos();
            BlockState state = worldFrom.getBlockState(posFrom);
            if (!state.isOf(ModBlocks.PORTAL.get())) return;
            Set<Direction> toCheck = state.get(Properties.HORIZONTAL_AXIS).equals(Direction.Axis.X) ?
                    Set.of(Direction.NORTH, Direction.SOUTH) : Set.of(Direction.EAST, Direction.WEST);
            for (Direction dir : toCheck) {
                BlockPos posTrack = posFrom.offset(dir);
                BlockState bs = worldFrom.getBlockState(posTrack);
                if (bs.getBlock() instanceof TrackBlock)
                    modifyRails(ipbe, worldFrom, posTrack, bs);
            }
        }
    }

    public static void modifyRails(InfinityPortalBlockEntity ipbe, ServerWorld worldFrom,
                                   BlockPos posTrack, BlockState bs) {
        if (!bs.contains(TrackBlock.SHAPE)) return;
        //ipbe.trySync();
        if (worldFrom.getBlockEntity(posTrack) instanceof TrackBlockEntity tbe) {
            ServerWorld worldTo = worldFrom.getServer().getWorld(tbe.boundLocation.getFirst());
            BlockPos posTo = tbe.boundLocation.getSecond();
            if (worldTo != null
                    && worldTo.getBlockState(posTo).getBlock() instanceof TrackBlock) {
                //worldTo.setBlockState(posTo, Blocks.AIR.getDefaultState());
            }
        }
        worldFrom.setBlockState(posTrack, Blocks.AIR.getDefaultState());
    }
}
