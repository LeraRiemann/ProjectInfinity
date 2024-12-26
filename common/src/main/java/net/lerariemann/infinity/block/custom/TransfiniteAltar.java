package net.lerariemann.infinity.block.custom;

import net.lerariemann.infinity.block.entity.BiomeBottleBlockEntity;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.registry.var.ModCriteria;
import net.lerariemann.infinity.util.AntBattle;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TransfiniteAltar extends Block {
    public static final int numColors = 13;
    public static final BooleanProperty FLOWER = BooleanProperty.of("flower");
    public static final IntProperty COLOR = IntProperty.of("color", 0, numColors - 1);
    public static final VoxelShape BASE_SHAPE = Block.createCuboidShape(1.5, 0, 1.5, 14.5, 14, 14.5);
    public static final VoxelShape TOP_SHAPE = Block.createCuboidShape(0, 14, 0, 16, 16, 16);
    public static final VoxelShape LEG1 = Block.createCuboidShape(0, 0, 0, 3, 3, 3);
    public static final VoxelShape LEG2 = Block.createCuboidShape(0, 0, 13, 3, 3, 16);
    public static final VoxelShape LEG3 = Block.createCuboidShape(13, 0, 0, 16, 3, 3);
    public static final VoxelShape LEG4 = Block.createCuboidShape(13, 0, 13, 16, 3, 16);
    public static final VoxelShape SHAPE = VoxelShapes.union(BASE_SHAPE, TOP_SHAPE, LEG1, LEG2, LEG3, LEG4);

    public TransfiniteAltar(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(COLOR, 0).with(FLOWER, false));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(COLOR);
        builder.add(FLOWER);
    }

    public void setColor(World world, BlockPos pos, BlockState state, int i) {
        world.setBlockState(pos, state.with(COLOR, i));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);
        if (world instanceof ServerWorld serverWorld) {
            if (itemStack.isEmpty()) {
                if (world.getBlockEntity(pos.up()) instanceof BiomeBottleBlockEntity bbbe) {
                    if (player instanceof ServerPlayerEntity spe) ModCriteria.BIOME_BOTTLE.get().trigger(spe, bbbe);
                    bbbe.startTicking();
                }
                if (world.getBlockState(pos.up()).isOf(ModBlocks.ANT.get())) {
                    world.removeBlock(pos.up(), false);
                    (new AntBattle(serverWorld)).start(pos.up());
                }
            }

            //coloration
            if (itemStack.getItem() instanceof DyeItem) {
                int i = getColor(itemStack, state);
                if (i>=0 && state.get(COLOR) != i) {
                    setColor(world, pos, state, i);
                    world.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1f, 1f);
                }
                return ActionResult.SUCCESS;
            }
            if (itemStack.isOf(Items.SUNFLOWER)) {
                world.setBlockState(pos, state.with(FLOWER, !state.get(FLOWER)));
                world.playSound(null, pos, SoundEvents.BLOCK_AZALEA_LEAVES_PLACE, SoundCategory.BLOCKS, 1f, 1f);
                return ActionResult.SUCCESS;
            }
        }
        if (itemStack.isOf(Items.SUNFLOWER)) return ActionResult.SUCCESS;
        return super.onUse(state, world, pos, player, hit);
    }

    public static int getColor(ItemStack itemStack, BlockState oldState) {
        int i = -1;
        if (itemStack.isOf(ModItems.IRIDESCENT_WOOL.get())) i = (oldState.get(COLOR) + 1) % numColors;
        if (itemStack.isOf(Items.RED_DYE)) i = 1;
        if (itemStack.isOf(Items.ORANGE_DYE)) i = 2;
        if (itemStack.isOf(Items.YELLOW_DYE)) i = 3;
        if (itemStack.isOf(Items.LIME_DYE)) i = oldState.get(COLOR) == 6 ? 5 : 4;
        if (itemStack.isOf(Items.GREEN_DYE)) i = oldState.get(COLOR) == 4 ? 5 : 6;
        if (itemStack.isOf(Items.LIGHT_BLUE_DYE)) i = 7;
        if (itemStack.isOf(Items.CYAN_DYE)) i = 8;
        if (itemStack.isOf(Items.BLUE_DYE)) i = 9;
        if (itemStack.isOf(Items.PURPLE_DYE)) i = 10;
        if (itemStack.isOf(Items.MAGENTA_DYE)) i = 11;
        if (itemStack.isOf(Items.PINK_DYE)) i = 12;
        if (itemStack.isOf(Items.GRAY_DYE)) i = 0;
        return i;
    }
}
