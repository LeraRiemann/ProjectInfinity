package net.lerariemann.infinity.block.custom;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.options.PortalColorApplier;
import net.lerariemann.infinity.util.RandomProvider;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.item.ModComponentTypes;
import net.lerariemann.infinity.item.ModItems;
import net.lerariemann.infinity.loading.DimensionGrabber;
import net.lerariemann.infinity.util.WarpLogic;
import net.lerariemann.infinity.var.*;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
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
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

import static net.lerariemann.infinity.compat.ComputerCraftCompat.checkPrintedPage;

public class NeitherPortalBlock extends NetherPortalBlock implements BlockEntityProvider {
    private static final Random RANDOM = new Random();

    public NeitherPortalBlock(Settings settings) {
        super(settings);
    }
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new NeitherPortalBlockEntity(pos, state, Math.abs(RANDOM.nextInt()));
    }

    public static void tryCreatePortalFromItem(BlockState state, World world, BlockPos pos, ItemEntity entity) {
        ItemStack itemStack = entity.getStack();

        /* Check if the item provided is a transfinite key. */
        Identifier key_dest = itemStack.getComponents().get(ModComponentTypes.KEY_DESTINATION.get());
        if ((entity.getStack().getItem().equals(ModItems.TRANSFINITE_KEY.get())) && key_dest == null) {
            key_dest = Identifier.of("minecraft:random");
        }
        if (key_dest != null)  {
            MinecraftServer server = world.getServer();
            if (server != null) {
                boolean bl = NeitherPortalBlock.modifyOnInitialCollision(key_dest, world, pos, state);
                if (bl) entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
                return;
            }
        }

        /* Check if the item provided is a book of some kind. */
        WritableBookContentComponent writableComponent = itemStack.getComponents().get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
        WrittenBookContentComponent writtenComponent = itemStack.getComponents().get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        String printedComponent = null;
        if (Platform.isModLoaded("computercraft")) {
            printedComponent = checkPrintedPage(itemStack);
        }
        if (writableComponent != null || writtenComponent != null || printedComponent != null) {
            String content = NeitherPortalBlock.parseComponents(writableComponent, writtenComponent, printedComponent);
            MinecraftServer server = world.getServer();
            if (server != null) {
                Identifier id = WarpLogic.getIdentifier(content, server);
                boolean bl = NeitherPortalBlock.modifyOnInitialCollision(id, world, pos, state);
                if (bl) entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
            }
        }
    }

    /* Extracts the string used to generate the dimension ID from component content. */
    public static String parseComponents(WritableBookContentComponent writableComponent, WrittenBookContentComponent writtenComponent, String printedComponent) {
        String content = "";
        try {
            if (writableComponent != null) {
                content = writableComponent.pages().getFirst().raw();
            }
            if (writtenComponent != null) {
                content = writtenComponent.pages().getFirst().raw().getString();
            }
        }
        catch (NoSuchElementException e) {
            content = "";
        }

        if (printedComponent != null) {
            content = printedComponent;
        }
        return content;
    }

    /* Sets the portal color and destination and calls to open the portal immediately if the portal key is blank.
    * Statistics for opening the portal are attributed to the nearest player. */
    public static boolean modifyOnInitialCollision(Identifier dimName, World world, BlockPos pos, BlockState state) {
        MinecraftServer server = world.getServer();
        if (dimName.toString().equals("minecraft:random")) {
            dimName = WarpLogic.getRandomId(world.getRandom());
        }
        if (server != null) {
            PlayerEntity nearestPlayer = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 5, false);

            if (((MinecraftServerAccess)server).infinity$needsInvocation()) {
                WarpLogic.onInvocationNeedDetected(nearestPlayer);
                return false;
            }

            /* Set color and destination. Open status = the world that is being accessed exists already. */
            boolean dimensionExistsAlready = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, dimName)) != null;
            NeitherPortalBlock.modifyPortalRecursive(world, pos, state, dimName, dimensionExistsAlready);

            if (dimensionExistsAlready) {
                if (nearestPlayer != null) nearestPlayer.increaseStat(ModStats.PORTALS_OPENED_STAT, 1);
                runAfterEffects(world, pos, false);
            }

            /* If the portal key is blank, open the portal immediately. */
            else if (RandomProvider.getProvider(server).portalKey.isBlank()) {
                NeitherPortalBlock.openWithStatIncrease(nearestPlayer, server, world, pos);
            }
        }
        return true;
    }

    /* This is being called when the portal is right-clicked. */
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            MinecraftServer s = world.getServer();
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (s!=null && blockEntity instanceof NeitherPortalBlockEntity) {
                /* If the portal is open already, nothing should happen. */
                if (((NeitherPortalBlockEntity)blockEntity).getOpen() &&
                        world_exists(s, ((NeitherPortalBlockEntity)blockEntity).getDimension()))
                    return ActionResult.SUCCESS;

                /* If the portal key is blank, open the portal on any right-click. */
                RandomProvider prov = RandomProvider.getProvider(s);
                if (prov.portalKey.isBlank()) {
                    openWithStatIncrease(player, s, world, pos);
                }

                /* Otherwise check if we're using the correct key. If so, open. */
                else {
                    ItemStack usedKey = player.getStackInHand(Hand.MAIN_HAND);
                    Item correctKey = Registries.ITEM.get(Identifier.of(prov.portalKey));
                    if (usedKey.isOf(correctKey)) {
                        if (!player.getAbilities().creativeMode && prov.rule("consumePortalKey")) {
                            usedKey.decrement(1); // Consume the key if needed
                        }
                        openWithStatIncrease(player, s, world, pos);
                    }
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    /* Jingle signaling the portal is now usable. */
    public static void runAfterEffects(World world, BlockPos pos, boolean dimensionIsNew) {
        if (dimensionIsNew) world.playSound(null, pos, SoundEvents.BLOCK_VAULT_OPEN_SHUTTER, SoundCategory.BLOCKS, 1f, 1f);
        world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1f, 1f);
    }

    /* Calls to open the portal and attributes the relevant statistics to a player provided. */
    public static void openWithStatIncrease(PlayerEntity player, MinecraftServer s, World world, BlockPos pos) {
        if (((MinecraftServerAccess)s).infinity$needsInvocation()) {
            WarpLogic.onInvocationNeedDetected(player);
            return;
        }
        boolean isDimensionNew = NeitherPortalBlock.open(s, world, pos);
        if (player != null) {
            if (isDimensionNew) {
                player.increaseStat(ModStats.DIMS_OPENED_STAT, 1);
                ModCriteria.DIMS_OPENED.get().trigger((ServerPlayerEntity)player);
            }
            player.increaseStat(ModStats.PORTALS_OPENED_STAT, 1);
        }
    }

    /* Opens the portal by trying to make it usable, including a call to generate a dimension if needed. */
    public static boolean open(MinecraftServer s, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        boolean bl = false;
        if (blockEntity instanceof NeitherPortalBlockEntity) {
            /* Call dimension creation. */
            Identifier i = ((NeitherPortalBlockEntity) blockEntity).getDimension();
            if (i.getNamespace().equals(InfinityMod.MOD_ID)) {
                bl = addInfinityDimension(s, i);
            }

            /* Set the portal's open status making it usable. */
            modifyPortalRecursive(world, pos, world.getBlockState(pos), i, true);
            runAfterEffects(world, pos, bl);
        }
        return bl;
    }

    /* Sets the portal color, destination and open status. Calls itself recursively for neighbouring blocks. */
    public static void modifyPortalRecursive(World world, BlockPos pos, BlockState state, Identifier id, boolean open) {
        Set<BlockPos> set = Sets.newHashSet();
        Queue<BlockPos> queue = Queues.newArrayDeque();
        queue.add(pos);
        BlockPos blockPos;
        Direction.Axis axis = state.get(AXIS);
        PortalColorApplier applier = WarpLogic.getPortalColorApplier(id, world.getServer());
        while ((blockPos = queue.poll()) != null) {
            set.add(blockPos);
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.getBlock() instanceof NetherPortalBlock || blockState.getBlock() instanceof NeitherPortalBlock) {
                modifyPortalBlock(world, blockPos, axis, id, open, applier.apply(id, world.getServer(), pos));
                Set<Direction> toCheck = (axis == Direction.Axis.Z) ?
                        Set.of(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH) :
                        Set.of(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST);
                BlockPos blockPos2;
                for (Direction dir : toCheck) {
                    blockPos2 = blockPos.offset(dir);
                    if (!set.contains(blockPos2))
                        queue.add(blockPos2);
                }
            }
        }
    }

    /* Sets the portal color, destination and open status for one portal block. */
    private static void modifyPortalBlock(World world, BlockPos pos, Direction.Axis axis, Identifier id, boolean open,
                                          int color) {
        world.setBlockState(pos, ModBlocks.NEITHER_PORTAL.get().getDefaultState().with(AXIS, axis));
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            ((NeitherPortalBlockEntity)blockEntity).setDimension(color, id);
            ((NeitherPortalBlockEntity)blockEntity).setOpen(open);
        }
    }

    public static boolean world_exists(MinecraftServer s, Identifier l) {
        return (!l.getNamespace().equals(InfinityMod.MOD_ID)) ||
                s.getSavePath(WorldSavePath.DATAPACKS).resolve(l.getPath()).toFile().exists() ||
                s.getWorld(RegistryKey.of(RegistryKeys.WORLD, l)) != null;
    }

    /* Calls to create the dimension based on its ID. Returns true if the dimension being opened is indeed brand new. */
    public static boolean addInfinityDimension(MinecraftServer server, Identifier id) {
        /* checks if the dimension requested is valid and does not already exist */
        if (!id.getNamespace().equals(InfinityMod.MOD_ID)) return false;
        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, id);
        if ((server.getWorld(key) != null) || ((MinecraftServerAccess)(server)).infinity$hasToAdd(key)) return false;

        /* creates the dimension datapack */
        RandomDimension d = new RandomDimension(id, server);

        if (!RandomProvider.getProvider(server).rule("runtimeGenerationEnabled")) return false;
        ((MinecraftServerAccess)(server)).infinity$addWorld(
                key, (new DimensionGrabber(server.getRegistryManager())).grab_all(d)); // create the dimension
        server.getPlayerManager().getPlayerList().forEach(
                a -> sendNewWorld(a, id, d)); //and send everyone its data for clientside updating
        return true;
    }

    /* Create and send S2C packets necessary for the client to process a freshly added dimension. */
    public static void sendNewWorld(ServerPlayerEntity player, Identifier id, RandomDimension d) {
        d.random_biomes.forEach(b -> PlatformMethods.sendS2CPayload(player, new ModPayloads.BiomeAddPayload(InfinityMod.getId(b.name), b.data)));
        PlatformMethods.sendS2CPayload(player, new ModPayloads.WorldAddPayload(id, d.type != null ? d.type.data : new NbtCompound()));
    }

    /* Spawns colourful particles. */
    @Environment(EnvType.CLIENT)
    @Override
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
            if (blockEntity instanceof NeitherPortalBlockEntity) {
                int colorInt = ((NeitherPortalBlockEntity)blockEntity).getPortalColor();
                Vec3d vec3d = Vec3d.unpackRgb(colorInt);
                double color = 1.0D + (colorInt >> 16 & 0xFF) / 255.0D;
                eff = new DustParticleEffect(new Vector3f((float)vec3d.x, (float)vec3d.y, (float)vec3d.z), (float)color);
            }

            world.addParticle(eff, d, e, f, g, h, j);
        }
    }

    static Map<Item, String> recipes = Map.ofEntries(
            Map.entry(Items.BOOKSHELF, "infinity:book_box"),
            Map.entry(Items.TNT, "infinity:timebomb"),
            Map.entry(Items.LECTERN, "infinity:altar"),
            Map.entry(Items.AMETHYST_SHARD, "infinity:key")
    );

    /* Adds logic for portal-based recipes. */
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient() && entity instanceof ItemEntity e && !e.isRemoved()) {
            ItemStack itemStack = e.getStack();
            if (recipes.containsKey(itemStack.getItem())) {
                Vec3d v = entity.getVelocity();
                ItemStack resStack = new ItemStack(Registries.ITEM.get(Identifier.of(recipes.get(itemStack.getItem()))));
                if (resStack.isOf(ModItems.TRANSFINITE_KEY.get())) {
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    if (blockEntity instanceof NeitherPortalBlockEntity portal) {
                        Integer keycolor = WarpLogic.getKeyColorFromId(portal.getDimension(), world.getServer());
                        ComponentMap newMap = (ComponentMap.builder().add(ModComponentTypes.KEY_DESTINATION.get(), portal.getDimension())
                                .add(ModComponentTypes.COLOR.get(), keycolor)).build();
                        resStack.applyComponentsFrom(newMap);
                    }
                }
                ItemEntity result = new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(),
                        resStack.copyWithCount(itemStack.getCount()),
                        -v.x, -v.y, -v.z);
                world.spawnEntity(result);
                entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
            }
        }
        if (!world.isClient() && entity instanceof PlayerEntity player &&
                RandomProvider.getProvider(world.getServer()).portalKey.isBlank() &&
                world.getBlockEntity(pos) instanceof NeitherPortalBlockEntity be && !be.getOpen()) {
            openWithStatIncrease(player, world.getServer(), world, pos);
        }
        super.onEntityCollision(state, world, pos, entity);
    }

    /* Spawns chaos pawns in the portal. */
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (world.getDimension().natural() && world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)
                && random.nextInt(2000) < world.getDifficulty().getId()) {
            ChaosPawn entity;
            while (world.getBlockState(pos).isOf(this)) {
                pos = pos.down();
            }
            if (world.getBlockState(pos).allowsSpawning(world, pos, ModEntities.CHAOS_PAWN.get()) &&
                    ModEntities.chaosMobsEnabled(world) &&
                    (entity = ModEntities.CHAOS_PAWN.get().spawn(world, pos.up(), SpawnReason.STRUCTURE)) != null) {
                entity.resetPortalCooldown();
                BlockEntity blockEntity = world.getBlockEntity(pos.up());
                if (blockEntity instanceof NeitherPortalBlockEntity) {
                    int color = ((NeitherPortalBlockEntity)blockEntity).getPortalColor();
                    Vec3d c = Vec3d.unpackRgb(color);
                    entity.setAllColors((int)(256 * c.z) + 256 * (int)(256 * c.y) + 65536 * (int)(256 * c.x));
                }
            }
        }
    }
}
