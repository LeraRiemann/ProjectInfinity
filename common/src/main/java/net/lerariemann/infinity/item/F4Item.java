package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.util.BackportMethods;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.screen.F4Screen;
import net.lerariemann.infinity.util.teleport.InfinityPortal;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;

import java.util.*;

public class F4Item extends Item implements PortalDataHolder {
    static final BlockState OBSIDIAN = Blocks.OBSIDIAN.getDefaultState();

    public F4Item(Settings settings) {
        super(settings);
    }

    public static MutableText getDimensionTooltip(Identifier dimension) {
        if (dimension == null) return Text.translatable(Blocks.NETHER_PORTAL.getTranslationKey());
        String name = dimension.toString();
        MutableText text = InfinityMethods.getDimensionNameAsText(dimension);
        if (name.contains("infinity:generated_") || name.equals(InfinityMethods.ofRandomDim))
            return text;
        return Text.translatable("tooltip.infinity.f4", text);
    }


    public static int getCharge(ItemStack f4) {
        if (f4.hasNbt()) {
            assert f4.getNbt() != null;
            return f4.getNbt().getInt("f4_charge");
        }
        return 0;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext type) {
        super.appendTooltip(stack, world, tooltip, type);
        Identifier dimension = BackportMethods.getDimensionIdentifier(stack);
        MutableText mutableText = getDimensionTooltip(dimension);
        tooltip.add(mutableText.formatted(Formatting.GRAY));
        MutableText mutableText2 = Text.translatable("tooltip.infinity.f4.charges", getCharge(stack));
        tooltip.add(mutableText2.formatted(Formatting.GRAY));
    }

