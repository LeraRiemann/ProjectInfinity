package net.lerariemann.infinity.util;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.var.ModPoi;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.NetherPortal;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;

public class InfinityPortal {
    InfinityPortalBlockEntity ipbe;
    ServerWorld worldFrom;
    BlockPos posFrom;
    Direction.Axis axisFrom;
    BlockLocating.Rectangle portalFrom;
    @Nullable ServerWorld worldTo;
    @Nullable BlockPos posTo;
    @Nullable BlockLocating.Rectangle portalTo;
    boolean unableToCreatePortalFlag = false;
    boolean noSyncFlag = false;

    public InfinityPortal(InfinityPortalBlockEntity ipbe, ServerWorld worldFrom, BlockPos startingPos) {
        this.ipbe = ipbe;
        this.worldFrom = worldFrom;
        portalFrom = getRect(worldFrom, startingPos);
        posFrom = lowerCenterPos(portalFrom, worldFrom);
        axisFrom = worldFrom.getBlockState(posFrom).get(Properties.HORIZONTAL_AXIS);
        worldTo = worldFrom.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, ipbe.getDimension()));
        tryUpdateOpenStatus(ipbe, worldFrom, startingPos, worldTo);

        if (portalShouldWork()) {
            BlockPos targetPos = ipbe.getOtherSidePos();
            if (isValidDestination(worldFrom, worldTo, targetPos)) {
                posTo = targetPos;
                scanExistingTarget();
            }
            scanNewTeleportTarget();
        }
    }

    /** A portal should be marked as "open" if and only if it has a non-null destination dimension. These functions are here to ensure it */
    public static void tryUpdateOpenStatus(InfinityPortalBlockEntity ipbe, ServerWorld worldFrom, BlockPos posFrom,
                                           MinecraftServer server) {
        ServerWorld worldTo = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, ipbe.getDimension()));
        tryUpdateOpenStatus(ipbe, worldFrom, posFrom, worldTo);
    }
    public static void tryUpdateOpenStatus(InfinityPortalBlockEntity ipbe, ServerWorld worldFrom, BlockPos posFrom,
                                           ServerWorld worldTo) {
        if (!ipbe.isOpen() ^ worldTo == null) {
            PortalCreator.modifyPortalRecursive(worldFrom, posFrom, e -> e.setOpen(!ipbe.isOpen()));
        }
    }

    /** Finding a rectangle of portal blocks provided a position of one of them. */
    public static BlockLocating.Rectangle getRect(World world, BlockPos pos) {
        BlockState blockStateFrom = world.getBlockState(pos);
        return BlockLocating.getLargestRectangle(
                pos, getAxisOrDefault(blockStateFrom),
                21, Direction.Axis.Y, 21,
                posx -> world.getBlockState(posx) == blockStateFrom
        );
    }

    /** Getting the central block of a portal rectangle; setting posTo to this ensures CreateCompat works correctly. */
    static BlockPos lowerCenterPos(BlockLocating.Rectangle rect, World world) {
        return lowerCenterPos(rect, world.getBlockState(rect.lowerLeft).get(Properties.HORIZONTAL_AXIS));
    }
    static BlockPos lowerCenterPos(BlockLocating.Rectangle rect, Direction.Axis axis) {
        boolean bl = axis.equals(Direction.Axis.X);
        int i = rect.width / 2;
        return rect.lowerLeft.add(bl ? i : 0, 0, bl ? 0 : i);
    }

    /** Finding where to teleport stuff. The constructor ensures all scanning is already done by this point */
    public TeleportTarget getTeleportTarget(Entity entity) {
        if (portalShouldWork() && portalTo != null && worldTo != null && posTo != null) {
            createTicket(worldTo, posTo);
            return NetherPortal.getNetherTeleportTarget(worldTo, portalTo, axisFrom, entity.positionInPortal(axisFrom, portalFrom),
                    entity, entity.getVelocity(), entity.getYaw(), entity.getPitch());
        }
        //below this point is error handling. note that not all such errors are bugs
        if (entity instanceof ServerPlayerEntity player) {
            sendErrors(player);
        }
        return emptyTarget(entity);
    }

    public boolean portalShouldWork() {
        return (InfinityMethods.dimExists(worldTo) //dimension exists and is not timebombed
                && ipbe.isOpen() //the portal is not closed
                && !worldTo.getRegistryKey().equals(worldFrom.getRegistryKey())); //the portal does not lead back to its own dimension
    }

    /** If teleportation failed for any reason, this sends the reason to the player. */
    public void sendErrors(ServerPlayerEntity player) {
        if (worldTo != null) {
            if (worldTo.getRegistryKey().equals(worldFrom.getRegistryKey()))
                player.sendMessage(Text.translatable("error.infinity.portal.matching_ends"));
            else if (InfinityMethods.isTimebombed(worldTo))
                player.sendMessage(Text.translatable("error.infinity.portal.deleted"));
            else if (unableToCreatePortalFlag)
                player.sendMessage(Text.translatable("error.infinity.portal.cannot_create"));
            else InfinityMethods.sendUnexpectedError(player, "portal");
        }
        else if (!ipbe.isOpen())
            InfinityMod.provider.getPortalKeyAsItem().ifPresent(item -> player.sendMessage(
                    Text.translatable("error.infinity.portal.closed",
                            ((MutableText)item.getName()).formatted(Formatting.AQUA))));
        else player.sendMessage(Text.translatable("error.infinity.portal.null"));
    }

    /**
     * Loads the other side of the portal for 300 ticks like nether portals do.
     */
    public static void createTicket(ServerWorld worldTo, BlockPos posTo) {
        worldTo.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(posTo), 3, posTo);
    }

    public static Direction.Axis getAxisOrDefault(BlockState state) {
        if (state.getProperties().contains(Properties.HORIZONTAL_AXIS))
            return state.get(Properties.HORIZONTAL_AXIS);
        return Direction.Axis.X;
    }

    /** If teleportation failed for any reason, this ensures the entity does not teleport anywhere. */
    public static TeleportTarget emptyTarget(Entity entity) {
        return new TeleportTarget(entity.getPos(),
                entity.getVelocity(), entity.getYaw(), entity.getPitch());
    }

    /** Filter for infinity portals of the correct destination. */
    public static boolean isValidDestination(ServerWorld worldFrom, ServerWorld worldTo, BlockPos posTo) {
        if (posTo == null || !InfinityMethods.dimExists(worldTo)) return false;
        return (worldTo.getBlockEntity(posTo) instanceof InfinityPortalBlockEntity ipbeTo
                && ipbeTo.getDimension().toString().equals(worldFrom.getRegistryKey().getValue().toString()));
    }

    /** Filling in the blanks asserting the portal is correctly synced */
    private void scanExistingTarget() {
        assert worldTo != null && posTo != null;
        BlockState blockState = worldTo.getBlockState(posTo);
        portalTo = BlockLocating.getLargestRectangle(
                posTo, getAxisOrDefault(blockState),
                21, Direction.Axis.Y, 21,
                posx -> worldTo.getBlockState(posx) == blockState
        );
    }

    /** If the portal is not correctly synced, this resyncs it, possibly creating a new exit portal */
    private void scanNewTeleportTarget() {
        assert worldTo != null;
        findOrCreateExitPortal();
        if (portalTo == null) return;
        posTo = lowerCenterPos(portalTo, worldTo);
        if (!noSyncFlag) trySyncPortals(worldFrom, posFrom, worldTo, posTo);
    }

    /** Searches for a rectangle of portal blocks to teleport to */
    private void findOrCreateExitPortal() {
        assert worldTo != null;
        WorldBorder wb = worldTo.getWorldBorder();
        double d = DimensionType.getCoordinateScaleFactor(worldFrom.getDimension(), worldTo.getDimension());
        BlockPos originOfTesting = wb.clamp(posFrom.getX() * d, posFrom.getY(), posFrom.getZ() * d);

        Optional<BlockPos> optional = findNewExitPortalPosition(wb, originOfTesting);

        if (optional.isPresent())
            portalTo = getRect(worldTo, optional.get());
        else { //we found nothing and will create a new portal
            Direction.Axis axis = getAxisOrDefault(worldFrom.getBlockState(posFrom));
            worldTo.getPortalForcer().createPortal(originOfTesting, axis)
                    .ifPresentOrElse(rect -> portalTo = rect, () -> unableToCreatePortalFlag = true);
        }
    }

    /** Trying to scan for any valid portal block. */
    private Optional<BlockPos> findNewExitPortalPosition(WorldBorder wbTo, BlockPos originOfTesting) {
        assert worldTo != null;
        int radiusOfTesting = 128;
        PointOfInterestStorage poiStorage = worldTo.getPointOfInterestStorage();
        poiStorage.preloadChunks(worldTo, originOfTesting, radiusOfTesting);

        //First scan for valid infinity portals
        Optional<BlockPos> portal = poiStorage.getInSquare(poiType ->
                                poiType.matchesKey(ModPoi.NEITHER_PORTAL_KEY),
                        originOfTesting, radiusOfTesting, PointOfInterestStorage.OccupationStatus.ANY)
                .map(PointOfInterest::getPos)
                .filter(wbTo::contains)
                .filter(pos -> isValidDestination(worldFrom, worldTo, pos))
                .min(Comparator.comparingDouble(posTo -> posTo.getSquaredDistance(originOfTesting)));
        if (portal.isPresent()) return portal;

        //If one wasn't found, find a nether portal instead and ensure it will not be overwritten
        portal = poiStorage.getInSquare(poiType ->
                                poiType.matchesKey(PointOfInterestTypes.NETHER_PORTAL),
                        originOfTesting, radiusOfTesting, PointOfInterestStorage.OccupationStatus.ANY)
                .map(PointOfInterest::getPos)
                .filter(wbTo::contains)
                .min(Comparator.comparingDouble(posTo -> posTo.getSquaredDistance(originOfTesting)));
        noSyncFlag = portal.isPresent(); //if a nether portal indeed was found we do not wish to overwrite it

        return portal;
    }

    /** Establishing a mutual connection between two portals */
    public static void trySyncPortals(ServerWorld worldFrom, BlockPos posFrom,
                                      ServerWorld worldTo, BlockPos posTo) {
        if (worldTo == null || posTo == null
                || !(worldTo.getBlockState(posTo).getBlock() instanceof NetherPortalBlock)) return;

        PortalCreator.PortalModifierUnion otherSideModifier = new PortalCreator.PortalModifierUnion();
        Identifier idFrom = worldFrom.getRegistryKey().getValue();

        if (worldTo.getBlockEntity(posTo) instanceof InfinityPortalBlockEntity ipbeTo) {
            if (!ipbeTo.getDimension().toString().equals(idFrom.toString())) return;
            if (ipbeTo.isConnectedBothSides()) return; //don't resync what's already synced
        }
        else {
            otherSideModifier = PortalCreator.forInitialSetupping(worldTo, posTo, idFrom, true);
            //make it an infinity portal while you're at it
        }

        otherSideModifier.addModifier(ipbe1 -> ipbe1.setBlockPos(posFrom));
        PortalCreator.modifyPortalRecursive(worldFrom, posFrom, ipbe -> ipbe.setBlockPos(posTo));
        PortalCreator.modifyPortalRecursive(worldTo, posTo, otherSideModifier);
    }
}
