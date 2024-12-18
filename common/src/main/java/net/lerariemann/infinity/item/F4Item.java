package net.lerariemann.infinity.item;

import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class F4Item extends PortalDataHolder {
    public F4Item(Settings settings) {
        super(settings);
    }

    @Override
    public MutableText getDimensionTooltip(Identifier dimension) {
        String s = dimension.toString();
        // Randomly generated dimensions.
        if (s.contains("infinity:generated_"))
            return Text.translatable("tooltip.infinity.key.generated")
                    .append(s.replace("infinity:generated_", ""));
        // All other dimensions.
        return Text.translatableWithFallback(
                Util.createTranslationKey("dimension", dimension),
                InfinityMethods.fallback(dimension.getPath()));
    }

    public static int getCharge(ItemStack f4) {
        return f4.getOrDefault(ModItemFunctions.CHARGE.get(), 0);
    }

    @Override
    public MutableText defaultDimensionTooltip() {
        return Text.translatable("tooltip.infinity.f4.default");
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        MutableText mutableText = Text.translatable("tooltip.infinity.f4.charges", getCharge(stack));
        tooltip.add(mutableText.formatted(Formatting.GRAY));
    }

    public ItemStack placePortal(World world, PlayerEntity player, ItemStack stack, BlockPos lowerCenter,
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
        Identifier id = getDestination(stack);

        //placing the portal
        for (int x = -1; x <= size_x; x++) {
            world.setBlockState(lowerLeft.offset(dir2, x).up(size_y), Blocks.OBSIDIAN.getDefaultState());
            world.setBlockState(lowerLeft.offset(dir2, x).down(), Blocks.OBSIDIAN.getDefaultState());
        }
        for (int y = 0; y < size_y; y++) {
            world.setBlockState(lowerLeft.offset(dir2, -1).offset(Direction.UP, y), Blocks.OBSIDIAN.getDefaultState());
            world.setBlockState(lowerLeft.offset(dir2, size_x).offset(Direction.UP, y), Blocks.OBSIDIAN.getDefaultState());
            for (int x = 0; x < size_x; x++) {
                BlockPos pos = lowerLeft.offset(dir2, x).offset(Direction.UP, y);
                world.setBlockState(pos,
                        ((id == null) ? Blocks.NETHER_PORTAL : ModBlocks.PORTAL.get())
                                .getDefaultState().with(NetherPortalBlock.AXIS, dir2));
                if (id != null && world.getBlockEntity(pos) instanceof InfinityPortalBlockEntity ipbe) {
                    ipbe.setData(world.getServer(), id);
                }
            }
        }

        player.playSound(SoundEvents.BLOCK_BELL_USE, 1, 0.75f);
        stack.applyComponentsFrom(ComponentMap.builder()
                .add(ModItemFunctions.CHARGE.get(), charges - useCharges).build());
        return stack;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        Direction dir = player.getHorizontalFacing();
        Direction.Axis dir2 = dir.rotateClockwise(Direction.Axis.Y).getAxis();
        BlockPos lowerCenter = player.getBlockPos().offset(dir, 4);
        ItemStack stack = player.getStackInHand(hand);

        int size_x = stack.getOrDefault(ModItemFunctions.SIZE_X.get(), 3);
        int size_y = stack.getOrDefault(ModItemFunctions.SIZE_Y.get(), 3);
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
            for (int j = 0; j < size_y; j++) for (int k = 0; k < size_x; k++) {
                if (!world.getBlockState(lowerCenter.up(i+j).offset(dir2, k - (size_x /2))).isAir()) {
                    i += j;
                    positionFound = false;
                    break;
                }
            }
            if (positionFound) break;
        }
        if (!positionFound) return TypedActionResult.pass(stack);

        return TypedActionResult.consume(placePortal(world, player, stack, lowerCenter.up(i), size_x, size_y));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) return ActionResult.FAIL;
        Direction.Axis dir2 =
                player.getHorizontalFacing().getAxis().equals(Direction.Axis.X) ? Direction.Axis.Z : Direction.Axis.X;
        ItemStack stack = context.getStack();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos().up();

        int size_x = stack.getOrDefault(ModItemFunctions.SIZE_X.get(), 3);
        int size_y = stack.getOrDefault(ModItemFunctions.SIZE_Y.get(), 3);

        //validating the place position
        for (int j = 0; j < size_y; j++) for (int k = 0; k < size_x; k++) {
            if (!world.getBlockState(pos.up(j).offset(dir2, k - (size_x /2))).isAir()) {
                return ActionResult.FAIL;
            }
        }

        ItemStack stackNew = placePortal(world, player, context.getStack(), pos,
                size_x, size_y);
        if (stackNew == null) {
            return ActionResult.FAIL;
        }
        player.setStackInHand(context.getHand(), stack);
        return ActionResult.CONSUME;
    }
}
