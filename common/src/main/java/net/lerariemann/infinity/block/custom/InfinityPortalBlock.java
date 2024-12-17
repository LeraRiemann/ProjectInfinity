package net.lerariemann.infinity.block.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.item.ModItems;
import net.lerariemann.infinity.item.function.ModItemFunctions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.PortalCreationLogic;
import net.lerariemann.infinity.util.RandomProvider;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.util.WarpLogic;
import net.lerariemann.infinity.var.ModPoi;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.NetherPortal;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import net.minecraft.state.property.Properties;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class InfinityPortalBlock extends NetherPortalBlock implements BlockEntityProvider {
    private static final Random RANDOM = new Random();
    public static final BooleanProperty BOOP = BooleanProperty.of("boop");

    public InfinityPortalBlock(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(BOOP, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(BOOP);
    }

    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new InfinityPortalBlockEntity(pos, state, Math.abs(RANDOM.nextInt()));
    }

    /**
     * This is being called when the portal is right-clicked.
     */
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world instanceof ServerWorld serverWorld) {
            MinecraftServer s = world.getServer();
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof InfinityPortalBlockEntity npbe) {
                /* If the portal is open already, nothing should happen. */
                if (npbe.isOpen() && world_exists(s, npbe.getDimension()))
                    return ActionResult.SUCCESS;

                /* If the portal key is blank, open the portal on any right-click. */
                RandomProvider prov = InfinityMod.provider;
                Optional<Item> key = prov.getPortalKeyAsItem();
                if (key.isEmpty()) {
                    PortalCreationLogic.openWithStatIncrease(player, s, serverWorld, pos);
                }
                /* Otherwise check if we're using the correct key. If so, open. */
                else {
                    ItemStack usedKey = player.getStackInHand(Hand.MAIN_HAND);
                    if (usedKey.isOf(key.get())) {
                        if (!player.getAbilities().creativeMode && prov.rule("consumePortalKey")) {
                            usedKey.decrement(1); // Consume the key if needed
                        }
                        PortalCreationLogic.openWithStatIncrease(player, s, serverWorld, pos);
                    }
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof InfinityPortalBlockEntity ipbe) {
            ItemStack stack = ModItems.TRANSFINITE_KEY.get().getDefaultStack();
            NbtCompound compound = putKeyComponents(Items.AMETHYST_SHARD, ipbe.getDimension());
            stack.setNbt(compound);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    static boolean world_exists(MinecraftServer s, Identifier l) {
        return (!l.getNamespace().equals(InfinityMod.MOD_ID)) ||
                s.getSavePath(WorldSavePath.DATAPACKS).resolve(l.getPath()).toFile().exists() ||
                s.getWorld(RegistryKey.of(RegistryKeys.WORLD, l)) != null;
    }

    /**
     * Spawns colourful particles.
     */
    @Environment(EnvType.CLIENT) @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (random.nextInt(100) == 0) {
            world.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F, false);
        }

        for(int i = 0; i < 4; ++i) {
            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() + random.nextDouble();
            double f = (double)pos.getZ() + random.nextDouble();
            double g = ((double)random.nextFloat() - 0.5) * 0.5;
            double h = ((double)random.nextFloat() - 0.5) * 0.5;
            double j = ((double)random.nextFloat() - 0.5) * 0.5;
            int k = random.nextInt(2) * 2 - 1;
            if (!world.getBlockState(pos.west()).isOf(this) && !world.getBlockState(pos.east()).isOf(this)) {
                d = (double)pos.getX() + 0.5 + 0.25 * (double)k;
                g = random.nextFloat() * 2.0F * (float)k;
            } else {
                f = (double)pos.getZ() + 0.5 + 0.25 * (double)k;
                j = random.nextFloat() * 2.0F * (float)k;
            }

            ParticleEffect eff = ParticleTypes.PORTAL;
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof InfinityPortalBlockEntity npbe) {
                int colorInt = npbe.getPortalColor();
                Vec3d vec3d = Vec3d.unpackRgb(colorInt);
                double color = 1.0D + (colorInt >> 16 & 0xFF) / 255.0D;
                eff = new DustParticleEffect(new Vector3f((float)vec3d.x, (float)vec3d.y, (float)vec3d.z), (float)color);
            }

            world.addParticle(eff, d, e, f, g, h, j);
        }
    }

    public static NbtCompound putKeyComponents(Item item, Identifier dim) {
        NbtCompound nbtCompound = new NbtCompound();
        if (!item.equals(Items.AMETHYST_SHARD)) return nbtCompound;
        int keycolor = WarpLogic.getKeyColorFromId(dim);
        nbtCompound.putInt("key_color", keycolor);
        nbtCompound.putString("key_destination", dim.toString());
        return nbtCompound;
    }

    /**
     * Adds logic for portal-based recipes.
     */
    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entity) {
        AtomicBoolean bl = new AtomicBoolean(false);
        if (w instanceof ServerWorld world
                && world.getBlockEntity(pos) instanceof InfinityPortalBlockEntity npbe) {
            MinecraftServer server = world.getServer();
            if (entity instanceof ItemEntity e) {
                ModItemFunctions.checkCollisionRecipes(world, e, ModItemFunctions.PORTAL_CRAFTING_TYPE.get(),
                        putKeyComponents(e.getStack().getItem(), npbe.getDimension()));
                InfinityMod.provider.getPortalKeyAsItem().ifPresent(item -> {
                    if (e.getStack().isOf(item)) {
                        tryUpdateOpenStatus(npbe, world, server, pos);
                        if (npbe.isOpen()) return;
                        PlayerEntity nearestPlayer =
                                world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 5, false);
                        PortalCreationLogic.openWithStatIncrease(nearestPlayer, server, world, pos);
                        e.getStack().decrement(1);
                        e.setVelocity(e.getVelocity().multiply(-1));
                        e.setPortalCooldown(200);
                        bl.set(true);
                    }
                });
            }
            if (entity instanceof PlayerEntity player
                    && InfinityMod.provider.isPortalKeyBlank()) {
                ServerWorld world1 = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, npbe.getDimension()));
                if ((world1 == null) || !npbe.isOpen())
                    PortalCreationLogic.openWithStatIncrease(player, server, world, pos);
                else {
                    Timebombable tw = (Timebombable)world1;
                    if (tw.infinity$isTimebombed() && tw.infinity$tryRestore()) {
                        new RandomDimension(npbe.getDimension(), server);
                        PortalCreationLogic.openWithStatIncrease(player, server, world, pos);
                    }
                }
            }
        }
        if (!bl.get()) super.onEntityCollision(state, w, pos, entity);
    }

    /** A portal should be open if and only if it has a valid destination. These functions are here to ensure it */
    public static void tryUpdateOpenStatus(InfinityPortalBlockEntity npbe, ServerWorld worldFrom,
                                           MinecraftServer server, BlockPos pos) {
        tryUpdateOpenStatus(npbe, worldFrom, server.getWorld(
                RegistryKey.of(RegistryKeys.WORLD, npbe.getDimension())), pos);
    }
    public static void tryUpdateOpenStatus(InfinityPortalBlockEntity npbe, ServerWorld worldFrom,
            ServerWorld worldTo, BlockPos pos) {
        if (!npbe.isOpen() ^ worldTo == null) { //a portal should be open if and only if it has a valid destination
            PortalCreationLogic.modifyPortalRecursive(worldFrom, pos,
                    new PortalCreationLogic.PortalModifier(e -> e.setOpen(!npbe.isOpen())));
        }
    }

    /**
     * Spawns chaos pawns in the portal.
     */
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (world.getDimension().natural() && world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)
                && random.nextInt(2000) < world.getDifficulty().getId()) {
            ChaosPawn entity;
            while (world.getBlockState(pos).isOf(this)) {
                pos = pos.down();
            }
            if (world.getBlockState(pos).allowsSpawning(world, pos, ModEntities.CHAOS_PAWN.get()) &&
                    InfinityMethods.chaosMobsEnabled() &&
                    (entity = ModEntities.CHAOS_PAWN.get().spawn(world, pos.up(), SpawnReason.STRUCTURE)) != null) {
                entity.resetPortalCooldown();
                BlockEntity blockEntity = world.getBlockEntity(pos.up());
                if (blockEntity instanceof InfinityPortalBlockEntity npbe) {
                    int color = npbe.getPortalColor();
                    Vec3d c = Vec3d.unpackRgb(color);
                    entity.setAllColors((int)(256 * c.z) + 256 * (int)(256 * c.y) + 65536 * (int)(256 * c.x));
                }
            }
        }
    }

    /**
     * This is being called when anything is trying to use the portal to return the data on where it ends up.
     */
    public static TeleportTarget getTeleportTarget(Entity entity, InfinityPortalBlockEntity portal,
                                                   ServerWorld worldFrom, BlockPos posFrom) {
        BlockState blockStateFrom = worldFrom.getBlockState(posFrom);
        Direction.Axis axisFrom = blockStateFrom.get(Properties.HORIZONTAL_AXIS);
        BlockLocating.Rectangle portalFrom = BlockLocating.getLargestRectangle(
                posFrom, axisFrom,
                21, Direction.Axis.Y, 21,
                posx -> worldFrom.getBlockState(posx) == blockStateFrom
        );
        Vec3d offset = entity.positionInPortal(axisFrom, portalFrom);

        RegistryKey<World> keyTo = RegistryKey.of(RegistryKeys.WORLD, portal.getDimension()); //redirect teleportation to infdims
        ServerWorld worldTo = worldFrom.getServer().getWorld(keyTo);

        return getTeleportTarget(entity, portal, worldFrom, posFrom, worldTo, axisFrom, offset);
    }

    public static TeleportTarget getTeleportTarget(Entity entity, InfinityPortalBlockEntity portal,
                                                   ServerWorld worldFrom, BlockPos posFrom,
                                                   @Nullable ServerWorld worldTo, Direction.Axis axisFrom, Vec3d offset) {
        tryUpdateOpenStatus(portal, worldFrom, worldTo, posFrom);
        if (InfinityMethods.dimExists(worldTo)
                && portal.isOpen()
                && !worldTo.getRegistryKey().equals(worldFrom.getRegistryKey())) {
            BlockPos posTo = portal.getOtherSidePos();
            if (isValidDestinationStrong(worldFrom, worldTo, posTo)) {
                createTicket(worldTo, posTo);
                return getExistingTarget(worldTo, posTo, entity, axisFrom, offset);
            }
            return findNewTeleportTarget(worldFrom, posFrom, worldTo, entity, axisFrom, offset);
        }
        if (entity instanceof ServerPlayerEntity player) {
            sendErrors(player, worldFrom, worldTo, portal);
        }
        return emptyTarget(entity); //if something goes wrong, don't teleport anywhere
    }

    /**
     * If teleportation failed for any reason, this sends the reason to the player.
     */
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

    /**
     * Loads the other side of the portal for 300 ticks like nether portals do.
     */
    public static void createTicket(ServerWorld worldTo, BlockPos posTo) {
        worldTo.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(posTo), 3, posTo);
    }

    static Direction.Axis getAxisOrDefault(BlockState state) {
        return state.getOrEmpty(AXIS).orElse(Direction.Axis.X);
    }

    /**
     * If teleportation failed for any reason, this ensures the entity does not teleport anywhere.
     */
    public static TeleportTarget emptyTarget(Entity entity) {
        return new TeleportTarget(entity.getPos(),
                entity.getVelocity(), entity.getYaw(), entity.getPitch());
    }

    /**
     *  Teleporting to already recorded coordinates.
     */
    public static TeleportTarget getExistingTarget(ServerWorld worldTo, BlockPos posTo,
                                                   Entity teleportingEntity,
                                                   Direction.Axis axisFrom, Vec3d offset) {
        BlockState blockStateTo = worldTo.getBlockState(posTo);
        Direction.Axis axisTo = blockStateTo.get(Properties.HORIZONTAL_AXIS);
        BlockLocating.Rectangle portalTo = BlockLocating.getLargestRectangle(
                posTo, axisTo,
                21, Direction.Axis.Y, 21,
                posx -> worldTo.getBlockState(posx) == blockStateTo
        );
        return NetherPortal.getNetherTeleportTarget(worldTo, portalTo, axisFrom, offset,
                teleportingEntity, teleportingEntity.getVelocity(), teleportingEntity.getYaw(), teleportingEntity.getPitch());
    }

    /**
     * Filter for portal blocks except infinity portals of other destinations.
     */
    public static boolean isValidDestinationWeak(ServerWorld worldFrom, ServerWorld worldTo, BlockPos posTo) {
        if (posTo == null || !InfinityMethods.dimExists(worldTo)) return false;
        return (worldTo.getBlockState(posTo).isOf(Blocks.NETHER_PORTAL))
                || (worldTo.getBlockEntity(posTo) instanceof InfinityPortalBlockEntity ipbe
                && ipbe.getDimension().toString().equals(worldFrom.getRegistryKey().getValue().toString()));
    }

    /**
     * Filter for infinity portals of the correct destination.
     */
    public static boolean isValidDestinationStrong(ServerWorld worldFrom, ServerWorld worldTo, BlockPos posTo) {
        if (posTo == null || !InfinityMethods.dimExists(worldTo)) return false;
        return (worldTo.getBlockEntity(posTo) instanceof InfinityPortalBlockEntity ipbe
                && ipbe.getDimension().toString().equals(worldFrom.getRegistryKey().getValue().toString()));
    }

    public static TeleportTarget findNewTeleportTarget(ServerWorld worldFrom, BlockPos posFrom,
                                                       ServerWorld worldTo,
                                                       Entity teleportingEntity,
                                                       Direction.Axis axisFrom, Vec3d offset) {
        BlockLocating.Rectangle portalTo = findOrCreateExitPortal(worldFrom, posFrom, worldTo);
        if (portalTo == null) {
            if (teleportingEntity instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.translatable("error.infinity.portal.cannot_create"));
            }
            return emptyTarget(teleportingEntity);
        }

        BlockPos posTo = lowerCenterPos(portalTo, worldTo);
        createTicket(worldTo, posTo);
        trySyncPortals(worldFrom, posFrom, worldTo, posTo);

        return NetherPortal.getNetherTeleportTarget(worldTo, portalTo, axisFrom, offset,
                teleportingEntity, teleportingEntity.getVelocity(), teleportingEntity.getYaw(), teleportingEntity.getPitch());
    }

    public static BlockLocating.Rectangle findOrCreateExitPortal(ServerWorld worldFrom, BlockPos posFrom,
                                                  ServerWorld worldTo) {
        WorldBorder wb = worldTo.getWorldBorder();
        double d = DimensionType.getCoordinateScaleFactor(worldFrom.getDimension(), worldTo.getDimension());
        BlockPos originOfTesting = wb.clamp(posFrom.getX() * d, posFrom.getY(), posFrom.getZ() * d);

        Optional<BlockPos> optional = findNewExitPortalPosition(worldFrom, worldTo, wb, originOfTesting);
        BlockLocating.Rectangle portalTo;
        BlockPos posTo;

        if (optional.isPresent()) { //we found a portal to hook up to
            posTo = optional.get();
            BlockState blockState = worldTo.getBlockState(posTo);
            Direction.Axis axisTo = getAxisOrDefault(blockState);
            portalTo = BlockLocating.getLargestRectangle(
                    posTo, axisTo,
                    21, Direction.Axis.Y, 21,
                    posx -> worldTo.getBlockState(posx) == blockState
            );
        }
        else { //we found nothing and will create a new portal
            Direction.Axis axisTo = getAxisOrDefault(worldFrom.getBlockState(posFrom));
            Optional<BlockLocating.Rectangle> optional2 = worldTo.getPortalForcer().createPortal(originOfTesting, axisTo);
            if (optional2.isEmpty()) {
                return null;
            }
            portalTo = optional2.get();
        }
        return portalTo;
    }

    public static BlockPos lowerCenterPos(BlockLocating.Rectangle rect, World world) {
        return lowerCenterPos(rect, world.getBlockState(rect.lowerLeft).get(Properties.HORIZONTAL_AXIS));
    }
    static BlockPos lowerCenterPos(BlockLocating.Rectangle rect, Direction.Axis axis) {
        boolean bl = axis.equals(Direction.Axis.X);
        int i = rect.width / 2;
        return rect.lowerLeft.add(bl ? i : 0, 0, bl ? 0 : i);
    }

    static Optional<BlockPos> findNewExitPortalPosition(ServerWorld worldFrom, ServerWorld worldTo, WorldBorder wbTo,
                                                        BlockPos originOfTesting) {
        int radiusOfTesting = 128;
        PointOfInterestStorage poiStorage = worldTo.getPointOfInterestStorage();
        poiStorage.preloadChunks(worldTo, originOfTesting, radiusOfTesting);

        Set<BlockPos> allPortalsInRange = poiStorage.getInSquare(poiType ->
                                poiType.matchesKey(PointOfInterestTypes.NETHER_PORTAL)
                                        || poiType.matchesKey(ModPoi.NEITHER_PORTAL_KEY),
                        originOfTesting, radiusOfTesting, PointOfInterestStorage.OccupationStatus.ANY)
                .map(PointOfInterest::getPos)
                .filter(wbTo::contains)
                .collect(Collectors.toSet());

        Set<BlockPos> matchingPortalsInRange = allPortalsInRange
                .stream()
                .filter(pos -> isValidDestinationStrong(worldFrom, worldTo, pos))
                .collect(Collectors.toSet()); //try a stronger selector first (only infinity portals of matching dimension)

        if (matchingPortalsInRange.isEmpty())
            matchingPortalsInRange = allPortalsInRange
                    .stream()
                    .filter(pos -> isValidDestinationWeak(worldFrom, worldTo, pos))
                    .collect(Collectors.toSet()); //try a weaker selector (which also accepts nether portals)

        return matchingPortalsInRange.stream()
                .min(Comparator.comparingDouble(posTo -> posTo.getSquaredDistance(originOfTesting)));
    }

    public static boolean trySyncPortals(ServerWorld worldFrom, BlockPos posFrom, ServerWorld worldTo, BlockPos posTo) {
        if (!(worldTo.getBlockState(posTo).getBlock() instanceof NetherPortalBlock)) return false;

        PortalCreationLogic.PortalModifierUnion otherSideModifier = new PortalCreationLogic.PortalModifierUnion();
        Identifier idFrom = worldFrom.getRegistryKey().getValue();

        if (worldTo.getBlockEntity(posTo) instanceof InfinityPortalBlockEntity ipbe) {
            if (ipbe.getDimension() != idFrom) return;
            if (ipbe.isConnectedBothSides()) return; //don't resync what's already synced
        }
        else {
            otherSideModifier = PortalCreationLogic.forInitialSetupping(worldTo, posTo, idFrom, true); //make it an infinity portal while you're at it
        }

        otherSideModifier.addModifier(ipbe1 -> ipbe1.setBlockPos(posFrom));
        PortalCreationLogic.modifyPortalRecursive(worldFrom, posFrom,
                new PortalCreationLogic.PortalModifier(ipbe -> ipbe.setBlockPos(posTo)));
        PortalCreationLogic.modifyPortalRecursive(worldTo, posTo, otherSideModifier);
        return true;
    }
}
