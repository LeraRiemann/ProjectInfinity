package net.lerariemann.infinity.util;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.var.ModPoi;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.Comparator;
import java.util.Optional;

public interface InfinityPortal {
    /** A portal should be open if and only if it has a valid destination. These functions are here to ensure it */
    static void tryUpdateOpenStatus(InfinityPortalBlockEntity npbe, ServerWorld worldFrom,
                                    MinecraftServer server, BlockPos pos) {
        tryUpdateOpenStatus(npbe, worldFrom, server.getWorld(
                RegistryKey.of(RegistryKeys.WORLD, npbe.getDimension())), pos);
    }
    static void tryUpdateOpenStatus(InfinityPortalBlockEntity npbe, ServerWorld worldFrom,
                                    ServerWorld worldTo, BlockPos pos) {
        if (!npbe.isOpen() ^ worldTo == null) {
            InfinityPortalCreation.modifyPortalRecursive(worldFrom, pos, e -> e.setOpen(!npbe.isOpen()));
        }
    }

    /** Root method for finding where to teleport stuff */
    static TeleportTarget createTeleportTarget(InfinityPortalBlockEntity ipbe,
                                     ServerWorld worldFrom, BlockPos posFrom, Entity entity) {
        Identifier id = ipbe.getDimension();
        RegistryKey<World> keyTo = RegistryKey.of(RegistryKeys.WORLD, id);
        ServerWorld worldTo = worldFrom.getServer().getWorld(keyTo);

        tryUpdateOpenStatus(ipbe, worldFrom, worldTo, posFrom);
        if (InfinityMethods.dimExists(worldTo)
                && ipbe.isOpen()
                && !worldTo.getRegistryKey().equals(worldFrom.getRegistryKey())) {
            BlockPos targetPos = ipbe.getOtherSidePos();
            if (isValidDestination(worldFrom, worldTo, targetPos))
                return getExistingTarget(worldTo, targetPos, entity);
            return findNewTeleportTarget(worldFrom, posFrom, worldTo, entity);
        }
        //below this point is error handling. note that not all such errors are bugs
        if (entity instanceof ServerPlayerEntity player) {
            sendErrors(player, worldFrom, worldTo, ipbe);
        }
        return emptyTarget(entity);
    }

    /** If teleportation failed for any reason, this sends the reason to the player. */
    static void sendErrors(ServerPlayerEntity player, ServerWorld worldFrom, ServerWorld worldTo, InfinityPortalBlockEntity ipbe) {
        if (worldTo != null) {
            if (worldTo.getRegistryKey().equals(worldFrom.getRegistryKey()))
                player.sendMessage(Text.translatable("error.infinity.portal.matching_ends"));
            else if (((Timebombable)worldTo).infinity$isTimebombed())
                player.sendMessage(Text.translatable("error.infinity.portal.deleted"));
            else InfinityMethods.sendUnexpectedError(player, "portal");
        }
        else if (!ipbe.isOpen())
            InfinityMod.provider.getPortalKeyAsItem().ifPresent(item -> player.sendMessage(
                    Text.translatable("error.infinity.portal.closed",
                            ((MutableText)item.getName()).formatted(Formatting.AQUA))));
        else player.sendMessage(Text.translatable("error.infinity.portal.null"));
    }

    static Direction.Axis getAxisOrDefault(BlockState state) {
        if (state.getProperties().contains(Properties.HORIZONTAL_AXIS))
            return state.get(Properties.HORIZONTAL_AXIS);
        return Direction.Axis.X;
    }

    /** If teleportation failed for any reason, this ensures the entity does not teleport anywhere. */
    static TeleportTarget emptyTarget(Entity entity) {
        return new TeleportTarget((ServerWorld)entity.getWorld(), entity.getPos(),
                entity.getVelocity(), entity.getYaw(), entity.getPitch(),
                TeleportTarget.NO_OP);
    }

    /** Teleporting to already recorded coordinates. */
    static TeleportTarget getExistingTarget(ServerWorld worldTo, BlockPos posTo, Entity teleportingEntity) {
        BlockState blockState = worldTo.getBlockState(posTo);
        BlockLocating.Rectangle rectangle = BlockLocating.getLargestRectangle(
                posTo, getAxisOrDefault(blockState),
                21, Direction.Axis.Y, 21,
                posx -> worldTo.getBlockState(posx) == blockState
        );
        return NetherPortalBlock.getExitPortalTarget(teleportingEntity, posTo, rectangle, worldTo,
                TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET.then(entityx -> entityx.addPortalChunkTicketAt(posTo)));
    }

    /** Filter for infinity portals of the correct destination. */
    static boolean isValidDestination(ServerWorld worldFrom, ServerWorld worldTo, BlockPos posTo) {
        if (posTo == null || !InfinityMethods.dimExists(worldTo)) return false;
        return (worldTo.getBlockEntity(posTo) instanceof InfinityPortalBlockEntity ipbe
                && ipbe.getDimension().toString().equals(worldFrom.getRegistryKey().getValue().toString()));
    }

