package net.lerariemann.infinity.block.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.item.function.ModItemFunctions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.PortalCreationLogic;
import net.lerariemann.infinity.util.RandomProvider;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.item.ModItems;
import net.lerariemann.infinity.var.ModPoi;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
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
    public ActionResult onUse(BlockState state, World w, BlockPos pos,
                              PlayerEntity player, BlockHitResult hit) {
        if (w instanceof ServerWorld world) {
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
                    PortalCreationLogic.openWithStatIncrease(player, s, world, pos);
                }

                /* Otherwise check if we're using the correct key. If so, open. */
                else {
                    ItemStack usedKey = player.getStackInHand(Hand.MAIN_HAND);
                    if (usedKey.isOf(key.get())) {
                        if (!player.getAbilities().creativeMode && prov.rule("consumePortalKey")) {
                            usedKey.decrement(1); // Consume the key if needed
                        }
                        PortalCreationLogic.openWithStatIncrease(player, s, world, pos);
                    }
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof InfinityPortalBlockEntity ipbe) {
            ItemStack stack = ModItems.TRANSFINITE_KEY.get().getDefaultStack();
            stack.applyComponentsFrom(getKeyComponents(ipbe.getDimension()));
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

    public static Optional<ComponentMap> getKeyComponents(Item item, Identifier dim) {
        if (!item.equals(ModItems.TRANSFINITE_KEY.get())) return Optional.empty();
        return Optional.of(getKeyComponents(dim));
    }

    public static ComponentMap getKeyComponents(Identifier dim) {
        Integer keycolor = InfinityMethods.getKeyColorFromId(dim);
        return (ComponentMap.builder()
                .add(ModItemFunctions.KEY_DESTINATION.get(), dim)
                .add(ModItemFunctions.COLOR.get(), keycolor)).build();
    }

    /**
     * Adds logic for portal-based recipes.
     */
    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entity) {
        if (w instanceof ServerWorld world
                && world.getBlockEntity(pos) instanceof InfinityPortalBlockEntity npbe) {
            MinecraftServer server = world.getServer();
            if (entity instanceof ItemEntity e)
                ModItemFunctions.checkCollisionRecipes(world, e, ModItemFunctions.PORTAL_CRAFTING_TYPE.get(),
                    item -> getKeyComponents(item, npbe.getDimension()));
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
        super.onEntityCollision(state, w, pos, entity);
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
     * This is called when something's trying to use the portal and returns the data on where they end up.
     */
    @Nullable @Override
    public TeleportTarget createTeleportTarget(ServerWorld worldFrom, Entity entity, BlockPos posFrom) {
        if (worldFrom.getBlockEntity(posFrom) instanceof InfinityPortalBlockEntity ipbe) {
            Identifier id = ipbe.getDimension();
            RegistryKey<World> keyTo = RegistryKey.of(RegistryKeys.WORLD, id);
            ServerWorld worldTo = worldFrom.getServer().getWorld(keyTo);

            if (InfinityMethods.dimExists(worldTo) && ipbe.isOpen()) {
                BlockPos targetPos = ipbe.getOtherSidePos();
                if (isValidDestinationStrong(worldFrom, worldTo, targetPos))
                    return getExistingTarget(worldTo, targetPos, entity);
                return findNewTeleportTarget(worldFrom, posFrom, worldTo, entity);
            }

            //below this point is error handling. note that not all such errors are bugs
            if (worldTo == null && ipbe.isOpen()) { //an open portal with no destination? close that
                PortalCreationLogic.modifyPortalRecursive(worldFrom, posFrom,
                        new PortalCreationLogic.PortalModifier(e -> e.setOpen(false)));
            }
            if (entity instanceof ServerPlayerEntity player) {
                sendErrors(player, worldTo, ipbe);
            }
        }
        else if (entity instanceof ServerPlayerEntity player)
            InfinityMethods.sendUnexpectedError(player, "portal");
        return emptyTarget(entity); //if anything goes wrong, don't teleport anywhere
    }

    static void sendErrors(ServerPlayerEntity player, ServerWorld worldTo, InfinityPortalBlockEntity ipbe) {
        if (worldTo != null && ((Timebombable)worldTo).infinity$isTimebombed())
            player.sendMessage(Text.translatable("error.infinity.portal.deleted"));
        else if (!ipbe.isOpen())
            InfinityMod.provider.getPortalKeyAsItem().ifPresent(item -> player.sendMessage(
                    Text.translatable("error.infinity.portal.closed",
                            ((MutableText)item.getName()).formatted(Formatting.AQUA))));
        else if (worldTo == null)
            player.sendMessage(Text.translatable("error.infinity.portal.null"));
        else InfinityMethods.sendUnexpectedError(player, "portal");
    }

    static Direction.Axis getAxisOrDefault(BlockState state) {
        if (state.getProperties().contains(Properties.HORIZONTAL_AXIS))
            return state.get(Properties.HORIZONTAL_AXIS);
        return Direction.Axis.X;
    }

    public static TeleportTarget emptyTarget(Entity entity) {
        return new TeleportTarget((ServerWorld)entity.getWorld(), entity.getPos(),
                entity.getVelocity(), entity.getYaw(), entity.getPitch(),
                TeleportTarget.NO_OP);
    }

    /**
     * Teleporting to already recorded coordinates.
     */
    public static TeleportTarget getExistingTarget(ServerWorld worldTo, BlockPos posTo, Entity teleportingEntity) {
        BlockState blockState = worldTo.getBlockState(posTo);
        BlockLocating.Rectangle rectangle = BlockLocating.getLargestRectangle(
                posTo, getAxisOrDefault(blockState),
                21, Direction.Axis.Y, 21,
                posx -> worldTo.getBlockState(posx) == blockState
        );
        return NetherPortalBlock.getExitPortalTarget(teleportingEntity, posTo, rectangle, worldTo,
                TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET.then(entityx -> entityx.addPortalChunkTicketAt(posTo)));
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

    public static TeleportTarget findNewTeleportTarget(ServerWorld worldFrom, BlockPos posFrom, ServerWorld worldTo, Entity teleportingEntity) {
        WorldBorder wb = worldTo.getWorldBorder();
        double d = DimensionType.getCoordinateScaleFactor(worldFrom.getDimension(), worldTo.getDimension());
        BlockPos originOfTesting = wb.clamp(posFrom.getX() * d, posFrom.getY(), posFrom.getZ() * d);
        int radiusOfTesting = 128;

        PointOfInterestStorage poiStorage = worldTo.getPointOfInterestStorage();
        poiStorage.preloadChunks(worldTo, originOfTesting, radiusOfTesting);
        Set<BlockPos> allPortalsInRange = poiStorage.getInSquare(poiType ->
                poiType.matchesKey(PointOfInterestTypes.NETHER_PORTAL) || poiType.matchesKey(ModPoi.NEITHER_PORTAL_KEY),
                originOfTesting, radiusOfTesting, PointOfInterestStorage.OccupationStatus.ANY)
                .map(PointOfInterest::getPos)
                .filter(wb::contains)
                .collect(Collectors.toSet());
        Set<BlockPos> matchingPortalsInRange = allPortalsInRange
                .stream()
                .filter(pos -> isValidDestinationStrong(worldFrom, worldTo, pos))
                .collect(Collectors.toSet());
        if (matchingPortalsInRange.isEmpty())
            matchingPortalsInRange = allPortalsInRange
                    .stream()
                    .filter(pos -> isValidDestinationWeak(worldFrom, worldTo, pos))
                    .collect(Collectors.toSet());

        Optional<BlockPos> optional = matchingPortalsInRange.stream()
                .min(Comparator.comparingDouble(posTo -> posTo.getSquaredDistance(originOfTesting)));
        BlockLocating.Rectangle rectangleTo;
        TeleportTarget.PostDimensionTransition postDimensionTransition;
        BlockPos posTo;

        if (optional.isPresent()) {
            posTo = optional.get();
            BlockState blockState = worldTo.getBlockState(posTo);
            rectangleTo = BlockLocating.getLargestRectangle(
                    posTo,
                    getAxisOrDefault(blockState),
                    21, Direction.Axis.Y, 21,
                    posx -> worldTo.getBlockState(posx) == blockState
            );
            postDimensionTransition = TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET
                    .then(entityx -> entityx.addPortalChunkTicketAt(posTo));
        }

        else {
            Direction.Axis axis = worldFrom.getBlockState(posFrom).getOrEmpty(AXIS).orElse(Direction.Axis.X);
            Optional<BlockLocating.Rectangle> optional2 = worldTo.getPortalForcer().createPortal(originOfTesting, axis);
            if (optional2.isEmpty()) {
                if (teleportingEntity instanceof ServerPlayerEntity player) {
                    player.sendMessage(Text.translatable("error.infinity.portal.cannot_create"));
                }
                return emptyTarget(teleportingEntity);
            }
            rectangleTo = optional2.get();
            posTo = rectangleTo.lowerLeft;
            postDimensionTransition = TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET
                    .then(TeleportTarget.ADD_PORTAL_CHUNK_TICKET);
        }

        trySyncPortals(worldFrom, posFrom, worldTo, posTo);

        return getExitPortalTarget(teleportingEntity, posFrom, rectangleTo, worldTo, postDimensionTransition);
    }

    static void trySyncPortals(ServerWorld worldFrom, BlockPos posFrom, ServerWorld worldTo, BlockPos posTo) {
        if (!(worldTo.getBlockState(posTo).getBlock() instanceof NetherPortalBlock)) return;

        PortalCreationLogic.PortalModifierUnion otherSideModifier = new PortalCreationLogic.PortalModifierUnion();
        Identifier idFrom = worldFrom.getRegistryKey().getValue();

        if (worldTo.getBlockEntity(posTo) instanceof InfinityPortalBlockEntity ipbe) {
            if (ipbe.getDimension() != idFrom) return;
            if (isValidDestinationStrong(worldTo, worldFrom, ipbe.getOtherSidePos())) return; //don't resync what's already synced
        }
        else {
            otherSideModifier = PortalCreationLogic.forInitialSetupping(worldTo, posTo, idFrom, true); //make it an infinity portal while you're at it
        }

        otherSideModifier.addModifier(ipbe1 -> ipbe1.setBlockPos(posFrom));
        PortalCreationLogic.modifyPortalRecursive(worldFrom, posFrom,
                new PortalCreationLogic.PortalModifier(ipbe -> ipbe.setBlockPos(posTo)));
        PortalCreationLogic.modifyPortalRecursive(worldTo, posTo, otherSideModifier);
    }
}
