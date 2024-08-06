package net.lerariemann.infinity.block.custom;

import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.TransfiniteAltarEntity;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Objects;

public class TransfiniteAltarBase extends Block {
    public static final BooleanProperty FLOWER = TransfiniteAltar.FLOWER;
    public static final IntProperty COLOR = TransfiniteAltar.COLOR;
    public static final VoxelShape SHAPE = TransfiniteAltar.SHAPE;

    public TransfiniteAltarBase(Settings settings) {
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

    public void setAge(World world, BlockPos pos, BlockState state, int i) {
        world.setBlockState(pos, state.with(COLOR, i));
    }

    static boolean testSpace(World world, BlockPos pos) {
        for (int i : TransfiniteAltarEntity.offsets) for (int j : TransfiniteAltarEntity.offsets_y) for (int k : TransfiniteAltarEntity.offsets) {
            if (!world.getBlockState(pos.add(i, j, k)).isOf(Blocks.AIR)) return false;
        }
        return true;
    }

    public static void ignite(World world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, ModBlocks.ALTAR_LIT.getDefaultState().with(FLOWER, state.get(FLOWER)));
        world.playSound(null, pos, SoundEvents.ITEM_TOTEM_USE, SoundCategory.BLOCKS, 1f, 1f);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (!world.isClient) {
            String s = ((MinecraftServerAccess)(Objects.requireNonNull(world.getServer()))).getDimensionProvider().altarKey;
            boolean bl0 = s.isBlank() ? itemStack.isEmpty() : itemStack.isOf(Registries.ITEM.get(new Identifier(s)));
            if (bl0) {
                boolean bl = testSpace(world, pos);
                if (!bl) {
                    return ActionResult.FAIL;
                }
                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }
                ignite(world, pos, state);
                return ActionResult.SUCCESS;
            }
            if (itemStack.getItem() instanceof DyeItem) {
                int i = -1;
                if (itemStack.isOf(Items.RED_DYE)) i = 1;
                if (itemStack.isOf(Items.ORANGE_DYE)) i = 2;
                if (itemStack.isOf(Items.YELLOW_DYE)) i = 3;
                if (itemStack.isOf(Items.GREEN_DYE)) i = 4;
                if (itemStack.isOf(Items.BLUE_DYE)) i = 5;
                if (itemStack.isOf(Items.PURPLE_DYE)) i = 6;
                if (itemStack.isOf(Items.GRAY_DYE)) i = 0;
                if (i>=0) {
                    setAge(world, pos, state, i);
                    world.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1f, 1f);
                }
            }
            if (itemStack.isOf(Items.SUNFLOWER)) {
                world.setBlockState(pos, state.with(FLOWER, true));
                world.playSound(null, pos, SoundEvents.BLOCK_AZALEA_LEAVES_PLACE, SoundCategory.BLOCKS, 1f, 1f);
            }
            if (itemStack.isOf(Items.SHEARS)) {
                world.setBlockState(pos, state.with(FLOWER, false));
                world.playSound(null, pos, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.BLOCKS, 1f, 1f);
            }
        }
        return ActionResult.FAIL;
    }
}
