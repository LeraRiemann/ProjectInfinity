package net.lerariemann.infinity.block.custom;

import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.var.ModCriteria;
import net.lerariemann.infinity.var.ModSounds;
import net.lerariemann.infinity.var.ModStats;
import net.minecraft.block.*;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

public class TimeBombBlock extends Block {
    public static  final BooleanProperty ACTIVE = BooleanProperty.of("active");
    public static final VoxelShape TOP_SHAPE = Block.createCuboidShape(3, 0, 3, 13, 2, 13);
    public static final VoxelShape BOTTOM_SHAPE = Block.createCuboidShape(3, 12, 3, 13, 14, 13);
    public static final VoxelShape INNER_SHAPE = Block.createCuboidShape(4, 2, 4, 12, 12, 12);
    public static final VoxelShape PIMP_SHAPE = Block.createCuboidShape(7, 14, 7, 9, 16, 9);
    public static final VoxelShape SHAPE = VoxelShapes.union(BOTTOM_SHAPE, TOP_SHAPE, INNER_SHAPE, PIMP_SHAPE);
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public TimeBombBlock(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(ACTIVE, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    void activate(ServerWorld world, Path path) {
        ((Timebombable)world).projectInfinity$timebomb(1);
        try {
            FileUtils.deleteDirectory(path.toFile());
        } catch (IOException ignored) {
        }
    }

    static AreaEffectCloudEntity genCloud(World world, BlockPos pos) {
        AreaEffectCloudEntity e = new AreaEffectCloudEntity(world, pos.toCenterPos().getX(), pos.toCenterPos().getY(), pos.toCenterPos().getZ());
        e.setParticleType(ParticleTypes.DRAGON_BREATH);
        e.setRadius(1.0f);
        e.setDuration(3500);
        e.setRadiusGrowth(0.006f);
        world.syncWorldEvent(WorldEvents.DRAGON_BREATH_CLOUD_SPAWNS, pos, 1);
        return e;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            if (world.getRegistryKey().getValue().toString().contains("infinity")) {
                ServerWorld w = ((ServerPlayerEntity)player).getServerWorld();
                if (((Timebombable)w).projectInfinity$isTimebobmed() == 0) {
                    if (state.get(ACTIVE)) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        world.getEntitiesByType(TypeFilter.instanceOf(AreaEffectCloudEntity.class), Box.of(pos.toCenterPos(), 1.0, 1.0, 1.0), Entity::isAlive).
                                forEach(e -> e.remove(Entity.RemovalReason.DISCARDED));
                        return ActionResult.SUCCESS;
                    } //remove after regenerating a dimension
                    if (player.getStackInHand(Hand.MAIN_HAND).isEmpty() && player.isSneaking()) {
                        Path path = w.getServer().getSavePath(WorldSavePath.DATAPACKS).resolve(w.getRegistryKey().getValue().getPath());
                        activate(w, path);
                        world.spawnEntity(genCloud(world, pos));
                        player.increaseStat(ModStats.WORLDS_DESTROYED_STAT, 1);
                        ModCriteria.DIMS_CLOSED.trigger((ServerPlayerEntity)player);
                        world.setBlockState(pos, state.with(ACTIVE, true));
                        world.playSound(null, pos, ModSounds.IVORY_MUSIC_CHALLENGER_EVENT, SoundCategory.BLOCKS, 1f, 1f);
                        return ActionResult.SUCCESS;
                    } //activate
                    if (!state.get(ACTIVE)) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        player.getInventory().insertStack(ModBlocks.TIME_BOMB_ITEM.get().getDefaultStack());
                        return ActionResult.SUCCESS;
                    } //pick up
                }
            }
            else if (player.getStackInHand(Hand.MAIN_HAND).isEmpty()) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                player.getInventory().insertStack(ModBlocks.TIME_BOMB_ITEM.get().getDefaultStack());
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}
