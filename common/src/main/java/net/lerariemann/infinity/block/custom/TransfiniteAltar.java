package net.lerariemann.infinity.block.custom;

import com.mojang.serialization.MapCodec;
import net.lerariemann.infinity.registry.core.ModBlockEntities;
import net.lerariemann.infinity.block.entity.TransfiniteAltarEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TransfiniteAltar extends BlockWithEntity {
    public static final BooleanProperty FLOWER = BooleanProperty.of("flower");
    public static final IntProperty COLOR = IntProperty.of("color", 0, 6);
    public static final VoxelShape BASE_SHAPE = Block.createCuboidShape(1.5, 0, 1.5, 14.5, 14, 14.5);
    public static final VoxelShape TOP_SHAPE = Block.createCuboidShape(0, 14, 0, 16, 16, 16);
    public static final VoxelShape LEG1 = Block.createCuboidShape(0, 0, 0, 3, 3, 3);
    public static final VoxelShape LEG2 = Block.createCuboidShape(0, 0, 13, 3, 3, 16);
    public static final VoxelShape LEG3 = Block.createCuboidShape(13, 0, 0, 16, 3, 3);
    public static final VoxelShape LEG4 = Block.createCuboidShape(13, 0, 13, 16, 3, 16);
    public static final VoxelShape SHAPE = VoxelShapes.union(BASE_SHAPE, TOP_SHAPE, LEG1, LEG2, LEG3, LEG4);
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public TransfiniteAltar(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(COLOR, 0).with(FLOWER, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(COLOR);
        builder.add(FLOWER);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TransfiniteAltarEntity(pos, state);
    }
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClient) return checkType(type, ModBlockEntities.ALTAR.get(), TransfiniteAltarEntity::serverTick);
        return null;
    }

    public static void bumpAge(World world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, state.with(COLOR, state.get(COLOR) + 1));
    }

    static double next() {
        return TransfiniteAltarEntity.r.nextDouble()-0.5;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(COLOR) > 0) {
            return;
        }
        Vec3d p = pos.toCenterPos();
        world.addParticle(ParticleTypes.HEART, p.x + next(), p.y + next(), p.z + next(), next(), next(), next());
    }
}
