package net.lerariemann.infinity.compat;

import com.simibubi.create.api.contraption.train.PortalTrackProvider;
import com.simibubi.create.content.trains.track.AllPortalTracks;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackShape;
import net.createmod.catnip.math.BlockFace;
import net.lerariemann.infinity.block.custom.RailHelper;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.Set;

public class CreateCompat {
    private static PortalTrackProvider.Exit infinityPortalProvider(ServerWorld worldFrom, BlockFace blockFace) {
        MinecraftServer server = worldFrom.getServer();
//        if (!server.isNetherAllowed()) return null;
        BlockPos posFrom = blockFace.getConnectedPos();
        if (worldFrom.getBlockEntity(posFrom) instanceof InfinityPortalBlockEntity ipbe
                && ipbe.isConnectedBothSides()) { //we only allow trains through if portals are in sync
            ServerWorld worldTo = ipbe.getDimensionAsWorld();
            BlockPos posTo = ipbe.getOtherSidePos();
            assert posTo != null;
            Direction targetDirection = blockFace.getFace();
            Direction.Axis axisTo = worldTo.getBlockState(posTo).get(Properties.HORIZONTAL_AXIS);
            if (targetDirection.getAxis().equals(axisTo)) {
                targetDirection = targetDirection.rotateYClockwise();
            }
            return new PortalTrackProvider.Exit(worldTo, new BlockFace(posTo.offset(targetDirection), targetDirection.getOpposite()));
        }
        return null;
    }

    public static void register() {
        AllPortalTracks.tryRegisterIntegration(ModBlocks.PORTAL.get().arch$registryName(), CreateCompat::infinityPortalProvider);
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
        if (ipbe.isConnectedBothSides()) {
            if (!bs.contains(TrackBlock.SHAPE)) return;
            worldFrom.setBlockState(posTrack, ModBlocks.RAIL_HELPER.get().getDefaultState());
            RailHelper.RHBEntity e = RailHelper.getBlockEntity(worldFrom, posTrack);
            e.trackBlock = Registries.BLOCK.getId(bs.getBlock());
            e.shape = switch (bs.get(TrackBlock.SHAPE)) {
                case TN, TS -> "zo";
                default -> "xo";
            };
        }
        worldFrom.setBlockState(posTrack, Blocks.AIR.getDefaultState());
    }
    public static void reattachRails(ServerWorld world, BlockPos pos, RailHelper.RHBEntity e) {
        BlockState bs;
        Optional<Block> b = Registries.BLOCK.getOrEmpty(e.trackBlock);
        if (b.isEmpty() || !(b.get() instanceof TrackBlock)) bs = Blocks.AIR.getDefaultState();
        else bs = b.get().getDefaultState().with(TrackBlock.SHAPE, e.shape.equals("zo") ? TrackShape.ZO : TrackShape.XO);
        world.setBlockState(pos, bs);
    }
}