    public static ItemStack placePortal(World world, PlayerEntity player, ItemStack stack, BlockPos lowerCenter,
                                    int size_x, int size_y) {
        Direction.Axis dir2 = player.getHorizontalFacing().rotateClockwise(Direction.Axis.Y).getAxis();

        int charges = getCharge(stack);
        int useCharges = player.isCreative() ? 0 : 2*(2 + size_x + size_y);
        if (charges < useCharges) {
            if (!world.isClient())
                player.sendMessage(Text.translatable("error.infinity.f4.no_charges", useCharges));
            return null;
        }
        BlockPos lowerLeft = lowerCenter.offset(dir2, -(size_x/2));
        Identifier id = BackportMethods.getDimensionIdentifier(stack);
        boolean doNotRenderPortal = (world.isClient && (id == null || !id.getPath().contains("generated_")));
        if (PortalDataHolder.isDestinationRandom(id))
            id = InfinityMethods.getRandomId(world.random);
        int obsNotReplaced = 0;

        //placing the portal
        for (int x = -1; x <= size_x; x++) {
            if (!world.setBlockState(lowerLeft.offset(dir2, x).up(size_y), OBSIDIAN)) obsNotReplaced++;
            if (!world.setBlockState(lowerLeft.offset(dir2, x).down(), OBSIDIAN)) obsNotReplaced++;
        }
        for (int y = 0; y < size_y; y++) {
            if (!world.setBlockState(lowerLeft.offset(dir2, -1).offset(Direction.UP, y), OBSIDIAN)) obsNotReplaced++;
            if (!world.setBlockState(lowerLeft.offset(dir2, size_x).offset(Direction.UP, y), OBSIDIAN)) obsNotReplaced++;
            if (!doNotRenderPortal) for (int x = 0; x < size_x; x++) {
                BlockPos pos = lowerLeft.offset(dir2, x).offset(Direction.UP, y);
                world.setBlockState(pos,
                        ((id == null) ? Blocks.NETHER_PORTAL : ModBlocks.PORTAL.get())
                                .getDefaultState().with(NetherPortalBlock.AXIS, dir2));
                if (id != null && world.getBlockEntity(pos) instanceof InfinityPortalBlockEntity ipbe) {
                    ipbe.setData(world.getServer(), id);
                }
            }
        }
        useCharges -= obsNotReplaced;

        world.playSound(player, player.getBlockPos(), SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 1, 0.75f);
        BackportMethods.apply(stack, ModComponentTypes.F4_CHARGE, charges-useCharges);
        return stack;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (player instanceof ClientPlayerEntity clientPlayer) {
            MinecraftClient.getInstance().setScreen(F4Screen.of(clientPlayer));
        }
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    public static TypedActionResult<ItemStack> deploy(World world, PlayerEntity player, Hand hand) {
        Direction dir = player.getHorizontalFacing();
        Direction.Axis dir2 = dir.rotateClockwise(Direction.Axis.Y).getAxis();
        BlockPos lowerCenter = player.getBlockPos().offset(dir, 4);
        ItemStack stack = player.getStackInHand(hand);

        int size_x = BackportMethods.getOrDefaultInt(stack, ModComponentTypes.SIZE_X, 3);
        int size_y =  BackportMethods.getOrDefaultInt(stack, ModComponentTypes.SIZE_Y, 3);

        if (size_y % 2 == 0) {
            double d = dir2.equals(Direction.Axis.X) ? player.getPos().x : player.getPos().z;
            if (d % 1 > 0.5) { //player on the positive side of the block
                lowerCenter = lowerCenter.offset(dir2, 1);
            }
        }

        int lowerY = lowerCenter.getY();
        if (world.isOutOfHeightLimit(lowerY)) return TypedActionResult.pass(stack);

        //finding a place position
        int i;
        boolean positionFound = true;
        for (i = 0; i <= 8 && !world.isOutOfHeightLimit(lowerY + i + size_y); i++) {
            positionFound = true;
            for (int j = 0; j <= size_y+1 && positionFound; j++) for (int k = -1; k <= size_x; k++) {
                BlockState bs = world.getBlockState(lowerCenter.up(i+j-1).offset(dir2, k - (size_x /2)));
                if (bs.isReplaceable()) continue;
                if (bs.isOf(Blocks.OBSIDIAN)) {
                    if (j == 0 || j == size_y+1 || k == -1 || k == size_x) continue;
                    i += j-1;
                }
                else i += j;
                positionFound = false;
                break;
            }
            if (positionFound) break;
        }
        if (!positionFound) return TypedActionResult.pass(stack);

        ItemStack newStack = placePortal(world, player, stack.copy(), lowerCenter.up(i), size_x, size_y);
        if (newStack == null) return TypedActionResult.pass(stack);
        return TypedActionResult.consume(player.isCreative() ? stack : newStack);
    }

    public static boolean isPortal(BlockState state) {
        return state.isOf(Blocks.NETHER_PORTAL) || state.isOf(ModBlocks.PORTAL.get());
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) return ActionResult.FAIL;
        ItemStack stack = context.getStack();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState bs = world.getBlockState(pos);
        Hand hand = context.getHand();

        if (isPortal(bs)) {
            ItemStack newStack = useOnPortalBlock(world, pos, stack.copy());
            if (!player.isCreative()) player.setStackInHand(hand, newStack);
            return ActionResult.CONSUME;
        }
        pos = pos.up(bs.isOf(Blocks.OBSIDIAN) ? 1 : 2);

        Direction.Axis dir2 =
                player.getHorizontalFacing().getAxis().equals(Direction.Axis.X) ? Direction.Axis.Z : Direction.Axis.X;

        int size_x = BackportMethods.getOrDefaultInt(stack, ModComponentTypes.SIZE_X, 3);
        int size_y = BackportMethods.getOrDefaultInt(stack, ModComponentTypes.SIZE_Y, 3);

        //validating the place position
        for (int j = -1; j <= size_y; j++) for (int k = -1; k <= size_x; k++) {
            bs = world.getBlockState(pos.up(j).offset(dir2, k - (size_x /2)));
            if (bs.isOf(Blocks.OBSIDIAN) && (j == -1 || j == size_y || k == -1 || k == size_x)) continue;
            if (!bs.isReplaceable()) return ActionResult.FAIL;
        }

        ItemStack newStack = placePortal(world, player, context.getStack().copy(), pos,
                size_x, size_y);
        if (newStack == null) {
            return ActionResult.FAIL;
        }
        if (!player.isCreative()) player.setStackInHand(hand, newStack);
        return ActionResult.CONSUME;
    }