    /** Finding (and recording) new coordinates to teleport to. */
    static TeleportTarget findNewTeleportTarget(ServerWorld worldFrom, BlockPos posFrom, ServerWorld worldTo, Entity teleportingEntity) {
        BlockLocating.Rectangle rectangleTo = findOrCreateExitPortal(worldFrom, posFrom, worldTo);
        if (rectangleTo == null) {
            if (teleportingEntity instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.translatable("error.infinity.portal.cannot_create"));
            }
            return emptyTarget(teleportingEntity);
        }

        BlockPos posTo = lowerCenterPos(rectangleTo, worldTo);
        trySyncPortals(worldFrom, posFrom, worldTo, posTo);
        return NetherPortalBlock.getExitPortalTarget(teleportingEntity, posFrom, rectangleTo, worldTo,
                TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET.then(TeleportTarget.ADD_PORTAL_CHUNK_TICKET));
    }

    /** Getting the central block of a portal rectangle; ensures CreateCompat works correctly. */
    static BlockPos lowerCenterPos(BlockLocating.Rectangle rect, World world) {
        return lowerCenterPos(rect, world.getBlockState(rect.lowerLeft).get(Properties.HORIZONTAL_AXIS));
    }
    static BlockPos lowerCenterPos(BlockLocating.Rectangle rect, Direction.Axis axis) {
        boolean bl = axis.equals(Direction.Axis.X);
        int i = rect.width / 2;
        return rect.lowerLeft.add(bl ? i : 0, 0, bl ? 0 : i);
    }

    /** Returns a rectangle of portal blocks to teleport to */
    static BlockLocating.Rectangle findOrCreateExitPortal(ServerWorld worldFrom, BlockPos posFrom, ServerWorld worldTo) {
        WorldBorder wb = worldTo.getWorldBorder();
        double d = DimensionType.getCoordinateScaleFactor(worldFrom.getDimension(), worldTo.getDimension());
        BlockPos originOfTesting = wb.clamp(posFrom.getX() * d, posFrom.getY(), posFrom.getZ() * d);

        Optional<BlockPos> optional = findNewExitPortalPosition(worldFrom, worldTo, wb, originOfTesting);
        BlockLocating.Rectangle rectangleTo;

        if (optional.isPresent()) { //we found a portal to hook up to
            BlockPos posTo = optional.get();
            BlockState blockState = worldTo.getBlockState(posTo);
            rectangleTo = BlockLocating.getLargestRectangle(
                    posTo,
                    getAxisOrDefault(blockState),
                    21, Direction.Axis.Y, 21,
                    posx -> worldTo.getBlockState(posx) == blockState
            );
        }
        else { //we found nothing and will create a new portal
            Direction.Axis axis = worldFrom.getBlockState(posFrom).getOrEmpty(NetherPortalBlock.AXIS).orElse(Direction.Axis.X);
            Optional<BlockLocating.Rectangle> optional2 = worldTo.getPortalForcer().createPortal(originOfTesting, axis);
            if (optional2.isEmpty()) {
                return null;
            }
            rectangleTo = optional2.get();
        }
        return rectangleTo;
    }

    /** Trying to scan for any valid portal block */
    static Optional<BlockPos> findNewExitPortalPosition(ServerWorld worldFrom, ServerWorld worldTo, WorldBorder wbTo,
                                                        BlockPos originOfTesting) {
        int radiusOfTesting = 128;
        PointOfInterestStorage poiStorage = worldTo.getPointOfInterestStorage();
        poiStorage.preloadChunks(worldTo, originOfTesting, radiusOfTesting);

        return poiStorage.getInSquare(poiType ->
                                poiType.matchesKey(PointOfInterestTypes.NETHER_PORTAL) || poiType.matchesKey(ModPoi.NEITHER_PORTAL_KEY),
                        originOfTesting, radiusOfTesting, PointOfInterestStorage.OccupationStatus.ANY)
                .map(PointOfInterest::getPos)
                .filter(wbTo::contains)
                .filter(pos -> isValidDestination(worldFrom, worldTo, pos))
                .min(Comparator.comparingDouble(posTo -> posTo.getSquaredDistance(originOfTesting)));
    }

    /** Establishing a mutual connection between two portals */
    static void trySyncPortals(ServerWorld worldFrom, BlockPos posFrom, ServerWorld worldTo, BlockPos posTo) {
        if (!(worldTo.getBlockState(posTo).getBlock() instanceof NetherPortalBlock)) return;

        InfinityPortalCreation.PortalModifierUnion otherSideModifier = new InfinityPortalCreation.PortalModifierUnion();
        Identifier idFrom = worldFrom.getRegistryKey().getValue();

        if (worldTo.getBlockEntity(posTo) instanceof InfinityPortalBlockEntity ipbe) {
            if (ipbe.getDimension() != idFrom) return;
            if (ipbe.isConnectedBothSides()) return; //don't resync what's already synced
        }
        else {
            otherSideModifier = InfinityPortalCreation.forInitialSetupping(worldTo, posTo, idFrom, true);
            //make it an infinity portal while you're at it
        }

        otherSideModifier.addModifier(ipbe1 -> ipbe1.setBlockPos(posFrom));
        InfinityPortalCreation.modifyPortalRecursive(worldFrom, posFrom, ipbe -> ipbe.setBlockPos(posTo));
        InfinityPortalCreation.modifyPortalRecursive(worldTo, posTo, otherSideModifier);
    }
}
