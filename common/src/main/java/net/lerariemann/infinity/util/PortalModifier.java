package net.lerariemann.infinity.util;

import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.lerariemann.infinity.options.PortalColorApplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public interface PortalModifier {
    void modify(World world, BlockPos pos);

    static PortalModifier onInitialCollision(World world, BlockState state, Identifier id, boolean open) {
        return new Union().addSetupper(new SetBlock(state.get(NetherPortalBlock.AXIS)))
                .addModifier(new DimChange(id))
                .addModifier(new Recolor(world, id))
                .addModifier(new Open(open));
    }

    record Union(List<PortalModifier> setuppers, List<BlockEntityBased> modifiers) implements PortalModifier {
        Union() {
            this(new ArrayList<>(), new ArrayList<>());
        }
        Union addSetupper(PortalModifier setupper) {
            setuppers.add(setupper);
            return this;
        }
        Union addModifier(BlockEntityBased modifier) {
            modifiers.add(modifier);
            return this;
        }

        @Override
        public void modify(World world, BlockPos pos) {
            setuppers.forEach(modifier -> modifier.modify(world, pos));
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof NeitherPortalBlockEntity npbe)
                modifiers.forEach(modifier -> modifier.modify(npbe));
        }
    }

    record SetBlock(Direction.Axis axis) implements PortalModifier {
        @Override
        public void modify(World world, BlockPos pos) {
            world.setBlockState(pos, ModBlocks.NEITHER_PORTAL.get()
                    .getDefaultState()
                    .with(NetherPortalBlock.AXIS, axis));
        }
    }

    interface BlockEntityBased extends PortalModifier {
        void modify(NeitherPortalBlockEntity npbe);
        default void modify(World world, BlockPos pos) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof NeitherPortalBlockEntity npbe) modify(npbe);
        }
    }

    record DimChange(Identifier id) implements BlockEntityBased {
        @Override
        public void modify(NeitherPortalBlockEntity npbe) {
            npbe.setDimension(id);
        }
    }
    class Recolor implements BlockEntityBased {
        PortalColorApplier applier;

        Recolor(World world, Identifier id) {
            applier = WarpLogic.getPortalColorApplier(id, world.getServer());
        }

        @Override
        public void modify(NeitherPortalBlockEntity npbe) {
            npbe.setColor(applier.apply(npbe.getPos()));
        }
    }
    record Open(boolean open) implements BlockEntityBased {
        @Override
        public void modify(NeitherPortalBlockEntity npbe) {
            npbe.setOpen(open);
        }
    }
}