    public static boolean checkIfValidAxis(Direction.Axis axisFound, Direction.Axis axisBeingChecked, Direction.Axis forceAxis) {
        if (forceAxis == null) {
            return axisBeingChecked.isVertical() || axisFound.equals(axisBeingChecked);
        }
        return axisFound.equals(forceAxis);
    }

    public static boolean checkNeighbors(World world, BlockPos bp,
                                         Collection<Direction> primaryOffsets, //possible directions in which blocks of other portals may be
                                         Collection<Direction> secondaryOffsets, //possible secondary directions from a neighboring obsidian in case we found a corner
                                         int max,
                                         Direction.Axis forcePrimaryAxis) {
        int i = 0;
        boolean checkCorners = !secondaryOffsets.isEmpty();
        for (Direction dir : primaryOffsets) {
            BlockState bs = world.getBlockState(bp.offset(dir));
            Direction.Axis axis = dir.getAxis();
            if (isPortal(bs)) {
                Direction.Axis axisFound = bs.get(Properties.HORIZONTAL_AXIS);
                if (checkIfValidAxis(axisFound, axis, forcePrimaryAxis))
                    if (++i > max) return true;
            }
            if (checkCorners && bs.isOf(Blocks.OBSIDIAN)) {
                Direction.Axis forceAxis = axis.isHorizontal() ? axis : null;
                return checkNeighbors(world, bp.offset(dir), secondaryOffsets, Set.of(), 0, forceAxis);
            }
        }
        return false;
    }

    public static void checkObsidianRemovalSides(World world, BlockPos bp,
                                                 Set<BlockPos> toRemove,
                                                 Set<BlockPos> toLeave,
                                                 Direction direction) {
        if (world.getBlockState(bp).isOf(Blocks.OBSIDIAN)) {
            boolean bl = direction.getAxis().isVertical();
            Set<Direction> primaryOffsets =  bl ? Set.of(direction) : Set.of(direction,
                    direction.rotateClockwise(Direction.Axis.Y),
                    direction.rotateCounterclockwise(Direction.Axis.Y));
            Set<Direction> secondaryOffsets = bl ? Set.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST) :
                    Set.of(Direction.UP, Direction.DOWN);
            if (checkNeighbors(world, bp, primaryOffsets, secondaryOffsets, 0, null)) {
                toLeave.add(bp);
                return;
            }
            toRemove.add(bp);
        }
    }

    public static ItemStack useOnPortalBlock(World world, BlockPos origin, ItemStack stack) {
        Direction.Axis axis = world.getBlockState(origin).get(NetherPortalBlock.AXIS);
        Direction positive = axis.equals(Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;
        BlockLocating.Rectangle portal = InfinityPortal.getRect(world, origin);
        Set<BlockPos> toRemove = new HashSet<>();
        Set<BlockPos> toLeave = new HashSet<>();
        for (int i = -1; i <= portal.width; i++) {
            checkObsidianRemovalSides(world, portal.lowerLeft.offset(axis, i).up(-1), toRemove, toLeave, Direction.DOWN);
            checkObsidianRemovalSides(world, portal.lowerLeft.offset(axis, i).up(portal.height), toRemove, toLeave, Direction.UP);
        }
        for (int j = -1; j <= portal.height; j++) {
            checkObsidianRemovalSides(world, portal.lowerLeft.offset(axis, -1).up(j), toRemove, toLeave, positive.getOpposite());
            checkObsidianRemovalSides(world, portal.lowerLeft.offset(axis, portal.width).up(j), toRemove, toLeave, positive);
        }
        int obsidian = 0;
        for (BlockPos bp : toRemove) if (!toLeave.contains(bp)) { //double check since we're checking the corners twice
            world.setBlockState(bp,  Blocks.AIR.getDefaultState(), 3, 0);
            obsidian++;
        }
        for (int i = 0; i <= portal.width; i++) for (int j = -1; j <= portal.height; j++)
            world.setBlockState(portal.lowerLeft.offset(axis, i).up(j), Blocks.AIR.getDefaultState(), 3, 0);
        BackportMethods.apply(stack, ModComponentTypes.F4_CHARGE, getCharge(stack) + obsidian);
        world.playSound(null, origin, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1, 0.75f);
        return stack;
    }
}