package net.lerariemann.infinity.block.custom;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.lerariemann.infinity.loading.DimensionGrabber;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class NeitherPortalBlock extends NetherPortalBlock implements BlockEntityProvider {
    private static final Random RANDOM = new Random();

    public NeitherPortalBlock(Settings settings) {
        super(settings);
    }
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new NeitherPortalBlockEntity(pos, state, Math.abs(RANDOM.nextInt()));
    }

    public static void open(MinecraftServer s, World world, BlockPos pos) {
        RandomProvider prov = ((MinecraftServerAccess)(s)).getDimensionProvider();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof NeitherPortalBlockEntity) {
            long i = ((NeitherPortalBlockEntity) blockEntity).getDimension();
            addDimension(s, i, prov.rule("runtimeGenerationEnabled"));
            modifyPortal(world, pos, world.getBlockState(pos), i, true);
            world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1f, 1f);
        }
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

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            LogManager.getLogger().info(((NeitherPortalBlockEntity) Objects.requireNonNull(world.getBlockEntity(pos))).getDimension());
            MinecraftServer s = world.getServer();
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (s!=null && blockEntity instanceof NeitherPortalBlockEntity) {
                if (((NeitherPortalBlockEntity)blockEntity).getOpen()) return ActionResult.SUCCESS;
                RandomProvider prov = ((MinecraftServerAccess)(s)).getDimensionProvider();
                boolean bl = prov.portalKey.isBlank();
                if (bl) {
                    open(s, world, pos);
                    return ActionResult.SUCCESS;
                }
                ItemStack itemStack = player.getStackInHand(hand);
                Item item = Registries.ITEM.get(new Identifier(prov.portalKey));
                if (itemStack.isOf(item)) {
                    if (!player.getAbilities().creativeMode) {
                        itemStack.decrement(1);
                    }
                    open(s, world, pos);
                }
            }
        }
        return ActionResult.SUCCESS;
    }


    public static void addDimension(MinecraftServer server, long i, boolean bl) {
        Identifier id = new Identifier(InfinityMod.MOD_ID, "generated_" + i);
        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, id);
        if ((server.getWorld(key) == null) && (!((MinecraftServerAccess)(server)).hasToAdd(key))) {
            RandomDimension d = new RandomDimension(i, server);
            if (bl) {
                ((MinecraftServerAccess) (server)).addWorld(key, (new DimensionGrabber(server.getRegistryManager())).grab_all(Paths.get(d.storagePath), i));
                server.getPlayerManager().getPlayerList().forEach(a ->
                        ServerPlayNetworking.send(a, InfinityMod.WORLD_ADD, buildPacket(id, d)));
                LogManager.getLogger().info("Packet sent");
            }
        }
    }

    static PacketByteBuf buildPacket(Identifier id, RandomDimension d) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(id);
        buf.writeNbt(d.type.data);
        buf.writeInt(d.random_biomes.size());
        d.random_biomes.forEach(b -> {
            buf.writeIdentifier(new Identifier(InfinityMod.MOD_ID, b.name));
            buf.writeNbt(b.data);
        });
        return buf;
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
}
