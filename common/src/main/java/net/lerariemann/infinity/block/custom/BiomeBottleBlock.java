package net.lerariemann.infinity.block.custom;

import net.lerariemann.infinity.block.entity.BiomeBottleBlockEntity;
import net.lerariemann.infinity.registry.core.ModBlockEntities;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class BiomeBottleBlock extends BlockWithEntity {
    public static final IntProperty LEVEL = IntProperty.of("level", 0, 10);
    public static final VoxelShape MAIN = Block.createCuboidShape(2, 0, 2, 14, 12, 14);
    public static final VoxelShape TIP = Block.createCuboidShape(6, 12, 6, 10, 16, 10);
    public static final VoxelShape CORK = Block.createCuboidShape(5, 14, 5, 11, 15, 11);
    public static final VoxelShape SHAPE = VoxelShapes.union(MAIN, TIP, CORK);

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public BiomeBottleBlock(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(LEVEL, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasNbt()) {
            assert itemStack.getNbt() != null;
            final int charge = itemStack.getNbt().getCompound("BlockEntityTag").getInt("Charge");
            final int level = getLevel(charge);
            state = state.with(LEVEL, level);
            world.setBlockState(pos, state);
            if (world.getBlockEntity(pos) instanceof BiomeBottleBlockEntity biomeBottleBlockEntity) {
                biomeBottleBlockEntity.color = itemStack.getNbt().getCompound("BlockEntityTag").getInt("Color");
            }
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BiomeBottleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlockEntities.BIOME_BOTTLE.get(), world.isClient ? null : BiomeBottleBlockEntity::serverTick);
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return world.getBlockEntity(pos) instanceof BiomeBottleBlockEntity bbbe
                ? bbbe.asStack()
                : super.getPickStack(world, pos, state);
    }

    public static final int maxAllowedCharge = 10000;

    public static Identifier defaultBiome() {
        return new Identifier("plains");
    }

    public static Rarity getRarity(int charge) {
        return charge < 1000 ? Rarity.COMMON : charge < 9000 ? Rarity.UNCOMMON : Rarity.RARE;
    }
    public static void updateCharge(ItemStack stack, int charge) {
        if (stack.hasNbt()) {
            assert stack.getNbt() != null;
            stack.getNbt().getCompound("BlockEntityTag").putInt("Charge", charge);
        }
    }

    public static void updateCharge(ItemStack stack) {
        int charge = getCharge(stack);
        if (charge > 0) updateCharge(stack, charge);
    }

    public static int getLevel(int charge) {
        return MathHelper.clamp(charge / 100, 0, 10);
    }

    public static boolean isEmpty(ItemStack stack) {
        return getCharge(stack) == 0;
    }

    public static Identifier getBiome(ItemStack stack) {
        if (stack.hasNbt()) {
            assert stack.getNbt() != null;
            return new Identifier(stack.getNbt().getCompound("BlockEntityTag").getString("Biome"));
        }
        return defaultBiome();
    }

    public static int getCharge(ItemStack stack) {
        if (stack.hasNbt()) {
            assert stack.getNbt() != null;
            return stack.getNbt().getCompound("BlockEntityTag").getInt("Charge");
        }
        return 0;
    }

    public static void playSploosh(ServerWorld world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1f, 1f);
        world.spawnParticles(ParticleTypes.SPLASH, pos.getX() + 0.5,
                pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.5, 0.5, 0.5, 0.2);
    }

    public static RegistryEntry<Biome> biomeFromId(ServerWorld world, Identifier biome) {
        Optional<RegistryEntry.Reference<Biome>> entry = world.getServer().getRegistryManager().get(RegistryKeys.BIOME).getEntry(biome);
        return entry.orElse(null);
    }

    public static void spreadCircle(ServerWorld world, BlockPos origin, Identifier biomeId, int charge) {
        spreadRing(world, origin, biomeId, 0, charge);
    }
    public static void spreadRing(ServerWorld world, BlockPos origin, Identifier biomeId, int chargemin, int chargemax) {
        Set<BlockPos> posSet = new HashSet<>();
        Set<Chunk> set = new HashSet<>();
        origin = origin.down(origin.getY());
        double ramax = chargemax / Math.PI;
        double ramin = chargemin / Math.PI;
        for (int i = 0; i*i < ramax; i++) {
            for (int j = 0; i*i + j*j < ramax; j++) if (i*i + j*j >= ramin) {
                List<BlockPos> signs = offsets(origin, i, j);
                posSet.addAll(signs);
                set.addAll(signs.stream().map(ChunkPos::new)
                        .map(chunkPos -> world.getChunk(chunkPos.getStartPos()))
                        .filter(Objects::nonNull).collect(Collectors.toSet()));
            }
        }
        spread(world, set, posSet, biomeFromId(world, biomeId));
    }

    public static void spread(ServerWorld world, Set<Chunk> set, Set<BlockPos> posSet, RegistryEntry<Biome> biome) {
        if (biome == null) return;
        set.forEach(chunk -> {
            if (chunk != null) {
                chunk.populateBiomes((x, y, z, noise) -> {
                    int i = BiomeCoords.toBlock(x);
                    int k = BiomeCoords.toBlock(z);
                    RegistryEntry<Biome> registryEntry2 = chunk.getBiomeForNoiseGen(x, y, z);
                    if (posSet.contains(new BlockPos(i, 0, k))) {
                        return biome;
                    }
                    return registryEntry2;
                }, world.getChunkManager().getNoiseConfig().getMultiNoiseSampler());
                chunk.setNeedsSaving(true);
            }
        });
        world.getChunkManager().threadedAnvilChunkStorage.sendChunkBiomePackets(set.stream().toList());
    }

    public static List<BlockPos> offsets(BlockPos origin, int i, int j) {
        return List.of(origin.add(i, 0, j), origin.add(j, 0, i), origin.add(-i, 0, j), origin.add(j, 0, -i),
                origin.add(i, 0, -j), origin.add(-j, 0, i), origin.add(-i, 0, -j), origin.add(-j, 0, -i));
    }
}