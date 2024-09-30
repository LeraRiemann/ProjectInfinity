package net.lerariemann.infinity.block.custom;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.loading.DimensionGrabber;
import net.lerariemann.infinity.var.ModCommands;
import net.lerariemann.infinity.var.ModCriteria;
import net.lerariemann.infinity.var.ModPayloads;
import net.lerariemann.infinity.var.ModStats;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
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
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
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

public class NeitherPortalBlock extends NetherPortalBlock implements BlockEntityProvider {
    private static final Random RANDOM = new Random();

    public NeitherPortalBlock(Settings settings) {
        super(settings);
    }
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new NeitherPortalBlockEntity(pos, state, Math.abs(RANDOM.nextInt()));
    }

    public static boolean open(MinecraftServer s, World world, BlockPos pos, boolean countRepeats) {
        RandomProvider prov = ((MinecraftServerAccess)(s)).projectInfinity$getDimensionProvider();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        boolean bl = false;
        if (blockEntity instanceof NeitherPortalBlockEntity) {
            long i = ((NeitherPortalBlockEntity) blockEntity).getDimension();
            bl = countRepeats || addDimension(s, i, prov.rule("runtimeGenerationEnabled"));
            modifyPortal(world, pos, world.getBlockState(pos), i, true);
            world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1f, 1f);
        }
        return bl;
    }

    private static void changeDim(World world, BlockPos pos, Direction.Axis axis, long i, boolean open) {
        world.setBlockState(pos, ModBlocks.NEITHER_PORTAL.getDefaultState().with(AXIS, axis));
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            ((NeitherPortalBlockEntity)blockEntity).setDimension(i);
            ((NeitherPortalBlockEntity)blockEntity).setOpen(open);
        }
    }

    public static void modifyPortal(World world, BlockPos pos, BlockState state, long i, boolean open) {
        Set<BlockPos> set = Sets.newHashSet();
        Queue<BlockPos> queue = Queues.newArrayDeque();
        queue.add(pos);
        BlockPos blockPos;
        Direction.Axis axis = state.get(AXIS);
        while ((blockPos = queue.poll()) != null) {
            set.add(blockPos);
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.getBlock() instanceof NetherPortalBlock || blockState.getBlock() instanceof NeitherPortalBlock) {
                changeDim(world, blockPos, axis, i, open);
                BlockPos blockPos2 = blockPos.offset(Direction.UP);
                if (!set.contains(blockPos2))
                    queue.add(blockPos2);
                blockPos2 = blockPos.offset(Direction.DOWN);
                if (!set.contains(blockPos2))
                    queue.add(blockPos2);
                blockPos2 = blockPos.offset(Direction.NORTH);
                if (!set.contains(blockPos2))
                    queue.add(blockPos2);
                blockPos2 = blockPos.offset(Direction.SOUTH);
                if (!set.contains(blockPos2))
                    queue.add(blockPos2);
                blockPos2 = blockPos.offset(Direction.WEST);
                if (!set.contains(blockPos2))
                    queue.add(blockPos2);
                blockPos2 = blockPos.offset(Direction.EAST);
                if (!set.contains(blockPos2))
                    queue.add(blockPos2);
            }
        }
        if (open) world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1f, 1f);
    }

    boolean world_exists(MinecraftServer s, long l) {
        return s.getSavePath(WorldSavePath.DATAPACKS).resolve(ModCommands.getIdentifier(l, s).getPath()).toFile().exists() ||
                s.getWorld(ModCommands.getKey(l, s)) != null;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            MinecraftServer s = world.getServer();
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (s!=null && blockEntity instanceof NeitherPortalBlockEntity) {
                if (((NeitherPortalBlockEntity)blockEntity).getOpen() && world_exists(s, ((NeitherPortalBlockEntity)blockEntity).getDimension()))
                    return ActionResult.SUCCESS;
                RandomProvider prov = ((MinecraftServerAccess)(s)).projectInfinity$getDimensionProvider();
                boolean bl = prov.portalKey.isBlank();
                boolean bl2 = false;
                if (bl) {
                    bl2 = open(s, world, pos, false);
                }
                else {
                    ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);
                    Item item = Registries.ITEM.get(Identifier.of(prov.portalKey));
                    if (itemStack.isOf(item)) {
                        if (!player.getAbilities().creativeMode && prov.rule("consumePortalKey")) {
                            itemStack.decrement(1);
                        }
                        bl2 = open(s, world, pos, false);
                    }
                }
                if (bl2) {
                    player.increaseStat(ModStats.DIMS_OPENED_STAT, 1);
                    ModCriteria.DIMS_OPENED.trigger((ServerPlayerEntity)player);
                }
                player.increaseStat(ModStats.PORTALS_OPENED_STAT, 1);
            }
        }
        return ActionResult.SUCCESS;
    }

    public static boolean addDimension(MinecraftServer server, long i, boolean bl) {
        RegistryKey<World> key = ModCommands.getKey(i, server);
        if ((server.getWorld(key) == null) && (!((MinecraftServerAccess)(server)).projectInfinity$hasToAdd(key)) && !ModCommands.checkEnd(i, server)) {
            RandomDimension d = new RandomDimension(i, server);
            if (bl) {
                ((MinecraftServerAccess) (server)).projectInfinity$addWorld(key, (new DimensionGrabber(server.getRegistryManager())).grab_all(d));
                server.getPlayerManager().getPlayerList().forEach(a -> sendNewWorld(a, ModCommands.getIdentifier(i, server), d));
                return true;
            }
        }
        return false;
    }

    public static void sendNewWorld(ServerPlayerEntity player, Identifier id, RandomDimension d) {
        d.random_biomes.forEach(b -> ServerPlayNetworking.send(player, new ModPayloads.BiomeAddPayload(InfinityMod.getId(b.name), b.data)));
        ServerPlayNetworking.send(player, new ModPayloads.WorldAddPayload(id, d.type != null ? d.type.data : new NbtCompound()));
    }

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
                long dim = ((NeitherPortalBlockEntity)blockEntity).getDimension();
                Vec3d vec3d = Vec3d.unpackRgb((int)dim);
                double color = 1.0D + (dim >> 16 & 0xFF) / 255.0D;
                eff = new DustParticleEffect(new Vector3f((float)vec3d.x, (float)vec3d.y, (float)vec3d.z), (float)color);
            }

            world.addParticle(eff, d, e, f, g, h, j);
        }
    }

    static Map<Item, String> recipes = Map.ofEntries(
            Map.entry(Items.BOOKSHELF, "infinity:book_box"),
            Map.entry(Items.TNT, "infinity:timebomb"),
            Map.entry(Items.LECTERN, "infinity:altar")
    );

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient() && entity instanceof ItemEntity e && !e.isRemoved()) {
            ItemStack itemStack = e.getStack();
            if (recipes.containsKey(itemStack.getItem())) {
                Vec3d v = entity.getVelocity();
                ItemEntity result = new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(),
                        new ItemStack(Registries.ITEM.get(Identifier.of(recipes.get(itemStack.getItem())))).copyWithCount(itemStack.getCount()),
                        -v.x, -v.y, -v.z);
                world.spawnEntity(result);
                entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
            }
        }
        super.onEntityCollision(state, world, pos, entity);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (world.getDimension().natural() && world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && random.nextInt(2000) < world.getDifficulty().getId()) {
            ChaosPawn entity;
            while (world.getBlockState(pos).isOf(this)) {
                pos = pos.down();
            }
            if (world.getBlockState(pos).allowsSpawning(world, pos, ModEntities.CHAOS_PAWN) &&
                    ((MinecraftServerAccess)world.getServer()).projectInfinity$getDimensionProvider().rule("chaosMobsEnabled") &&
                    (entity = ModEntities.CHAOS_PAWN.spawn(world, pos.up(), SpawnReason.STRUCTURE)) != null) {
                entity.resetPortalCooldown();
                BlockEntity blockEntity = world.getBlockEntity(pos.up());
                if (blockEntity instanceof NeitherPortalBlockEntity) {
                    int dim = (int)((NeitherPortalBlockEntity)blockEntity).getDimension();
                    Vec3d c = Vec3d.unpackRgb(dim);
                    entity.setAllColors((int)(256 * c.z) + 256 * (int)(256 * c.y) + 65536 * (int)(256 * c.x));
                }
            }
        }
    }
}
